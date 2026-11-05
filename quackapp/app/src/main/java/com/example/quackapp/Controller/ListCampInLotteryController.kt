package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import com.example.quackapp.Model.LotteryStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class ListCampInLotteryController(savedStateHandle: SavedStateHandle): ViewModel() {
    private val campsiteId: String = checkNotNull(savedStateHandle["campsiteId"])
    private val campsiteRef = Firebase.firestore.collection("campsites").document(campsiteId)
    private val db = Firebase.firestore
    private val lotteryRef = db.collection("lotteries")

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
    var lottery by mutableStateOf<Lottery?>(null)
    var isLoading by mutableStateOf(true)
        private set
    private val _status = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val status: StateFlow<SaveStatus> = _status

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
                    lottery = campsite.lottery
                } else {
                    _status.value = SaveStatus.Error("Campsite not found")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Couldn't load campsite")
            } finally {
                isLoading = false
            }
        }
    }

    fun addLottery(
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
        numWinners: Int,
        waitlistCap: Int?,
    ) {


        viewModelScope.launch {
            // Change status to saving
            _status.value = SaveStatus.Saving
            try {
                // Create database references, any image and campsite object will be linked by their ids (document name)
                val docRef = lotteryRef.document()

                // Make a new campsite object to store
                val lottery = Lottery(
                    lotteryID = docRef.id,
                    registrationPeriodStart = registrationStart,
                    registrationPeriodEnd = registrationEnd,
                    bookingPeriodStart = bookingStart,
                    bookingPeriodEnd = bookingEnd,
                    numLotteryWinners = numWinners,
                    optionalWaitlistCapacity = waitlistCap,
                    campIds = listOf(campsiteId),
                    status = LotteryStatus.OPEN
                )

                // upload to firestore
                docRef.set(lottery).await()
                _status.value = SaveStatus.Success
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }

        }
    }
}