package com.example.quackapp.Model

import java.util.UUID

/**
 * Status of a lottery offer extended to an entrant who won a lottery.
 */
enum class OfferStatus {
    /** Waiting for entrant decision (accept or decline). */
    PENDING_RESPONSE,
    /** Entrant accepted and confirmed the reservation. */
    ACCEPTED,
    /** Entrant declined the booking opportunity. */
    DECLINED,
    /** Offer expired after deadline passed without response. */
    EXPIRED
}

/**
 * Summary details for a campsite available to book in a lottery win.
 *
 * @property campsiteId Unique identifier of the campsite.
 * @property name Display name of the campsite.
 * @property location Regional park or area (e.g., "Banff National Park").
 * @property pricePerNight Nightly rate in CAD.
 * @property maxOccupancy Maximum number of guests accommodated.
 * @property description Overview of amenities and site features.
 */
data class CampsiteSummary(
    val campsiteId: String,
    val name: String,
    val location: String,
    val pricePerNight: Double,
    val maxOccupancy: Int,
    val description: String = ""
)

/**
 * Model representing an invitation/offer to reserve following a lottery win.
 * Satisfies:
 * - US 01.04.01: Entrant sees available dates to reserve upon winning a lottery.
 * - US 01.04.02: Multi-campsite lottery selection choice.
 * - US 01.04.03: Entrant can decline a reservation offer.
 *
 * @property id Unique identifier for this offer.
 * @property lotteryTitle Title of the lottery draw (e.g. "Banff Long Weekend Draw").
 * @property entrantDeviceId Device ID of the entrant receiving this offer.
 * @property campsites List of campsites available in this lottery offer.
 * @property availableDateRanges List of available dates/weekends to book.
 * @property status Current response status of this offer.
 * @property expiresAt Epoch timestamp when this offer expires.
 */
data class LotteryOffer(
    val id: String = UUID.randomUUID().toString(),
    val lotteryTitle: String,
    val entrantDeviceId: String,
    val campsites: List<CampsiteSummary>,
    val availableDateRanges: List<String>,
    val status: OfferStatus = OfferStatus.PENDING_RESPONSE,
    val expiresAt: Long = System.currentTimeMillis() + (86400000L * 3) // 3 days
)
