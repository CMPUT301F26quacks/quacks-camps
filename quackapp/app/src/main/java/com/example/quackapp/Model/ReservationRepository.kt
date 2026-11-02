package com.example.quackapp.Model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository and Controller for managing Entrant reservations and lottery win offers.
 * Implements in-memory state with realistic default data for local execution and testing,
 * architected for future Firestore collection synchronization.
 */
class ReservationRepository private constructor() {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _offers = MutableStateFlow<List<LotteryOffer>>(emptyList())
    val offers: StateFlow<List<LotteryOffer>> = _offers.asStateFlow()

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
        return true
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
