package com.example.quackapp

import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationRepository
import com.example.quackapp.Model.ReservationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for owner reservation rejection.
 * User Story: As a campsite owner, I want to reject a reservation.
 */
class RejectReservationTest {

    //owner can reject a confirmed reservation and cannot reject it twice
    @Test
    fun rejectReservationMarksConfirmedAsDeclined() {
        val repo = ReservationRepository.getInstance()
        val res = Reservation(
            id = "res-1",
            campsiteId = "camp-1",
            entrantDeviceId = "dev-1",
            status = ReservationStatus.CONFIRMED
        )
        repo.setLocalData(listOf(res), emptyList())

        assertTrue(repo.rejectReservation("res-1"))
        assertEquals(ReservationStatus.DECLINED, repo.reservations.value.first().status)

        assertFalse(repo.rejectReservation("res-1"))
        repo.clear()
    }

    //rejecting a non-existent reservation ID fails without changing existing reservations
    @Test
    fun rejectReservationFailsWhenIdDoesNotExist() {
        val repo = ReservationRepository.getInstance()
        val res = Reservation(
            id = "res-1",
            campsiteId = "camp-1",
            entrantDeviceId = "dev-1",
            status = ReservationStatus.CONFIRMED
        )
        repo.setLocalData(listOf(res), emptyList())

        assertFalse(repo.rejectReservation("wrong-id"))
        assertEquals(ReservationStatus.CONFIRMED, repo.reservations.value.first().status)

        repo.clear()
    }
}