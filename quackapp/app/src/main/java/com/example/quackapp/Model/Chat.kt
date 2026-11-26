package com.example.quackapp.Model

import java.sql.Time
import java.sql.Timestamp

/**
 * Chat between a campsite owner and an entrant regarding a
 * campsite reservation.
 *
 * Reservation ID is used to identify the chat, so only one chat exists
 * per reservation relationship.
 *
 * @property chatId Unique ID for the chat
 * @property reservationId Reservation associated with the chat
 * @property campsiteId Campsite associated with the chat
 * @property entrantId Device ID of entrant associated with the chat
 * @property ownerId Device ID of owner associated with the chat
 * @property participantIds List of device IDs of both participants
 * @property lastMessage Preview of the most recent message for chat summary
 * @property lastMessageTimestamp Time of most recent message
 * @property created Time the chat was created
 */
class Chat(
    var chatId: String = "",
    var reservationId: String = "",
    var campsiteId: String = "",
    var entrantId: String = "",
    var ownerId: String = "",
    var participantIds: List<String> = emptyList(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Timestamp? = null,
    var created: Timestamp? = null
) {
}