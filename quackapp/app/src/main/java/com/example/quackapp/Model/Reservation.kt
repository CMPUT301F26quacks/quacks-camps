package com.example.quackapp.Model

import java.util.UUID

/**
 * Status representation for a campsite reservation.
 */
enum class ReservationStatus {
    /** Reservation is active and confirmed for stay. */
    CONFIRMED,
    /** Reservation is awaiting final confirmation or processing. */
    PENDING,
    /** Entrant declined the reservation opportunity. */
    DECLINED,
    /** Entrant or campsite owner cancelled the reservation. */
    CANCELLED
}

/**
 * Model class representing a confirmed campsite reservation made by an entrant.
 * Satisfies requirements for US 01.05.01 (confirmation) and US 01.06.01 (active reservation list).
 *
 * @property id Unique identifier for the reservation.
 * @property campsiteId Unique identifier of the campsite.
 * @property campsiteName Human-readable name of the campsite (e.g. "Two Jack Lakeside").
 * @property campsiteLocation Region or park where the campsite is located (e.g. "Banff National Park").
 * @property entrantDeviceId Unique device ID of the entrant holding this reservation.
 * @property entrantName Name of the entrant who booked the stay.
 * @property dateRange Date range string for the stay (e.g. "May 14 - May 16, 2027").
 * @property numGuests Number of guests included in the reservation.
 * @property totalPrice Total booking cost in CAD.
 * @property status Current status of the reservation.
 * @property createdAt Epoch millisecond timestamp of reservation creation.
 */
data class Reservation(
    val id: String = UUID.randomUUID().toString(),
    val campsiteId: String,
    val campsiteName: String,
    val campsiteLocation: String = "",
    val entrantDeviceId: String,
    val entrantName: String = "",
    val dateRange: String,
    val numGuests: Int = 1,
    val totalPrice: Double = 0.0,
    val status: ReservationStatus = ReservationStatus.CONFIRMED,
    val createdAt: Long = System.currentTimeMillis()
)
