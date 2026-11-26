package com.example.quackapp.Controller

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationRepository



/**
 * Controller for the "Detailed Campsite" screen.
 *
 * Fetches a campsite from Firestore and stores its fields into their
 * respective state variables
 *
 * @property savedStateHandle a map used to access outside data. Used to get campsiteId in this case
 */
class DetailedCampsiteController(savedStateHandle: SavedStateHandle): ViewModel() {
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
                        val userName = userDoc.getString("name")

                        if (userName != null && userName != "") {
                            namesList.add("$userName ($id)")
                        } else {
                            namesList.add(id)
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