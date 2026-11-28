package com.example.quackapp.Controller

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.example.quackapp.Model.AppNotification
import com.example.quackapp.Model.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


/**
 * Represents an announcement and its recipients.
 */
data class SentAnnouncement(
    val notification: AppNotification,
    val recipientIds: List<String>
)

/**
 * Retrieves and groups previously sent notifications belonging
 * to a campsite owner.
 *
 * Supports US 02.10.02.
 */
class OwnerNotificationHistoryController : ViewModel() {

    private val repo = NotificationRepository.getInstance()

    private var listener: ListenerRegistration? = null

    private var cleared = false

    var announcements by mutableStateOf<List<SentAnnouncement>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var campsiteNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    private val requestedCampsiteIds = mutableSetOf<String>()

    init {
        loadNotifications()
    }

    /**
     * Retrieves the current owner's device ID and listens
     * for their previously sent notifications.
     */
    private fun loadNotifications() {

        DeviceIdGetter.getDeviceId(

            onSuccess = { ownerId ->

                if (!cleared) {
                    repo.listenForSentNotifications(
                        ownerId = ownerId,

                        onResult = { notifications ->

                            if (!cleared) {
                                announcements = groupAnnouncements(notifications)
                                loadCampsiteNames(notifications)
                                isLoading = false
                            }
                        },

                        onListening = { registration ->

                            if (cleared) {
                                registration.remove()
                            } else {
                                listener = registration
                            }
                        },

                        onFail = { exception ->
                            handleError(exception)
                        }
                    )
                }
            },

            onFail = {exception -> handleError(exception)}

        )
    }

    /**
     * Groups notifications belonging to the same announcement.
     */
    private fun groupAnnouncements(
        notifications: List<AppNotification>
    ): List<SentAnnouncement> {

        return notifications
            .groupBy { it.announcementId ?: it.id }
            .values
            .map { group ->

                SentAnnouncement(
                    notification = group.first(),
                    recipientIds = group.map {
                        it.recipientDeviceId
                    }.distinct()
                )
            }
            .sortedByDescending { it.notification.timestamp }
    }

    /**
     * Retrieves campsite names associated with announcements.
     */
    private fun loadCampsiteNames(
        notifications: List<AppNotification>
    ) {

        val campsiteIds = notifications.mapNotNull { it.relatedCampsiteId }
            .filter {it.isNotBlank()}
            .distinct()

        for (campsiteId in campsiteIds) {

            if (!requestedCampsiteIds.add(campsiteId)) {
                continue
            }

            FirebaseFirestore.getInstance()
                .collection("campsites")
                .document(campsiteId)
                .get()
                .addOnSuccessListener { document ->

                    val name = document.getString("name")

                    if (!cleared && name != null) {
                        campsiteNames += (campsiteId to name)
                    }

                }
        }
    }

    private fun handleError(exception: Exception) {

        if (!cleared) {
            errorMessage = exception.message ?: "Unable to load sent notifications"
            isLoading = false
        }
    }

    /**
     * Removes Firestore listener when controller is destroyed.
     */
    override fun onCleared() {
        cleared = true
        listener?.remove()
        super.onCleared()
    }

}