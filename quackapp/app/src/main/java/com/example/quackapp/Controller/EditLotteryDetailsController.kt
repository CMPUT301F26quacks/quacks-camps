package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
<<<<<<< HEAD
=======
import androidx.compose.runtime.mutableStateListOf
>>>>>>> dev
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

/**
 * Controller for the editing [Lottery] details
 *
 * Holds some state variables to display on the form.
 * Contains logic to edit the [Lottery] containing the [Campsite]
 *
 * @property savedStateHandle a map used to access outside data. Used to get campsiteId in this case
 */
class EditLotteryDetailsController(savedStateHandle: SavedStateHandle): ViewModel() {
    val campsiteId: String = checkNotNull(savedStateHandle["campsiteId"])

    private val db = Firebase.firestore
<<<<<<< HEAD
    private val campsiteRef = db.collection("campsites").document(campsiteId)
=======
    private val campsiteRef = db.collection("campsites")
>>>>>>> dev
    private val lotteryRef = db.collection("lotteries")
    var registrationStart by mutableStateOf<Long?>(null)
    var registrationEnd by mutableStateOf<Long?>(null)
    var bookingStart by mutableStateOf<Long?>(null)
    var bookingEnd by mutableStateOf<Long?>(null)
    var numWinners by mutableStateOf("")
    var waitlistCap by mutableStateOf("")
    var newAvailability by mutableStateOf(listOf<Long>())
    var availabilityStart by mutableStateOf<Long?>(null)
    var availabilityEnd by mutableStateOf<Long?>(null)
<<<<<<< HEAD

    var name by mutableStateOf("")
=======
    var names by mutableStateOf(listOf<String>())
        private set
    var campsites = arrayListOf<Campsite>()
>>>>>>> dev
    var lotteryId by mutableStateOf<String>("")
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
<<<<<<< HEAD
                val campsite = campsiteRef.get().await().toObject(Campsite::class.java)
                if (campsite != null) {
                    name = campsite.name
=======
                val campsite = campsiteRef.document(campsiteId).get().await().toObject(Campsite::class.java)
                if (campsite != null) {
>>>>>>> dev
                    // If this file is active, a lottery must exist
                    lotteryId = campsite.lotteryId!!
                    availabilityStart = campsite.availability[0]
                    availabilityEnd = campsite.availability.last()
                } else {
                    _status.value = SaveStatus.Error("Campsite not found")
                }
                val lottery = lotteryRef.document(lotteryId).get().await().toObject(Lottery::class.java)
                registrationStart = lottery!!.registrationPeriodStart
                registrationEnd = lottery.registrationPeriodEnd
                bookingStart = lottery.bookingPeriodStart
                bookingEnd = lottery.bookingPeriodEnd
                lottery.optionalWaitlistCapacity?.let{
                    waitlistCap = lottery.optionalWaitlistCapacity.toString()
                }
                numWinners = lottery.numLotteryWinners.toString()
<<<<<<< HEAD
=======
                for (campId in lottery.campIds){
                    val campsite: Campsite? = campsiteRef.document(campId).get().await().toObject(Campsite::class.java)
                    campsites.add(campsite!!)
                    names = names + campsite.name
                }
>>>>>>> dev
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
        availabilityStart?.let { if (it < end) return "Availability must start after booking ends" }
        if (availabilityEnd == null || availabilityEnd!! <= availabilityStart!!) return "Availability must be after booking period"
        return null
    }


    /**
     * Updates a [Lottery] object in the Firestore.
     *
     */
    fun updateLottery() {
        validateDates()?.let {
            _status.value = SaveStatus.Error(it)
            return
        }
        viewModelScope.launch {
            // Change status to saving
            _status.value = SaveStatus.Saving
            try {
                // Create database references, any image and campsite object will be linked by their ids (document name)
                val docRef = lotteryRef.document(lotteryId)

                docRef.update(
                    mapOf(
                        "registrationPeriodStart" to registrationStart,
                        "registrationPeriodEnd" to registrationEnd,
                        "bookingPeriodStart" to bookingStart,
                        "bookingPeriodEnd" to bookingEnd,
                        "numLotteryWinners" to numWinners.toInt(),
                        "optionalWaitlistCapacity" to if (waitlistCap != "") waitlistCap.toInt() else null,
                    )
                ).await()
                newAvailability = (availabilityStart!!..availabilityEnd!! step 86400000).toList()
<<<<<<< HEAD
                // update the campsite's availability'
                campsiteRef.update(mapOf("availability" to newAvailability)).await()
=======
                for (camp in campsites) {
                    // update the campsites availability
                    campsiteRef.document(camp.campId).update(mapOf("availability" to newAvailability)).await()
                }
>>>>>>> dev
                _status.value = SaveStatus.Success
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }

        }
    }
}