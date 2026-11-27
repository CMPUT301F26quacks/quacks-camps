package com.example.quackapp.Model

import android.util.Log
import coil.request.SuccessResult
import com.example.quackapp.Controller.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.internal.ignoreIoExceptions
import java.util.UUID
import kotlin.math.exp

/**
 * Repository and Controller for managing platform notifications.
 * Synchronizes with Firebase Cloud Firestore ("notifications" collection),
 * Holds no data until [syncFromFirestore] runs; unit tests load data with [setLocalData].
 *
 * Fulfills CRC card specifications:
 * - Encapsulates notification payload (type, message, timestamp, status).
 * - Dispatches lottery win/loss alerts, booking confirmations, and cancellations.
 * - Respects user notification opt-out preferences (US 01.10.01).
 */
class NotificationRepository private constructor() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }

    // Live Firestore listener for the signed-in user's notifications
    private var listener: ListenerRegistration? = null

    /**
     * Replaces the locally held notifications. Used by unit tests to load test data without Firestore.
     *
     * @param notifications notifications to hold.
     */
    fun setLocalData(notifications: List<AppNotification>) {
        _notifications.value = notifications
    }

    /**
     * Sends an in-app notification to the specified entrant device.
     * Respects the opt-out preference (US 01.10.01) two ways: if [userPreferenceEnabled] is false
     * nothing is sent, and before saving to Firestore the recipient's own `notificationsEnabled`
     * field in "users" is checked, so callers (e.g. the lottery draw) don't need to look it up.
     *
     * @param recipientDeviceId Target device identifier.
     * @param title Title of notification.
     * @param message Body text.
     * @param type Notification category.
     * @param relatedOfferId Optional ID of associated lottery offer.
     * @param relatedReservationId Optional ID of associated reservation.
     * @param userPreferenceEnabled Whether user has notifications enabled.
     * @return The [AppNotification] built for sending, or null if [userPreferenceEnabled] is false.
     */
    fun sendNotification(
        recipientDeviceId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedOfferId: String? = null,
        relatedReservationId: String? = null,
        userPreferenceEnabled: Boolean = true
    ): AppNotification? {
        if (!userPreferenceEnabled) {
            // Notification suppressed per user opt-out preference (US 01.10.01)
            return null
        }

        val notification = AppNotification(
            recipientDeviceId = recipientDeviceId,
            title = title,
            message = message,
            type = type,
            relatedOfferId = relatedOfferId,
            relatedReservationId = relatedReservationId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )

        val db = firestore
        if (db == null) {
            // No Firebase (unit tests): keep it locally only
            _notifications.value = listOf(notification) + _notifications.value
            return notification
        }

        // Check the recipient's own opt-out setting (US 01.10.01) before saving.
        // The recipient's live listener then shows it, so it is not added locally here.
        try {
            db.collection("users").document(recipientDeviceId).get()
                .addOnSuccessListener { user ->
                    if (user.getBoolean("notificationsEnabled") == false) return@addOnSuccessListener

                    // reference to the recipient's notification
                    val notificationRef = db.collection("notifications")
                        .document(notification.id)

                    // reference to the notification log for admin control
                    val logRef = db.collection("notification_logs")
                        .document(notification.id)

                    // stores the notification and its log together
                    val batch = db.batch()

                    batch.set(notificationRef, notification)
                    batch.set(logRef, notification)

                    batch.commit()
                        .addOnFailureListener { e ->
                            Log.w(
                                "NotificationRepo",
                                "Failed to save notification and log",
                                e
                            )

                        }
                }
                .addOnFailureListener { e ->
                    Log.w("NotificationRepo", "Couldn't read recipient's notification preference", e)
                }
        } catch (e: Throwable) {
            Log.w("NotificationRepo", "Firestore error writing notification", e)
        }

        return notification
    }

    /**
     * Dispatches a lottery win notification.
     * US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
     */
    fun notifyLotteryWin(
        recipientDeviceId: String,
        lotteryTitle: String,
        offerId: String,
        userPreferenceEnabled: Boolean = true
    ): AppNotification? {
        return sendNotification(
            recipientDeviceId = recipientDeviceId,
            title = "🎉 You Won: $lotteryTitle!",
            message = "Great news! You were drawn in $lotteryTitle. Choose your dates and reserve before the offer expires.",
            type = NotificationType.LOTTERY_WIN,
            relatedOfferId = offerId,
            userPreferenceEnabled = userPreferenceEnabled
        )
    }

    /**
     * Dispatches a lottery loss notification.
     * US 01.03.01 / US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
     */
    fun notifyLotteryLoss(
        recipientDeviceId: String,
        lotteryTitle: String,
        userPreferenceEnabled: Boolean = true
    ): AppNotification? {
        return sendNotification(
            recipientDeviceId = recipientDeviceId,
            title = "Lottery Result: $lotteryTitle",
            message = "We're sorry to inform you that you were not selected for $lotteryTitle. Stay tuned for future campsite lotteries!",
            type = NotificationType.LOTTERY_LOSS,
            userPreferenceEnabled = userPreferenceEnabled
        )
    }

    /**
     * Dispatches a reservation confirmation notification.
     * US 01.05.02: As an entrant, I want to receive reservation by notification or downloadable CSV.
     */
    fun notifyReservationConfirmed(
        recipientDeviceId: String,
        reservation: Reservation,
        userPreferenceEnabled: Boolean = true
    ): AppNotification? {
        return sendNotification(
            recipientDeviceId = recipientDeviceId,
            title = "🏕️ Booking Confirmed: ${reservation.campsiteName}",
            message = "Your campsite reservation at ${reservation.campsiteName} is confirmed for ${reservation.dateRange} with ${reservation.numGuests} guest(s). Total: $${String.format(java.util.Locale.US, "%.2f", reservation.totalPrice)} CAD. Confirmation ID: #${reservation.id.take(8)}.",
            type = NotificationType.RESERVATION_CONFIRMATION,
            relatedReservationId = reservation.id,
            userPreferenceEnabled = userPreferenceEnabled
        )
    }

    /**
     * Dispatches a reservation cancellation notification.
     */
    fun notifyReservationCancelled(
        recipientDeviceId: String,
        reservation: Reservation,
        userPreferenceEnabled: Boolean = true
    ): AppNotification? {
        return sendNotification(
            recipientDeviceId = recipientDeviceId,
            title = "Reservation Cancelled: ${reservation.campsiteName}",
            message = "Your reservation #${reservation.id.take(8)} for ${reservation.dateRange} has been cancelled.",
            type = NotificationType.RESERVATION_CANCELLATION,
            relatedReservationId = reservation.id,
            userPreferenceEnabled = userPreferenceEnabled
        )
    }

    /**
     * Notifies an entrant that they declined a lottery reservation offer.
     * US 01.04.03: decline a reservation if I changed my mind.
     *
     * @param recipientDeviceId Entrant device identifier.
     * @param offer The declined [LotteryOffer].
     * @param userPreferenceEnabled Whether the entrant has notifications enabled.
     */
    fun notifyOfferDeclined(
        recipientDeviceId: String,
        offer: LotteryOffer,
        userPreferenceEnabled: Boolean = true
    ): AppNotification? {
        return sendNotification(
            recipientDeviceId = recipientDeviceId,
            title = "Offer Declined: ${offer.lotteryTitle}",
            message = "You declined your reservation offer for ${offer.lotteryTitle}. It will be offered to another entrant.",
            type = NotificationType.GENERAL,
            relatedOfferId = offer.id,
            userPreferenceEnabled = userPreferenceEnabled
        )
    }

    /**
     * Retrieves all notifications for the given device.
     *
     * @param deviceId Target device ID. If blank, returns all notifications.
     */
    fun getNotificationsForUser(deviceId: String = ""): List<AppNotification> {
        return _notifications.value.filter { notif ->
            deviceId.isBlank() || notif.recipientDeviceId == deviceId
        }
    }

    /**
     * Returns count of unread notifications for the device.
     */
    fun getUnreadCount(deviceId: String = ""): Int {
        return getNotificationsForUser(deviceId).count { !it.isRead }
    }

    /**
     * Marks a specific notification as read.
     */
    fun markAsRead(notificationId: String) {
        val current = _notifications.value
        val index = current.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            val updated = current.toMutableList()
            updated[index] = updated[index].copy(isRead = true)
            _notifications.value = updated
        }
        firestore?.let { db ->
            try {
                db.collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
            } catch (e: Throwable) {
                Log.w("NotificationRepo", "Firestore error marking notification as read", e)
            }
        }
    }

    /**
     * Marks all notifications for a user as read, locally and in Firestore.
     */
    fun markAllAsRead(deviceId: String = "") {
        getNotificationsForUser(deviceId).filter { !it.isRead }.forEach { markAsRead(it.id) }
    }

    /**
     * Deletes a notification by ID.
     */
    fun deleteNotification(notificationId: String) {
        _notifications.value = _notifications.value.filterNot { it.id == notificationId }
        firestore?.let { db ->
            try {
                db.collection("notifications")
                    .document(notificationId)
                    .delete()
            } catch (e: Throwable) {
                Log.w("NotificationRepo", "Firestore error deleting notification", e)
            }
        }
    }

    /**
     * Starts a live Firestore listener for the given device's notifications,
     * replacing any previous listener. New notifications appear in real time.
     *
     * @param deviceId The signed-in user's device identifier.
     * @param onComplete Optional callback invoked with the user's notifications after the first load.
     */
    fun syncFromFirestore(deviceId: String, onComplete: ((List<AppNotification>) -> Unit)? = null) {
        val db = firestore
        if (db == null || deviceId.isBlank()) {
            onComplete?.invoke(getNotificationsForUser(deviceId))
            return
        }

        listener?.remove()
        var completed = false
        listener = db.collection("notifications")
            .whereEqualTo("recipientDeviceId", deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("NotificationRepo", "Error syncing notifications from Firestore", error)
                } else if (snapshot != null) {
                    _notifications.value = snapshot.documents
                        .mapNotNull { it.toObject(AppNotification::class.java) }
                        .sortedByDescending { it.timestamp }
                }
                if (!completed) {
                    completed = true
                    onComplete?.invoke(getNotificationsForUser(deviceId))
                }
            }
    }

    /**
     * Deletes all notifications for a user, locally and in Firestore.
     * With a blank [deviceId] it only clears local state (used by unit tests).
     */
    fun clearAll(deviceId: String = "") {
        if (deviceId.isBlank()) {
            _notifications.value = emptyList()
        } else {
            getNotificationsForUser(deviceId).forEach { deleteNotification(it.id) }
        }
    }

    companion object {
        @Volatile
        private var instance: NotificationRepository? = null

        /**
         * Accessor for singleton [NotificationRepository] instance.
         */
        fun getInstance(): NotificationRepository {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepository().also { instance = it }
            }
        }
    }

    /**
     * Sends a notification from a campsite owner to the
     * entrants on the campsite lottery waitlist.
     *
     * Verifies ownership and respects notification preferences.
     * Supports US 02.10.01.
     *
     * @param ownerId Device ID of owner sending announcement.
     * @param campsiteId ID of the owner's campsite.
     * @param lotteryId ID of the associated lottery.
     * @param title Title of the announcement.
     * @param message Content of the announcement.
     * @param onSuccess Called with the number of notifications sent.
     * @param onFail called if the operation fails.
     */
    fun sendWaitlistNotification(
        ownerId: String,
        campsiteId: String,
        lotteryId: String,
        title: String,
        message: String,
        onSuccess: (Int) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        if (title.isBlank() || message.isBlank()) {
            onFail(Exception("Title and message cannot be empty"))
            return
        }

        if (
            ownerId.isBlank() ||
            campsiteId.isBlank() ||
            lotteryId.isBlank()
        ) {
            onFail(Exception("Missing owner, campsite, or lottery ID"))
            return
        }

        val db = FirebaseFirestore.getInstance()

        val campsiteRef = db.collection("campsites")
            .document(campsiteId)

        val lotteryRef = db.collection("lotteries")
            .document(lotteryId)

        val announcementId = UUID.randomUUID().toString()

        db.runTransaction { transaction ->

            val campsite = transaction.get(campsiteRef)
                .toObject(Campsite::class.java)
                ?: throw Exception("Campsite not found")

            if (campsite.ownerId != ownerId) {
                throw Exception("You do not own this campsite")
            }

            val lottery = transaction.get(lotteryRef)
                .toObject(Lottery::class.java)
                ?: throw Exception("Lottery not found")

            if (
                campsiteId !in lottery.campIds ||
                campsite.lotteryId != lotteryId
            ) {
                throw Exception("Campsite does not belong to this lottery")
            }

            val entrants = lottery.waitlist
                .filter { it.isNotBlank() }
                .distinct()

            // sets max announcement size for the waitlist, configurable is list is too long
            val maxAnnouncementSize = 200
            entrants.size.let {
                if (it > maxAnnouncementSize) {
                    throw Exception("Waitlist is too large for one announcement")
                }
            }

            val recipients = entrants.filter { entrantId ->

                val userRef = db.collection("users")
                    .document(entrantId)

                val user = transaction.get(userRef)

                user.exists() && user.getBoolean("notificationsEnabled") != false
            }

            // database reads complete above, database transaction writes start below


            for (recipientId in recipients) {

                val notification = AppNotification(
                    recipientDeviceId = recipientId,
                    senderDeviceId = ownerId,
                    title = title.trim(),
                    message = message.trim(),
                    type = NotificationType.GENERAL,
                    relatedLotteryId = lotteryId,
                    relatedCampsiteId = campsiteId,
                    announcementId = announcementId
                )

                val notificationRef = db.collection("notifications")
                    .document(notification.id)

                val logRef = db.collection("notification_logs")
                    .document(notification.id)

                transaction.set(notificationRef, notification)
                transaction.set(logRef, notification)
            }

            // returns the number of recipients
            recipients.size

        }.addOnSuccessListener {
            count -> onSuccess(count) 
        }.addOnFailureListener {
            exception -> onFail(exception)
        }
    }

    /**
     * Listens to previously sent notifications belonging
     * to a campsite owner.
     *
     * Supports US 02.10.02.
     *
     * @param ownerId Device ID of campsite owner
     * @param onResult Called with the owner's sent notifications
     * @param onListening Provides the active listener registration
     * @param onFail Called if notifications cannot be retrieved
     */
    fun listenForSentNotifications(
        ownerId: String,
        onResult: (List<AppNotification>) -> Unit,
        onListening: (ListenerRegistration) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        UserRepository.getUser(
            deviceId = ownerId,

            onResult = { user ->

                if (user == null || "OWNER" !in user.roles) {
                    onFail(Exception("Owner access required"))
                } else {

                    val db = FirebaseFirestore.getInstance()

                    val listener = db.collection("notification_logs")
                        .whereEqualTo("senderDeviceId", ownerId)
                        .addSnapshotListener { snapshot, error ->

                            if (error != null) {
                                onFail(error)
                                return@addSnapshotListener
                            }

                            val notifications = snapshot
                                ?.toObjects(AppNotification::class.java)
                                ?: emptyList()

                            onResult(
                                notifications.sortedByDescending {
                                    it.timestamp
                                }
                            )
                        }

                    onListening(listener)

                }
            },

            onFail = onFail

        )
    }

    /**
     * Allows an admin to review all notification logs.
     *
     * Supports US 03.08.01.
     *
     * @param deviceId Device ID of the admin
     * @param onResult Called with all notification logs
     * @param onListening Provides the active listener registration
     * @param onFail Called if access is denied or retrieval fails
     */
    fun listenForAllNotificationLogs(
        deviceId: String,
        onResult: (List<AppNotification>) -> Unit,
        onListening: (ListenerRegistration) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        UserRepository.getUser(
            deviceId = deviceId,

            onResult = {user ->

                if (user == null || !Admin.isAdmin(user.roles)) {
                    onFail(Exception("Admin access required"))
                } else {

                    val db = FirebaseFirestore.getInstance()

                    val listener = db.collection("notification_logs")
                        .addSnapshotListener { snapshot, error ->

                            if (error != null) {
                                onFail(error)
                                return@addSnapshotListener
                            }

                            val notifications = snapshot
                                ?.toObjects(AppNotification::class.java)
                                ?: emptyList()

                            onResult(
                                notifications.sortedByDescending {
                                    it.timestamp
                                }
                            )
                        }

                    onListening(listener)

                }
            },

            onFail = onFail

        )

    }
}
