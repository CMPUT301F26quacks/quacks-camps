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
    val campsiteId: String = checkNotNull(savedStateHandle["campsiteId"])
    private val campsiteRef = Firebase.firestore.collection("campsites").document(campsiteId)
    private val db = Firebase.firestore
    private val lotteryRef = db.collection("lotteries")

    var registrationStart by mutableStateOf<Long?>(null)
    var registrationEnd by mutableStateOf<Long?>(null)
    var bookingStart by mutableStateOf<Long?>(null)
    var bookingEnd by mutableStateOf<Long?>(null)
    var name by mutableStateOf("")
    var lottery by mutableStateOf<String?>(null)
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
                    lottery = campsite.lotteryId
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

    /**
     * Validates the inputted registration and booking periods
     * @return a String message of the error or null if all good
     */
    private fun validateDates(): String? {
        val open = registrationStart; val close = registrationEnd
        val start = bookingStart; val end = bookingEnd
        if (open == null || close == null || start == null || end == null)
            return "Pick registration and booking dates"
        if (close < open) return "Registration must close after it opens"
        if (start <= close) return "Booking must start after registration closes"
        if (end < start) return "Booking must end after it starts"
        return null
    }


    /**
     * Adds a new [Lottery] object to the Firestore.
     *
     * @param numWinners the number of winners allowed for the lottery
     * @param waitlistCap the number of people allowed in the waitlist, or null if no limit
     */
    fun addLottery(
        numWinners: Int,
        waitlistCap: Int?,
    ) {
        validateDates()?.let {
            _status.value = SaveStatus.Error(it)
            return
        }
        viewModelScope.launch {
            // Change status to saving
            _status.value = SaveStatus.Saving
            try {
                // Create database references, any image and campsite object will be linked by their ids (document name)
                val docRef = lotteryRef.document()

                // Make a new lottery object
                val newLottery = Lottery(
                    lotteryID = docRef.id,
                    registrationPeriodStart = Timestamp(Date(registrationStart!!)),
                    registrationPeriodEnd = Timestamp(Date(registrationEnd!!)),
                    bookingPeriodStart = Timestamp(Date(bookingStart!!)),
                    bookingPeriodEnd = Timestamp(Date(bookingEnd!!)),
                    numLotteryWinners = numWinners,
                    optionalWaitlistCapacity = waitlistCap,
                    campIds = listOf(campsiteId),
                    status = LotteryStatus.OPEN
                )

                // upload to firestore
                docRef.set(newLottery).await()
                // update the campsite to have an active lottery
                campsiteRef.update(mapOf("lotteryId" to newLottery.lotteryID)).await()
                _status.value = SaveStatus.Success
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }

        }
    }
}