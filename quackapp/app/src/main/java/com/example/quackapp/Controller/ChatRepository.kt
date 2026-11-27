package com.example.quackapp.Controller

import com.example.quackapp.Model.Admin
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Chat
import com.example.quackapp.Model.Message
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlin.collections.emptyList

/**
 * Repository responsible for creating, retrieving and managing
 * chats and messages in Firebase.
 *
 * Each chat is associated with a campsite reservation, allowing for
 * messaging between the campsite owner and entrant.
 *
 * Supports US 01.14.01, 02.12.01, and 03.12.01
 */
object ChatRepository {

    /**
     * Retrieves an existing chat or creates a new one for a confirmed reservation.
     *
     * @param reservationId ID of the associated reservation
     * @param deviceId Device ID of the user requesting the chat
     * @param onSuccess Called with the retrieved/created chat
     * @param onFail Called if chat retrieval fails
     */
    fun getOrCreateChat(
        reservationId: String,
        deviceId: String,
        onSuccess: (Chat) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val database = FirebaseFirestore.getInstance()

        val chatRef = database.collection("chats")
            .document(reservationId)

        val reservationRef = database.collection("reservationns")
            .document(reservationId)

        // firebase transaction allows firebase to retry an operation if the document is changing
        // while being worked on, so chats don't override each other
        database.runTransaction { transaction ->
            // check whether a chat already exists
            val chatDocument = transaction.get(chatRef)

            if (chatDocument.exists()) {

                val chat = chatDocument.toObject(Chat::class.java)
                    ?: throw Exception("Unable to load chat")

                if (deviceId !in chat.participantIds) {
                    throw Exception("You are not a participant in this chat")
                }

                return@runTransaction chat
            }

            // retrieves the reservation associated with the chat
            val reservation = transaction.get(reservationRef)
                .toObject(Reservation::class.java)
                ?: throw Exception("Reservation not found")

            if (reservation.status != ReservationStatus.CONFIRMED) {
                throw Exception("Reservation must be confirmed")
            }

            val campsiteRef = database.collection("campsites")
                .document(reservation.campsiteId)

            val campsite = transaction.get(campsiteRef)
                .toObject(Campsite::class.java)
                ?: throw Exception("Campsite not found")

            val entrantId = reservation.entrantDeviceId
            val ownerId = campsite.ownerId

            val participants = listOf(entrantId, ownerId)

            // only reservation participants can create the chat
            if (deviceId !in participants) {
                throw Exception("You are not a participant in this reservation")
            }

            val chat = Chat(
                chatId = reservationId,
                reservationId = reservationId,
                campsiteId = reservation.campsiteId,
                entrantId = entrantId,
                ownerId = ownerId,
                participantIds = participants
            )

            transaction.set(
                chatRef,
                mapOf(
                    "chatId" to chat.chatId,
                    "reservationId" to chat.reservationId,
                    "campsiteId" to chat.campsiteId,
                    "entrantId" to chat.entrantId,
                    "ownerId" to chat.ownerId,
                    "participantIds" to chat.participantIds,
                    "lastMessage" to "",
                    "lastMessageTimestamp" to null,
                    "created" to FieldValue.serverTimestamp()
                )
            )

            chat

        }
            .addOnSuccessListener { chat -> onSuccess(chat) }
            .addOnFailureListener { exception -> onFail(exception)}
    }


    /**
     * Listens for changes in conversations for a specific user, to display a
     * chat summary and sort chats by recent activity.
     *
     * @return Returns to stop listener when screen is closed.
     */
    fun listenForChats(
        deviceId: String,
        onResult: (List<Chat>) -> Unit,
        onFail: (Exception) -> Unit
    ): ListenerRegistration {

        val database = FirebaseFirestore.getInstance()

        return database.collection("chats")
            .whereArrayContains("participantIds", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFail(error)
                    return@addSnapshotListener
                }

                val chats = snapshot
                    ?.toObjects(Chat::class.java)
                    ?: emptyList()

                onResult(
                    chats.sortedByDescending {
                        it.lastMessageTimestamp ?: it.created
                    }
                )
            }

    }

    /**
     * Verifies that the user's profile has the admin role.
     */
    private fun checkAdmin(
        deviceId: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
        UserRepository.getUser(
            deviceId = deviceId,

            onResult = { user ->
                if (user != null && Admin.isAdmin(user.roles)) {
                    onSuccess()
                } else {
                onFail(Exception("Admin access required"))
                }
            },

        onFail = onFail

        )
    }

    /**
     *
     */
    fun listenForMessages(
        chatId: String,
        deviceId: String,
        onResult: (List<Message>) -> Unit,
        onListening: (ListenerRegistration) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val database = FirebaseFirestore.getInstance()

        val chatRef = database.collection("chats")
            .document(chatId)

        // checks user's role access and then starts listening
        fun startListener() {

            val listener = chatRef.collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        onFail(error)
                        return@addSnapshotListener
                    }

                    val messages = snapshot
                        ?.toObjects(Message::class.java)
                        ?: emptyList()

                    onResult(messages)
                }

            onListening(listener)
        }

        chatRef.get(Source.SERVER)
            .addOnSuccessListener { document ->

                val chat = document.toObject(Chat::class.java)

                if (chat == null) {
                    onFail(Exception("Chat not found"))
                } else if (deviceId in chat.participantIds) {
                    startListener()
                } else {
                    checkAdmin(
                        deviceId = deviceId,
                        onSuccess = { startListener() },
                        onFail = onFail
                    )
                }
            }
            .addOnFailureListener { exception -> onFail(exception) }
    }

    /**
     * Sends a text message within a chat and updates its summary.
     *
     * Only participants associated with a confirmed reservation may send messages.
     * The recipient of the message is determined based on the chat participants' device IDs.
     *
     * @param chatId Conversation receiving the message
     * @param senderId Device ID of message sender
     * @param message Text content being sent
     * @param onSuccess Called after message is successfully sent
     * @param onFail Called is message can't be sent
     */
    fun sendMessage(
        chatId: String,
        senderId: String,
        message: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val messageText = message.trim()

        // prevents blank messages from being sent
        if (messageText.isBlank()) {
            onFail(Exception("Message can't be empty"))
            return
        }

        val database = FirebaseFirestore.getInstance()

        val chatRef = database.collection("chats")
            .document(chatId)

        // firebase automatically creates a unique message ID per message
        val messageRef = chatRef.collection("messages")
            .document()

        database.runTransaction { transaction ->

            val chat = transaction.get(chatRef)
                .toObject(Chat::class.java)
                ?: throw Exception("Chat not found")

            if (senderId !in chat.participantIds) {
                throw Exception("You are not a participant in this chat")
            }

            val reservationRef = database.collection("reservations")
                .document(chat.reservationId)

            val reservation = transaction.get(reservationRef)
                .toObject(Reservation::class.java)
                ?: throw Exception("Reservation not found")

            if (reservation.status != ReservationStatus.CONFIRMED) {
                throw Exception("This conversation is no longer active")
            }

            val campsiteRef = database.collection("campsites")
                .document(reservation.campsiteId)

            val campsite = transaction.get(campsiteRef)
                .toObject(Campsite::class.java)
                ?: throw Exception("Campsite not found")

            if (
                reservation.entrantDeviceId != chat.entrantId || // OR
                campsite.ownerId != chat.ownerId
            ) {
                throw Exception("Chat participants no longer match reservation")
            }

            // determines message recipient
            val recipientId = when (senderId) {
                chat.entrantId -> chat.ownerId
                chat.ownerId -> chat.entrantId
                else -> throw Exception("Invalid sender")
            }

            // stores the individual message
            transaction.set(
                messageRef,
                mapOf(
                    "messageId" to messageRef.id,
                    "chatId" to chatId,
                    "senderId" to senderId,
                    "recipientId" to recipientId,
                    "message" to messageText,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "messageType" to "TEXT",
                    "imageUrl" to ""
                )
            )

            // updates the chat preview shown, based on the most recent message
            transaction.update(
                chatRef,
                mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp()
                )
            )

            Unit

        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            exception -> onFail(exception)
        }
    }

    /**
     * Allows an admin to listen to all chats.
     *
     * Supports US 03.12.01, "As an admin, I want to review logs of all messages
     * from campsite owners to entrants who reserved their campsite"
     */
    fun listenForAllChats(
        deviceId: String,
        onResult:(List<Chat>) -> Unit,
        onListening: (ListenerRegistration) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        checkAdmin(
            deviceId = deviceId,

            onSuccess = {

                val database = FirebaseFirestore.getInstance()

                val listener = database.collection("chats")
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {
                            onFail(error)
                            return@addSnapshotListener
                    }

                        val chats = snapshot
                            ?.toObjects(Chat::class.java)
                            ?: emptyList()

                        onResult(
                            chats.sortedByDescending {
                                it.lastMessageTimestamp ?: it.created
                            }
                        )
                    }

                onListening(listener)

            },

            onFail = onFail
            
        )
    }
}
