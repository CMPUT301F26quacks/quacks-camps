package com.example.quackapp

import com.example.quackapp.Model.CampsiteSummary
import com.example.quackapp.Model.LotteryOffer
import com.example.quackapp.Model.OfferStatus
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationStatus

/**
 * Realistic test data for the reservation unit tests: one confirmed reservation,
 * a single-campsite lottery offer and a multi-campsite lottery offer for [DEVICE].
 * Loaded into the repository with ReservationRepository.setLocalData.
 */
object ReservationTestData {

    const val DEVICE = "test_entrant_device"

    val reservations = listOf(
        Reservation(
            id = "res-101",
            campsiteId = "site-hinton",
            campsiteName = "Hinton-Grande Cache Cookhouse",
            campsiteLocation = "Hinton / Grande Cache, AB",
            entrantDeviceId = DEVICE,
            entrantName = "Test Entrant",
            dateRange = "May 7 - May 9, 2027",
            numGuests = 4,
            totalPrice = 130.0,
            status = ReservationStatus.CONFIRMED
        )
    )

    private val banffSite = CampsiteSummary(
        campsiteId = "site-banff-two-jack",
        name = "Two Jack Lakeside Campground",
        location = "Banff National Park, AB",
        pricePerNight = 65.0,
        maxOccupancy = 6,
        description = "Lakeside camping with scenic views, fire pits, and hiking trails."
    )

    private val jasperWapitiSite = CampsiteSummary(
        campsiteId = "site-jasper-wapiti",
        name = "Wapiti Campground",
        location = "Jasper National Park, AB",
        pricePerNight = 58.0,
        maxOccupancy = 6,
        description = "Year-round forested campground along the Athabasca River."
    )

    private val jasperWhistlersSite = CampsiteSummary(
        campsiteId = "site-jasper-whistlers",
        name = "Whistlers Campground",
        location = "Jasper National Park, AB",
        pricePerNight = 62.0,
        maxOccupancy = 8,
        description = "Recently updated campsites with modern bike trails and campfire pits."
    )

    val offers = listOf(
        LotteryOffer(
            id = "offer-banff-01",
            lotteryTitle = "Banff Victoria Day Long Weekend Draw",
            entrantDeviceId = DEVICE,
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
            entrantDeviceId = DEVICE,
            campsites = listOf(jasperWapitiSite, jasperWhistlersSite),
            availableDateRanges = listOf(
                "June 4 - June 6, 2027",
                "June 11 - June 13, 2027",
                "June 18 - June 20, 2027"
            ),
            status = OfferStatus.PENDING_RESPONSE
        )
    )
}
