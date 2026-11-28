package com.example.quackapp

import com.example.quackapp.Model.Entrant
import com.example.quackapp.Model.OfferStatus
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationRepository
import com.example.quackapp.Model.ReservationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ReservationRepository] and Entrant reservation logic.
 * Validates requirements for:
 * - US 01.04.01: Entrant views available dates upon winning lottery.
 * - US 01.04.03: Entrant declines reservation offer.
 * - US 01.05.01: Entrant receives reservation confirmation.
 * - US 01.06.01: Entrant views active reservations.
 */
class ReservationRepositoryTest {

    private lateinit var repository: ReservationRepository

    @Before
    fun setUp() {
        repository = ReservationRepository.getInstance()
        repository.setLocalData(ReservationTestData.reservations, ReservationTestData.offers)
    }

    @Test
    fun testTestDataLoadsActiveReservationsAndOffers() {
        val activeReservations = repository.getActiveReservations()
        val pendingOffers = repository.getPendingOffers()

        assertTrue("Initial active reservations should not be empty", activeReservations.isNotEmpty())
        assertTrue("Initial pending offers should not be empty", pendingOffers.isNotEmpty())
        assertEquals("res-101", activeReservations.first().id)
        assertEquals(ReservationStatus.CONFIRMED, activeReservations.first().status)
    }

    /**
     * US 01.04.01: As an entrant, if I win the lottery, I want to see available dates that I can reserve the campsite for.
     */
    @Test
    fun testAvailableDatesVisibleForLotteryWin() {
        val pendingOffers = repository.getPendingOffers()
        val offer = pendingOffers.firstOrNull { it.id == "offer-banff-01" }

        assertNotNull("Offer for Banff should exist", offer)
        assertTrue("Offer should contain available date ranges", offer!!.availableDateRanges.isNotEmpty())
        assertTrue(
            "Offer should list May 21 weekend",
            offer.availableDateRanges.any { it.contains("May 21") }
        )
    }

    /**
     * US 01.04.03: As an entrant, if I win the lottery, I want to decline a reservation if I changed my mind.
     */
    @Test
    fun testDeclineLotteryOffer() {
        val initialPendingCount = repository.getPendingOffers().size
        val offerToDecline = repository.getPendingOffers().first()

        val success = repository.declineOffer(offerToDecline.id)
        assertTrue("Decline should return true for pending offer", success)

        val updatedOffers = repository.getPendingOffers()
        assertEquals("Pending offers count should decrease by 1", initialPendingCount - 1, updatedOffers.size)
        assertFalse("Declined offer should no longer be pending", updatedOffers.any { it.id == offerToDecline.id })

        val allOffers = repository.offers.value
        val declinedOffer = allOffers.first { it.id == offerToDecline.id }
        assertEquals(OfferStatus.DECLINED, declinedOffer.status)
    }

    /**
     * US 01.05.01 & US 01.06.01: Confirming reservation generates a confirmed booking visible in active reservations.
     */
    @Test
    fun testConfirmReservationSuccess() {
        val offer = repository.getPendingOffers().first { it.id == "offer-banff-01" }
        val campsite = offer.campsites.first()
        val chosenDate = offer.availableDateRanges.first()

        val initialActiveCount = repository.getActiveReservations().size

        val confirmedReservation = repository.confirmReservation(
            offerId = offer.id,
            campsiteId = campsite.campsiteId,
            chosenDate = chosenDate,
            numGuests = 3,
            deviceId = "test-device-123",
            entrantName = "Alex Entrant"
        )

        assertNotNull("Reservation should be successfully created", confirmedReservation)
        assertEquals(ReservationStatus.CONFIRMED, confirmedReservation!!.status)
        assertEquals(campsite.name, confirmedReservation.campsiteName)
        assertEquals(chosenDate, confirmedReservation.dateRange)
        assertEquals(3, confirmedReservation.numGuests)
        assertEquals(130.0, confirmedReservation.totalPrice, 0.01)

        // Verify offer status updated to ACCEPTED
        val allOffers = repository.offers.value
        assertEquals(OfferStatus.ACCEPTED, allOffers.first { it.id == offer.id }.status)

        // Verify reservation is in active reservations
        val activeReservations = repository.getActiveReservations()
        assertEquals(initialActiveCount + 1, activeReservations.size)
        assertTrue(activeReservations.any { it.id == confirmedReservation.id })
    }

    @Test
    fun testConfirmReservationWithInvalidDateFails() {
        val offer = repository.getPendingOffers().first { it.id == "offer-banff-01" }
        val campsite = offer.campsites.first()

        val result = repository.confirmReservation(
            offerId = offer.id,
            campsiteId = campsite.campsiteId,
            chosenDate = "Invalid Date 2099",
            numGuests = 2,
            deviceId = "test-device"
        )

        assertNull("Confirming with non-existent date range should fail", result)
    }

    /**
     * US 01.06.01: Entrant class helper methods correctly query active reservations.
     */
    @Test
    fun testEntrantModelIntegration() {
        val entrant = Entrant(
            deviceId = ReservationTestData.DEVICE,
            name = "Test Entrant",
            email = "entrant@ualberta.ca",
            phoneNumber = "780-555-0100"
        )

        val activeReservations = entrant.getActiveReservations(repository)
        assertTrue("Entrant should have active reservations", activeReservations.isNotEmpty())

        val pendingOffers = entrant.getPendingOffers(repository)
        assertTrue("Entrant should have pending offers", pendingOffers.isNotEmpty())
    }

    @Test
    fun testCancelReservation() {
        val initialActiveCount = repository.getActiveReservations().size
        val activeReservation = repository.getActiveReservations().first()

        val success = repository.cancelReservation(activeReservation.id)
        assertTrue("Cancel should succeed for confirmed reservation", success)

        val updatedActive = repository.getActiveReservations()
        assertEquals(initialActiveCount - 1, updatedActive.size)
        assertFalse(updatedActive.any { it.id == activeReservation.id })
    }

    /**
     * US 01.04.01: Entrant can add custom calendar date range and confirm reservation.
     */
    @Test
    fun testAddAvailableDateRangeAndConfirm() {
        val offer = repository.getPendingOffers().first { it.id == "offer-banff-01" }
        val customRange = "Jul 10, 2027 - Jul 14, 2027"

        repository.addAvailableDateRange(offer.id, customRange)
        val updatedOffer = repository.getPendingOffers().first { it.id == offer.id }
        assertTrue("Updated offer should contain custom date range", updatedOffer.availableDateRanges.contains(customRange))

        val reservation = repository.confirmReservation(
            offerId = offer.id,
            campsiteId = offer.campsites.first().campsiteId,
            chosenDate = customRange,
            numGuests = 2,
            deviceId = "test-custom-date-device"
        )

        assertNotNull("Reservation with custom calendar dates should succeed", reservation)
        assertEquals(customRange, reservation?.dateRange)
    }

    /**
     * US 01.06.01: An entrant only sees their own reservations and offers, never another user's.
     */
    @Test
    fun testOtherEntrantSeesNoReservationsOrOffers() {
        assertTrue(repository.getActiveReservations("another_device").isEmpty())
        assertTrue(repository.getPendingOffers("another_device").isEmpty())
    }

    @Test
    fun testSyncFromFirestoreSafeFallback() {
        var callbackInvoked = false
        repository.syncFromFirestore("test-device") { resList ->
            callbackInvoked = true
            assertNotNull(resList)
        }
        assertTrue("Firestore sync callback should complete gracefully in test mode", callbackInvoked)
    }

    /**
     * US 01.06.01: active reservations show the soonest stay first,
     * and ones with a date we cant read go at the end.
     */
    @Test
    fun testActiveReservationsSortedByDate() {
        val august = Reservation(id = "aug", dateRange = "Aug 3, 2027 - Aug 5, 2027")
        val badDate = Reservation(id = "bad", dateRange = "sometime")
        val june = Reservation(id = "june", dateRange = "Jun 10, 2027 - Jun 12, 2027")
        val decLastYear = Reservation(id = "dec", dateRange = "Dec 20, 2026 - Dec 22, 2026")
        repository.setLocalData(listOf(august, badDate, june, decLastYear), emptyList())

        val active = repository.getActiveReservations()

        assertEquals(4, active.size)
        assertEquals("dec", active[0].id)
        assertEquals("june", active[1].id)
        assertEquals("aug", active[2].id)
        assertEquals("bad", active[3].id)
    }
}
