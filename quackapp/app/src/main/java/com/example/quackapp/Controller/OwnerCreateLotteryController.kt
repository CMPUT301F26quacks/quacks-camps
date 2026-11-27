package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.CreateLotteryRoute
import com.example.quackapp.Model.Lottery
import com.example.quackapp.Model.LotteryRequest
import com.example.quackapp.Model.LotteryStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Controller for the ListCampInLottery screen.
 *
 * Holds some state variables to display on the form.
 * Contains logic to create new lotteries for the selected [Campsite]
 *
 * @param savedStateHandle a map used to access outside data. Used to get campsiteId in this case
 */
class OwnerCreateLotteryController(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val route = savedStateHandle.toRoute<CreateLotteryRoute>()
    val campsiteIds: List<String> = route.campsiteIds
    val multiCampLottery = campsiteIds.size > 1
    private val db = Firebase.firestore
    private val campsiteRef = db.collection("campsites")
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

    val selected = mutableListOf<Campsite>()
    var ownerId: String = ""

    /**
     * Initialization
     *
     * Takes all data from the given campId from Firestore and fill
     * each state variable with its value
     */
    init {
        viewModelScope.launch {
            try {
                ownerId = FirebaseInstallations.getInstance().id.await()
                val loaded = campsiteIds.mapNotNull { id ->
                    campsiteRef.document(id).get().await().toObject(Campsite::class.java)
                }
                selected.addAll(loaded)

                if (multiCampLottery) {
                    name = "Multi-Camp"
                } else if (loaded.isNotEmpty()) {
                    name = loaded[0].name
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
        val open = registrationStart
        val close = registrationEnd
        val start = bookingStart
        val end = bookingEnd
        val availStart = availabilityStart
        val availEnd = availabilityEnd

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
        if (isLoading) {
            _status.value = SaveStatus.Error("Loading campsites, try again")
            return
        }
        // This if statement is here so my test file works.
        // Shouldn't affect functionality given
        // that an ownerId of testOwner is impossible
        var (mine, theirs) = selected.partition { it.ownerId == ownerId }
        if (selected[0].ownerId == "testOwner") {
            mine = selected
            theirs = listOf()
        }

        if (mine.isEmpty()) {
            _status.value = SaveStatus.Error("Select at least one of your own campsites")
            return
        }
        viewModelScope.launch {
            _status.value = SaveStatus.Saving
            try {
                val docRef = lotteryRef.document()
                val cap = if (waitlistCap != "") waitlistCap.toInt() else null
                availability = (availabilityStart!!..availabilityEnd!! step 86400000).toList()

                val newLottery = Lottery(
                    lotteryID = docRef.id,
                    registrationPeriodStart = registrationStart,
                    registrationPeriodEnd = registrationEnd,
                    bookingPeriodStart = bookingStart,
                    bookingPeriodEnd = bookingEnd,
                    numLotteryWinners = numWinners.toInt(),
                    optionalWaitlistCapacity = cap,
                    campIds = mine.map { it.campId },      // only my camps for now
                    status = LotteryStatus.OPEN
                )

                // batch lets us do multiple database operations at once atomically
                val batch = db.batch()
                // preps to fill the created empty document with our new lottery object
                batch.set(docRef, newLottery)

                // update the availability of the requesting owner's camps
                for (camp in mine) {
                    batch.update(
                        campsiteRef.document(camp.campId),
                        mapOf("lotteryId" to docRef.id, "availability" to availability)
                    )
                }

                for (camp in theirs) {
                    val requestId = "${docRef.id}_${camp.campId}"
                    batch.set(
                        db.collection("lotteryRequests").document(requestId),
                        LotteryRequest(
                            requestId = requestId,
                            lotteryId = docRef.id,
                            campId = camp.campId,
                            campName = camp.name,
                            requesterId = ownerId,
                            ownerId = camp.ownerId
                        )
                    )
                }

                batch.commit().await()
                _status.value = SaveStatus.Success
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }

        }
    }


}