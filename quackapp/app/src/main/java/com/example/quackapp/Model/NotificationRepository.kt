package com.example.quackapp.Model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository and Controller for managing platform notifications.
 * Synchronizes with Firebase Cloud Firestore ("notifications" collection),
 * with safe local memory fallback for offline use and unit tests.
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

    init {
        loadInitialMockData()
    }

    /**
     * Resets notifications to standard mock seed data for testing.
     */
    fun resetToMockData() {
        loadInitialMockData()
    }

    private fun loadInitialMockData() {
        val initialList = listOf(
            AppNotification(
                id = "notif-win-01",
                recipientDeviceId = "default_device",
                title = "🎉 You Won the Lottery!",
                message = "Congratulations! You have been selected in the Banff Victoria Day Long Weekend Draw. Select your campsite dates now to confirm your stay.",
                type = NotificationType.LOTTERY_WIN,
                relatedOfferId = "offer-banff-01",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000L // 1 hour ago
            ),
            AppNotification(
                id = "notif-loss-01",
                recipientDeviceId = "default_device",
                title = "Lottery Draw Result",
                message = "You were not drawn for the Maligne Canyon Summer Solstice Lottery. Thank you for participating, and check back for future draws!",
                type = NotificationType.LOTTERY_LOSS,
                isRead = false,
                timestamp = System.currentTimeMillis() - 7200000L // 2 hours ago
            ),
            AppNotification(
                id = "notif-res-01",
                recipientDeviceId = "default_device",
                title = "🏕️ Reservation Confirmed",
                message = "Your reservation at Hinton-Grande Cache Cookhouse (May 7 - May 9, 2027) is confirmed. Reservation ID: #res-101.",
                type = NotificationType.RESERVATION_CONFIRMATION,
                relatedReservationId = "res-101",
                isRead = true,
                timestamp = System.currentTimeMillis() - 86400000L // 1 day ago
            )
        )
        _notifications.value = initialList
    }

    /**
     * Sends an in-app notification to the specified entrant device.
     * Respects user preference: if [userPreferenceEnabled] is false (opted out),
     * delivery is suppressed.
     *
     * @param recipientDeviceId Target device identifier.
     * @param title Title of notification.
     * @param message Body text.
     * @param type Notification category.
     * @param relatedOfferId Optional ID of associated lottery offer.
     * @param relatedReservationId Optional ID of associated reservation.
     * @param userPreferenceEnabled Whether user has notifications enabled.
     * @return Dispatched [AppNotification], or null if suppressed by user preference.
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
            recipientDeviceId = recipientDeviceId.ifBlank { "default_device" },
            title = title,
            message = message,
            type = type,
            relatedOfferId = relatedOfferId,
            relatedReservationId = relatedReservationId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )

        _notifications.value = listOf(notification) + _notifications.value

        // Sync to Firestore
        firestore?.let { db ->
            try {
                db.collection("notifications")
                    .document(notification.id)
                    .set(notification)
                    .addOnFailureListener { e ->
                        Log.w("NotificationRepo", "Failed to sync notification to Firestore", e)
                    }
            } catch (e: Throwable) {
                Log.w("NotificationRepo", "Firestore error writing notification", e)
            }
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
     * Retrieves all notifications applicable to the given entrant device.
     *
     * @param deviceId Target device ID. If blank, returns all notifications.
     * @param includeDefaults Whether to also include general default mock notifications.
     */
    fun getNotificationsForUser(deviceId: String = "", includeDefaults: Boolean = false): List<AppNotification> {
        return _notifications.value.filter { notif ->
            deviceId.isBlank() ||
                    notif.recipientDeviceId == deviceId ||
                    (includeDefaults && notif.recipientDeviceId == "default_device")
        }
    }

    /**
     * Returns count of unread notifications for the device.
     */
    fun getUnreadCount(deviceId: String = ""): Int {
        return getNotificationsForUser(deviceId, includeDefaults = (deviceId == "default_device" || deviceId.isBlank())).count { !it.isRead }
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
     * Marks all notifications for a user as read.
     */
    fun markAllAsRead(deviceId: String = "") {
        _notifications.value = _notifications.value.map { notif ->
            if (deviceId.isBlank() || notif.recipientDeviceId == deviceId || notif.recipientDeviceId == "default_device") {
                notif.copy(isRead = true)
            } else {
                notif
            }
        }
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
     * Synchronizes notifications from Cloud Firestore for the given recipient device.
     */
    fun syncFromFirestore(deviceId: String = "", onComplete: ((List<AppNotification>) -> Unit)? = null) {
        val db = firestore ?: run {
            onComplete?.invoke(getNotificationsForUser(deviceId))
            return
        }

        val targetDevice = if (deviceId.isBlank()) "default_device" else deviceId
        db.collection("notifications")
            .whereEqualTo("recipientDeviceId", targetDevice)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { it.toObject(AppNotification::class.java) }
                if (fetched.isNotEmpty()) {
                    val fetchedIds = fetched.map { it.id }.toSet()
                    val remaining = _notifications.value.filter { it.id !in fetchedIds }
                    _notifications.value = fetched + remaining
                }
                onComplete?.invoke(getNotificationsForUser(deviceId))
            }
            .addOnFailureListener { e ->
                Log.w("NotificationRepo", "Error syncing notifications from Firestore", e)
                onComplete?.invoke(getNotificationsForUser(deviceId))
            }
    }

    /**
     * Clears all notifications for a user.
     */
    fun clearAll(deviceId: String = "") {
        if (deviceId.isBlank()) {
            _notifications.value = emptyList()
        } else {
            _notifications.value = _notifications.value.filterNot {
                it.recipientDeviceId == deviceId || it.recipientDeviceId == "default_device"
            }
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
}
