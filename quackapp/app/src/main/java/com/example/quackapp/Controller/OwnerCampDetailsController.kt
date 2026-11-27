package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationRepository
import com.example.quackapp.Model.SaveStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException


/**
 * Controller for the "Detailed Campsite" screen.
 *
 * Fetches a campsite from Firestore and stores its fields into their
 * respective state variables
 *
 * @param savedStateHandle a map used to access outside data. Used to get campsiteId in this case
 * @property campsiteId unique camp ID. This is the camp whose details we grab from Firestore
 * @property campsiteRef reference to the campsite document in Firestore
 * @property name campsite's name fetched from the
 * @property location campsite's location
 * @property price campsite's price
 * @property capacity campsite's max capacity
 * @property features campsite's amenities/features
 * @property rules campsite's rules
 * @property cancellationPolicy campsite's cancellation policy
 * @property selectedTags campsite's tags
 * @property latitude campsite's map latitude
 * @property longitude campsite's map longitude
 * @property isLoading status of loading the campsite from Firestore
 * @property _status private status of loading info from Firestore. Holds error messages too
 * @property status public status of loading info from Firestore. Holds error messages too
 */
class OwnerCampDetailsController(savedStateHandle: SavedStateHandle): ViewModel() {
    val campsiteId: String = checkNotNull(savedStateHandle["campsiteId"])
    private val campsiteRef = Firebase.firestore.collection("campsites").document(campsiteId)
    private val lotteryRef = Firebase.firestore.collection("lotteries")

    var name by mutableStateOf("")
    var location by mutableStateOf("")
    var price by mutableStateOf("")
    var capacity by mutableStateOf("")
    var features by mutableStateOf(listOf(""))
        private set
    var rules by mutableStateOf(listOf(""))
    var cancellationPolicy by mutableStateOf("")
    var selectedTags by mutableStateOf(setOf<String>())
        private set
    var imageUrl by mutableStateOf("")
        private set
    var latitude by mutableDoubleStateOf(0.00)
    var longitude by mutableDoubleStateOf(0.00)
    var lottery by mutableStateOf<Lottery?>(null)
    var lotteryId by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(true)
        private set
    private val _status = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val status: StateFlow<SaveStatus> = _status
    var rating by mutableStateOf("0.0")
        private set
    var bookedReservations by mutableStateOf<List<Reservation>>(emptyList())
    var waitlistEntrants by mutableStateOf<List<String>>(emptyList())
        private set

    /**
     * Initialization
     *
     * Takes all data from the given campId from Firestore and fill
     * each state variable with its value
     */
    init {
        viewModelScope.launch {
            try {
                val campsite = campsiteRef.get().await().toObject(Campsite::class.java)
                if (campsite != null) {
                    name = campsite.name
                    location = campsite.location
                    price = campsite.price.toString()
                    capacity = campsite.capacity.toString()
                    imageUrl = campsite.optionalPromoImage
                    features = campsite.features
                    rules = campsite.rules
                    cancellationPolicy = campsite.cancellationPolicy
                    selectedTags = campsite.tags.toSet()
                    lotteryId = campsite.lotteryId
                    rating = campsite.rating.toString()
                    longitude = campsite.longitude
                    latitude = campsite.latitude
                }
                else {
                    _status.value = SaveStatus.Error("Campsite not found")
                }
                if (lotteryId != null){
                    val existingLottery = lotteryRef.document(lotteryId!!).get().await().toObject(Lottery::class.java)
                    lottery = existingLottery

                    //Loads waitlist entrants from Lottery.waitlist
                    val rawIds = existingLottery?.waitlist?.filter { it.isNotBlank() } ?: emptyList()
                    val entryIds = if (rawIds.isNotEmpty()) {
                        rawIds
                    } else {
                        val waitlistSnap = Firebase.firestore.collection("waitlist")
                            .whereEqualTo("lotteryID", lotteryId!!)
                            .get()
                            .await()
                        waitlistSnap.documents
                            .mapNotNull { it.getString("entrantID") }
                            .filter { it.isNotBlank() }
                    }

                    //looks for each entrant's name from the "users" collection
                    val usersRef = Firebase.firestore.collection("users")
                    val namesList = mutableListOf<String>()

                    for (id in entryIds) {
                        val userDoc = usersRef.document(id).get().await()
                        val userName = userDoc.getString("name")?.trim()

                        if (!userName.isNullOrEmpty()) {
                            namesList.add(userName)
                        } else {
                            namesList.add("Entrant")
                        }
                    }
                    waitlistEntrants = namesList
                }

                ReservationRepository.getInstance()
                    .getConfirmedReservations(campsiteId) { reservations ->
                        bookedReservations = reservations }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Couldn't load campsite")
            } finally {
                isLoading = false
            }
        }

    }
}