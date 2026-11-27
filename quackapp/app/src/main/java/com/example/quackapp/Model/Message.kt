package com.example.quackapp.Model

import com.google.firebase.Timestamp


/**
 * Represents an individual message in a chat.
 *
 * Supports messaging between campsite owners and entrants.
 *
 * @property messageId Unique ID for the message
 * @property chatId ID for chat containing the message
 * @property senderId Device ID of user sending the message
 * @property recipientId Device ID of user receiving the message
 * @property message Text of the actual message
 * @property timestamp Time the message was sent
 * @property messageType Type of content being sent
 * @property imageUrl Download URL for an optional image attachment
 */
class Message(
    var messageId: String = "",
    var chatId: String = "",
    var senderId: String = "",
    var recipientId: String = "",
    var message: String = "",
    var timestamp: Timestamp? = null,
    var messageType: String = "TEXT",
    var imageUrl: String = ""
)

