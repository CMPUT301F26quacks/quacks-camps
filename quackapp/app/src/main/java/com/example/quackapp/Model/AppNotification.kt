package com.example.quackapp.Model

import java.util.UUID

/**
 * Categorization of system and entrant notifications.
 * Implements requirements for:
 * - US 01.03.01: Lottery win notification.
 * - US 01.03.02: Lottery loss notification.
 * - US 01.05.02: Reservation confirmation notification.
 */
enum class NotificationType {
    /** Winner notification inviting entrant to select dates and book. */
    LOTTERY_WIN,
    /** Notification informing entrant they were not drawn for a lottery. */
    LOTTERY_LOSS,
    /** Confirmation alert with reservation details. */
    RESERVATION_CONFIRMATION,
    /** Notification confirming cancellation of a reservation. */
    RESERVATION_CANCELLATION,
    /** General system broadcast or owner update. */
    GENERAL
}

/**
 * Model class representing an in-app or system notification delivered to an entrant.
 * Follows the Notification CRC card design: encapsulates payload, type, timestamp,
 * and maintains links to related offers or reservations.
 *
 * @property id Unique notification identifier.
 * @property recipientDeviceId Device identifier of the intended recipient.
 * @property title Concise summary of the notification alert.
 * @property message Detailed description of the notification event.
 * @property type Category of notification (win, loss, confirmation, cancellation).
 * @property timestamp Epoch millisecond timestamp of notification dispatch.
 * @property isRead Whether the notification has been viewed by the entrant.
 * @property relatedOfferId Optional ID of the associated [LotteryOffer], if applicable.
 * @property relatedReservationId Optional ID of the associated [Reservation], if applicable.
 */
data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val recipientDeviceId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedOfferId: String? = null,
    val relatedReservationId: String? = null
)
