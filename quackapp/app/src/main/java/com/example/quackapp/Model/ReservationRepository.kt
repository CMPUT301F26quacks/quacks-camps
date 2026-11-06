package com.example.quackapp.Model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository and Controller for managing Entrant reservations and lottery win offers.
 * Synchronizes with Firebase Cloud Firestore ("reservations" and "lottery_offers" collections),
 * with safe local memory fallback for offline use and unit tests.
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

    init {
        loadInitialMockData()
    }

    /**
     * Resets repository state to the initial mock data set.
     */
    fun resetToMockData() {
        loadInitialMockData()
    }

    private fun loadInitialMockData() {
        val initialReservations = listOf(
            Reservation(
                id = "res-101",
                campsiteId = "site-hinton",
                campsiteName = "Hinton-Grande Cache Cookhouse",
                campsiteLocation = "Hinton / Grande Cache, AB",
                entrantDeviceId = "default_device",
                entrantName = "Default Entrant",
                dateRange = "May 7 - May 9, 2027",
                numGuests = 4,
                totalPrice = 130.0,
                status = ReservationStatus.CONFIRMED
            )
        )

        val banffSite = CampsiteSummary(
            campsiteId = "site-banff-two-jack",
            name = "Two Jack Lakeside Campground",
            location = "Banff National Park, AB",
            pricePerNight = 65.0,
            maxOccupancy = 6,
            description = "Lakeside camping with scenic views, fire pits, and hiking trails."
        )

        val jasperWapitiSite = CampsiteSummary(
            campsiteId = "site-jasper-wapiti",
            name = "Wapiti Campground",
            location = "Jasper National Park, AB",
            pricePerNight = 58.0,
            maxOccupancy = 6,
            description = "Year-round forested campground along the Athabasca River."
        )

        val jasperWhistlersSite = CampsiteSummary(
            campsiteId = "site-jasper-whistlers",
            name = "Whistlers Campground",
            location = "Jasper National Park, AB",
            pricePerNight = 62.0,
            maxOccupancy = 8,
            description = "Recently updated campsites with modern bike trails and campfire pits."
        )

        val initialOffers = listOf(
            LotteryOffer(
                id = "offer-banff-01",
                lotteryTitle = "Banff Victoria Day Long Weekend Draw",
                entrantDeviceId = "default_device",
                campsites = listOf(banffSite),
                availableDateRanges = listOf(
                    "May 14 - May 16, 2027",
                    "May 21 - May 24, 2027 (Long Weekend)",
                    "May 28 - May 30, 2027"
                ),
                status = OfferStatus.PENDING_RESPONSE
            ),
            LotteryOffer(
                id = "offer-jasper-02",
                lotteryTitle = "Jasper Summer Solstice Multi-Site Draw",
                entrantDeviceId = "default_device",
                campsites = listOf(jasperWapitiSite, jasperWhistlersSite),
                availableDateRanges = listOf(
                    "June 4 - June 6, 2027",
                    "June 11 - June 13, 2027",
                    "June 18 - June 20, 2027"
                ),
                status = OfferStatus.PENDING_RESPONSE
            )
        )

        _reservations.value = initialReservations
        _offers.value = initialOffers
    }

    /**
     * Retrieves all active (confirmed) reservations.
     * US 01.06.01: As an entrant, I want to view a list of my active reservations.
     *
     * @param deviceId The entrant device identifier. If blank, returns all confirmed reservations.
     */
    fun getActiveReservations(deviceId: String = ""): List<Reservation> {
        return _reservations.value.filter { res ->
            res.status == ReservationStatus.CONFIRMED &&
                    (deviceId.isBlank() || res.entrantDeviceId == deviceId || res.entrantDeviceId == "default_device")
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
                    (deviceId.isBlank() || offer.entrantDeviceId == deviceId || offer.entrantDeviceId == "default_device")
        }
    }

    /**
    /**
     * Retrieves past (completed, cancelled, or declined) reservations.
     * US 01.06.02: As an entrant, I want to view a list of past reservations.
     */
    fun getPastReservations(deviceId: String = ""): List<Reservation> {
        return _reservations.value.filter { res ->
            (res.status == ReservationStatus.CANCELLED || res.status == ReservationStatus.DECLINED || res.status == ReservationStatus.COMPLETED) &&
                    (deviceId.isBlank() || res.entrantDeviceId == deviceId || res.entrantDeviceId == "default_device")
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
            entrantDeviceId = if (deviceId.isBlank()) "default_device" else deviceId,
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
     * Synchronizes reservations from Cloud Firestore for the given entrant device.
     * Merges fetched Firestore documents into the local state flow.
     *
     * @param deviceId The entrant device identifier.
     * @param onComplete Callback invoked with refreshed reservations list.
     */
    fun syncFromFirestore(deviceId: String = "", onComplete: ((List<Reservation>) -> Unit)? = null) {
        val db = firestore ?: run {
            onComplete?.invoke(getActiveReservations(deviceId))
            return
        }

        val targetDevice = if (deviceId.isBlank()) "default_device" else deviceId
        db.collection("reservations")
            .whereEqualTo("entrantDeviceId", targetDevice)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
                if (fetched.isNotEmpty()) {
                    val fetchedIds = fetched.map { it.id }.toSet()
                    val remaining = _reservations.value.filter { it.id !in fetchedIds }
                    _reservations.value = fetched + remaining
                }
                onComplete?.invoke(getActiveReservations(deviceId))
            }
            .addOnFailureListener { e ->
                Log.w("ReservationRepo", "Error syncing reservations from Firestore", e)
                onComplete?.invoke(getActiveReservations(deviceId))
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
