package com.example.quackapp.Controller

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.ListCampInLotteryRoute
import com.example.quackapp.Model.Lottery
import com.example.quackapp.Model.LotteryStatus
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

/**
 * Controller for the ListCampInLottery screen.
 *
 * Holds some state variables to display on the form.
 * Contains logic to create new lotteries for the selected [Campsite]
 *
 * @property savedStateHandle a map used to access outside data. Used to get campsiteId in this case
 */
class ListCampInLotteryController(savedStateHandle: SavedStateHandle): ViewModel() {
    private val route = savedStateHandle.toRoute<ListCampInLotteryRoute>()
    val campsiteIds: List<String> = route.campsiteIds
    val multiCampLottery = campsiteIds.size > 1
    private val campsiteRef = Firebase.firestore.collection("campsites")
    private val db = Firebase.firestore
    private val lotteryRef = db.collection("lotteries")

    var registrationStart by mutableStateOf<Long?>(null)
    var registrationEnd by mutableStateOf<Long?>(null)
    var bookingStart by mutableStateOf<Long?>(null)
    var bookingEnd by mutableStateOf<Long?>(null)
    var name by mutableStateOf("")
    var availability by mutableStateOf(listOf<Long>())
    var availabilityStart by mutableStateOf<Long?>(null)
    var availabilityEnd by mutableStateOf<Long?>(null)

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
            if (multiCampLottery){
                name = "Multi-Camp"
            }
            else {
                try {
                    val campsite = campsiteRef.document(campsiteIds[0]).get().await().toObject(Campsite::class.java)
                    if (campsite != null) {
                        name = campsite.name
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
    }

    /**
     * Validates the inputted registration and booking periods
     * @return a String message of the error or null if all good
     */
    private fun validateDates(): String? {
        val open = registrationStart; val close = registrationEnd
        val start = bookingStart; val end = bookingEnd
        val availStart = availabilityStart; val availEnd = availabilityEnd

        if (open == null || close == null || start == null || end == null)
            return "Pick registration and booking dates"
        if (availStart == null || availEnd == null)
            return "Pick availability dates"
        if (close < open) return "Registration must close after it opens"
        if (start <= close) return "Booking must start after registration closes"
        if (end < start) return "Booking must end after it starts"
        if (availStart < end) return "Availability must start after booking ends"
        if (availEnd <= availStart) return "Availability must end after it starts"
        return null
    }


    /**
     * Adds a new [Lottery] object to the Firestore.
     *
     * @param numWinners the number of winners allowed for the lottery
     * @param waitlistCap the number of people allowed in the waitlist, or null if no limit
     */
    fun addLottery(
        numWinners: String,
        waitlistCap: String,
    ) {
        validateDates()?.let {
            _status.value = SaveStatus.Error(it)
            return
        }
        if (numWinners.isBlank()) {
            _status.value = SaveStatus.Error("Lottery must have winners")
            return
        }


        viewModelScope.launch {
            // Change status to saving
            _status.value = SaveStatus.Saving
            try {
                // Create database references, campsite object will be linked by their ids (document name)
                val docRef = lotteryRef.document()
                var cap: Int? = null
                if (waitlistCap != ""){
                    cap = waitlistCap.toInt()
                }
                // Make a new lottery object
                val newLottery = Lottery(
                    lotteryID = docRef.id,
                    registrationPeriodStart = registrationStart,
                    registrationPeriodEnd = registrationEnd,
                    bookingPeriodStart = bookingStart,
                    bookingPeriodEnd = bookingEnd,
                    numLotteryWinners = numWinners.toInt(),
                    optionalWaitlistCapacity = cap,
                    campIds = campsiteIds,
                    status = LotteryStatus.OPEN
                )

                // upload to firestore
                docRef.set(newLottery).await()
                // update the campsite to have an active lottery
                for (campId in campsiteIds) {
                    campsiteRef.document(campId).update(mapOf("lotteryId" to newLottery.lotteryID)).await()
                    availability = (availabilityStart!!..availabilityEnd!! step 86400000).toList()
                    campsiteRef.document(campId).update(mapOf("availability" to availability))
                }
                _status.value = SaveStatus.Success
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }

        }
    }
}