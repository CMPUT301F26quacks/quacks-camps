package com.example.quackapp.Model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Repository and Controller for managing Entrant reservations and lottery win offers.
 * Synchronizes live with Firebase Cloud Firestore ("reservations" and "lottery_offers" collections).
 * Holds no data until [syncFromFirestore] runs; unit tests load data with [setLocalData].
 *
 * Outstanding: lottery offers are created by the lottery draw (US 02.05.01, not on dev yet).
 * Date ranges are stored as display strings, so overlap checks are not possible yet.
 */
class ReservationRepository private constructor() {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _offers = MutableStateFlow<List<LotteryOffer>>(emptyList())
    val offers: StateFlow<List<LotteryOffer>> = _offers.asStateFlow()

    /**
     * Lazily obtains FirebaseFirestore instance, safely falling back to null in JVM unit tests
     * or environments where FirebaseApp is not initialized.
     */
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }

    // Live Firestore listeners for the signed-in entrant; replaced on each sync
    private var reservationListener: ListenerRegistration? = null
    private var offerListener: ListenerRegistration? = null

    /**
     * Replaces the locally held reservations and offers.
     * Used by unit tests to load test data without Firestore.
     *
     * @param reservations reservations to hold.
     * @param offers lottery offers to hold.
     */
    fun setLocalData(reservations: List<Reservation>, offers: List<LotteryOffer>) {
        _reservations.value = reservations
        _offers.value = offers
    }

    /**
     * Removes all locally held reservations and offers.
     */
    fun clear() {
        setLocalData(emptyList(), emptyList())
    }

    /**
     * Retrieves all active (confirmed) reservations.
     * US 01.06.01: As an entrant, I want to view a list of my active reservations.
     *
     * Sorted so the soonest stay is first.
     *
     * @param deviceId The entrant device identifier. If blank, returns all confirmed reservations.
     */
    fun getActiveReservations(deviceId: String = ""): List<Reservation> {
        val active = _reservations.value.filter { res ->
            res.status == ReservationStatus.CONFIRMED &&
                    (deviceId.isBlank() || res.entrantDeviceId == deviceId)
        }
        return active.sortedBy { startDateOf(it.dateRange) }
    }


    /**
     * Gets the first date out of a date range like "Jan 1, 2026 - Jan 5, 2026"
     * (the format LotteryDraw makes with toDisplayDate).
     *
     * @param dateRange the date range string
     * @return the start date in millis, or Long.MAX_VALUE if it cant be read
     */
    private fun startDateOf(dateRange: String): Long {
        val start = dateRange.split(" - ")[0].trim()
        val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        try {
            return format.parse(start)!!.time
        } catch (e: Exception) {
            // cant read the date so just put it at the end
            return Long.MAX_VALUE
        }
    }

    /**
     * Retrieves pending lottery offers awaiting entrant decision.
     * US 01.04.01: As an entrant, if I win the lottery, I want to see available dates that I can reserve.
     *
     * @param deviceId The entrant device identifier. If blank, returns all pending offers.
     */
    fun getPendingOffers(deviceId: String = ""): List<LotteryOffer> {
        return _offers.value.filter { offer ->
            offer.status == OfferStatus.PENDING_RESPONSE &&
                    (deviceId.isBlank() || offer.entrantDeviceId == deviceId)
        }
    }

    /**
     * Retrieves past (completed, cancelled, or declined) reservations.
     * US 01.06.02: As an entrant, I want to view a list of past reservations.
     */
    fun getPastReservations(deviceId: String = ""): List<Reservation> {
        return _reservations.value.filter { res ->
            (res.status == ReservationStatus.CANCELLED || res.status == ReservationStatus.DECLINED || res.status == ReservationStatus.COMPLETED) &&
                    (deviceId.isBlank() || res.entrantDeviceId == deviceId)
        }
    }

    /**
     * Dynamically registers an available date range for a lottery offer (e.g., when selected via calendar).
     * US 01.04.01: Entrant date selection on lottery win.
     */
    fun addAvailableDateRange(offerId: String, dateRange: String) {
        val currentOffers = _offers.value
        val index = currentOffers.indexOfFirst { it.id == offerId }
        if (index != -1) {
            val offer = currentOffers[index]
            if (!offer.availableDateRanges.contains(dateRange)) {
                val updated = offer.copy(availableDateRanges = offer.availableDateRanges + dateRange)
                val list = currentOffers.toMutableList()
                list[index] = updated
                _offers.value = list
            }
        }
    }

    /**
     * Confirms a reservation following a lottery win.
     * US 01.05.01: As an entrant, I want to receive confirmation of my campsite reservation.
     *
     * @param offerId Identifier of the lottery offer being accepted.
     * @param campsiteId Identifier of the chosen campsite.
     * @param chosenDate Selected date range.
     * @param numGuests Number of guests.
     * @param deviceId Entrant's device identifier.
     * @param entrantName Optional entrant name.
     * @return Newly created confirmed [Reservation], or null if offer is invalid/expired.
     */
    fun confirmReservation(
        offerId: String,
        campsiteId: String,
        chosenDate: String,
        numGuests: Int,
        deviceId: String,
        entrantName: String = ""
    ): Reservation? {
        val currentOffers = _offers.value
        val offerIndex = currentOffers.indexOfFirst { it.id == offerId && it.status == OfferStatus.PENDING_RESPONSE }
        if (offerIndex == -1) return null

        val offer = currentOffers[offerIndex]
        val campsite = offer.campsites.firstOrNull { it.campsiteId == campsiteId } ?: return null
        if (!offer.availableDateRanges.contains(chosenDate)) return null

        // Mark offer accepted
        val updatedOffer = offer.copy(status = OfferStatus.ACCEPTED)
        val updatedOffers = currentOffers.toMutableList()
        updatedOffers[offerIndex] = updatedOffer
        _offers.value = updatedOffers

        // Create confirmed reservation
        val reservation = Reservation(
            campsiteId = campsite.campsiteId,
            campsiteName = campsite.name,
            campsiteLocation = campsite.location,
            entrantDeviceId = deviceId,
            entrantName = entrantName.ifBlank { "Entrant (${deviceId.take(6)})" },
            dateRange = chosenDate,
            numGuests = numGuests.coerceIn(1, campsite.maxOccupancy),
            totalPrice = campsite.pricePerNight * 2, // assume 2-night weekend stay
            status = ReservationStatus.CONFIRMED
        )

        _reservations.value = listOf(reservation) + _reservations.value

        // Sync confirmed reservation to Firestore
        firestore?.let { db ->
            try {
                db.collection("reservations")
                    .document(reservation.id)
                    .set(reservation)
                    .addOnFailureListener { e ->
                        Log.w("ReservationRepo", "Failed to sync reservation to Firestore", e)
                    }

                db.collection("lottery_offers")
                    .document(offerId)
                    .update("status", OfferStatus.ACCEPTED.name)
            } catch (e: Throwable) {
                Log.w("ReservationRepo", "Firestore confirmReservation failed", e)
            }
        }

        return reservation
    }

    /**
     * Declines a reservation offer following a lottery win.
     * US 01.04.03: As an entrant, if I win the lottery, I want to decline a reservation.
     *
     * @param offerId Identifier of the offer to decline.
     * @return True if offer was found and successfully declined; false otherwise.
     */
    fun declineOffer(offerId: String): Boolean {
        val currentOffers = _offers.value
        val offerIndex = currentOffers.indexOfFirst { it.id == offerId && it.status == OfferStatus.PENDING_RESPONSE }
        if (offerIndex == -1) return false

        val offer = currentOffers[offerIndex]
        val updatedOffer = offer.copy(status = OfferStatus.DECLINED)
        val updatedOffers = currentOffers.toMutableList()
        updatedOffers[offerIndex] = updatedOffer
        _offers.value = updatedOffers

        // Sync decline to Firestore
        firestore?.let { db ->
            try {
                db.collection("lottery_offers")
                    .document(offerId)
                    .update("status", OfferStatus.DECLINED.name)
            } catch (e: Throwable) {
                Log.w("ReservationRepo", "Firestore declineOffer failed", e)
            }
        }

        return true
    }

    /**
     * Cancels an existing reservation.
     *
     * @param reservationId Identifier of reservation to cancel.
     * @return True if found and cancelled; false otherwise.
     */
    fun cancelReservation(reservationId: String): Boolean {
        val currentReservations = _reservations.value
        val resIndex = currentReservations.indexOfFirst { it.id == reservationId && it.status == ReservationStatus.CONFIRMED }
        if (resIndex == -1) return false

        val res = currentReservations[resIndex]
        val updatedRes = res.copy(status = ReservationStatus.CANCELLED)
        val updatedList = currentReservations.toMutableList()
        updatedList[resIndex] = updatedRes
        _reservations.value = updatedList

        // Sync cancellation to Firestore
        firestore?.let { db ->
            try {
                db.collection("reservations")
                    .document(reservationId)
                    .update("status", ReservationStatus.CANCELLED.name)
                    .addOnFailureListener { e ->
                        Log.w("ReservationRepo", "Failed to update cancellation in Firestore", e)
                    }
            } catch (e: Throwable) {
                Log.w("ReservationRepo", "Firestore cancelReservation failed", e)
            }
        }

        return true
    }


/**
 * Rejects an existing reservation as a campsite owner.
 * Starts live Firestore listeners for this entrant's reservations ("reservations")
 * and lottery win offers ("lottery_offers"), replacing any previous listeners.
 * Local state updates automatically whenever Firestore changes (real-time status updates).
 *
 * @param reservationId Identifier of reservation to reject.
 * @return True if found and rejected; false otherwise.
 **/
    fun rejectReservation(reservationId: String): Boolean {
        val currentReservations = _reservations.value
        val resIndex = currentReservations.indexOfFirst { it.id == reservationId && it.status == ReservationStatus.CONFIRMED }
        if (resIndex != -1) {
            val res = currentReservations[resIndex]
            val updatedRes = res.copy(status = ReservationStatus.DECLINED)
            val updatedList = currentReservations.toMutableList()
            updatedList[resIndex] = updatedRes
            _reservations.value = updatedList
        } else if (firestore == null) {
            return false
        }

        firestore?.let { db ->
            try {
                db.collection("reservations")
                    .document(reservationId)
                    .update("status", ReservationStatus.DECLINED.name)
                    .addOnFailureListener { e ->
                        Log.w("ReservationRepo", "Failed to update rejection in Firestore", e)
                    }
            } catch (e: Throwable) {
                Log.w("ReservationRepo", "Firestore rejectReservation failed", e)
            }
        }

        return true
    }



    /**
     * Starts live Firestore listeners for this entrant's reservations ("reservations")
     * and lottery win offers ("lottery_offers"), replacing any previous listeners.
     * Local state updates automatically whenever Firestore changes (real-time status updates).
     *
     * @param deviceId The signed-in entrant's device identifier.
     * @param onComplete Optional callback invoked with active reservations after the first load.
     */
    fun syncFromFirestore(deviceId: String, onComplete: ((List<Reservation>) -> Unit)? = null) {
        val db = firestore
        if (db == null || deviceId.isBlank()) {
            onComplete?.invoke(getActiveReservations(deviceId))
            return
        }

        reservationListener?.remove()
        offerListener?.remove()

        var completed = false
        reservationListener = db.collection("reservations")
            .whereEqualTo("entrantDeviceId", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ReservationRepo", "Error syncing reservations from Firestore", error)
                } else if (snapshot != null) {
                    val fetched = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
                    // keep reservations of other users (e.g. loaded for an owner's campsite)
                    _reservations.value = fetched + _reservations.value.filter { it.entrantDeviceId != deviceId }
                }
                if (!completed) {
                    completed = true
                    onComplete?.invoke(getActiveReservations(deviceId))
                }
            }

        offerListener = db.collection("lottery_offers")
            .whereEqualTo("entrantDeviceId", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ReservationRepo", "Error syncing lottery offers from Firestore", error)
                    return@addSnapshotListener
                }
                val fetched = snapshot?.documents?.mapNotNull { it.toObject(LotteryOffer::class.java) } ?: return@addSnapshotListener
                _offers.value = fetched + _offers.value.filter { it.entrantDeviceId != deviceId }
            }
    }


     /**
      *  Synchronizes all reservations from Cloud Firestore for a specific campsite.
     * Merges fetched Firestore documents into the local StateFlow regardless of reservation status.
     *
     * @param campsiteId The campsite identifier.
     * @param onResult Callback invoked with all reservations for this campsite.
     */
    fun syncReservationsForCampsite(
        campsiteId: String,
        onResult: ((List<Reservation>) -> Unit)? = null
    ) {
        val localMatches = _reservations.value.filter { it.campsiteId == campsiteId }

        val db = firestore ?: run {
            onResult?.invoke(localMatches)
            return
        }

        db.collection("reservations")
            .whereEqualTo("campsiteId", campsiteId)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
                if (fetched.isNotEmpty()) {
                    val fetchedIds = fetched.map { it.id }.toSet()
                    val remaining = _reservations.value.filter { it.id !in fetchedIds }
                    _reservations.value = fetched + remaining
                    onResult?.invoke(_reservations.value.filter { it.campsiteId == campsiteId })
                } else {
                    onResult?.invoke(localMatches)
                }
            }
            .addOnFailureListener { e ->
                Log.w("ReservationRepo", "Error fetching campsite reservations", e)
                onResult?.invoke(localMatches)
            }
    }

    /**
     * Fetches confirmed reservations for a campsite from Firestore ("reservations"),
     * Local state (_reservations) is updated with any newly fetched records.
     *
     * @param campsiteId The unique identifier of the campsite.
     * @param onResult Callback invoked with the matching list of confirmed [Reservation]s.
     */
    fun getConfirmedReservations(
        campsiteId: String,
        onResult: (List<Reservation>) -> Unit
    ){
        val localMatches = _reservations.value.filter {
            it.campsiteId == campsiteId && it.status == ReservationStatus.CONFIRMED
        }
        val db = firestore ?: run {
            onResult(localMatches)
            return
        }
        db.collection("reservations")
            .whereEqualTo("campsiteId", campsiteId)
            .whereEqualTo("status", ReservationStatus.CONFIRMED.name)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
                if (fetched.isNotEmpty()) {
                    val fetchedIds = fetched.map { it.id }.toSet()
                    val remaining = _reservations.value.filter { it.id !in fetchedIds }
                    _reservations.value = fetched + remaining
                    onResult(fetched)
                } else {
                    onResult(localMatches)
                }
            }
            .addOnFailureListener { e ->
                Log.w("ReservationRepo", "Error fetching campsite reservations", e)
                onResult(localMatches)
            }
    }

    companion object {
        @Volatile
        private var instance: ReservationRepository? = null

        /**
         * Singleton accessor for [ReservationRepository].
         */
        fun getInstance(): ReservationRepository {
            return instance ?: synchronized(this) {
                instance ?: ReservationRepository().also { instance = it }
            }
        }
    }
}
