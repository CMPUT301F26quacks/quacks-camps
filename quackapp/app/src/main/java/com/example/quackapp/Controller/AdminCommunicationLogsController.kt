package com.example.quackapp.Controller

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.FirstBaseline
import androidx.lifecycle.ViewModel
import androidx.test.platform.device.DeviceController
import com.example.quackapp.Model.AppNotification
import com.example.quackapp.Model.Chat
import com.example.quackapp.Model.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


/**
 * Controller responsible for retrieving chats and notifications
 * for admin review.
 *
 * Supports US 03.08.01 and US 03.12.01.
 */
class AdminCommunicationLogsController : ViewModel() {

    private val notificationRepository = NotificationRepository.getInstance()

    private var notificationListener: ListenerRegistration? = null

    private var chatListener: ListenerRegistration? = null

    private var cleared = false

    var notifications by mutableStateOf<List<AppNotification>>(emptyList())
        private set

    var chats by mutableStateOf<List<Chat>>(emptyList())
        private set

    var notificationsLoading by mutableStateOf(true)
        private set

    var chatsLoading by mutableStateOf(true)
        private set

    var notificationError by mutableStateOf<String?>(null)
        private set

    var chatError by mutableStateOf<String?>(null)
        private set

    var userNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var campsiteNames by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    private val requestedUserIds = mutableSetOf<String>()

    private val requestedCampsiteIds = mutableSetOf<String>()

    init {
        loadCommunicationLogs()
    }

    /**
     * Identifies the current device and starts retrieving
     * notification and chat logs.
     */
    private fun loadCommunicationLogs() {

        DeviceIdGetter.getDeviceId(

            onSuccess = { deviceId ->

                if (!cleared) {
                    loadNotificationLogs(deviceId)
                    loadChatLogs(deviceId)
                }
            },

            onFail = { exception ->

                if (!cleared) {

                    val error = exception.message?: "Unable to identify this device"

                    notificationError = error
                    chatError = error

                    notificationsLoading = false
                    chatsLoading = false
                }
            }
        )
    }

    /**
     * Listens for all notifications recorded in Firebase.
     *
     * @param deviceId Device ID of the admin
     */
    private fun loadNotificationLogs(deviceId: String) {

        notificationRepository.listenForAllNotificationLogs(

            deviceId = deviceId,

            onResult = { logs ->

                if (!cleared) {

                    notifications = logs
                    notificationsLoading = false
                    notificationError = null

                    logs.forEach { notification ->

                        loadUserName(notification.senderDeviceId)

                        loadUserName(notification.recipientDeviceId)

                        loadCampsiteName(notification.relatedCampsiteId)
                    }
                }
            },

            onListening = { registration ->

                if (cleared) {
                    registration.remove()
                } else {
                    notificationListener = registration
                }
            },

            onFail = { exception ->

                if (!cleared) {

                    notificationError = exception.message?: "Unable to load notification logs"

                    notificationsLoading = false
                }
            }
        )
    }

    /**
     * Listens for all conversations recorded in Firebase.
     *
     * @param deviceId Device ID of the admin.
     */
    private fun loadChatLogs(deviceId: String) {

        ChatRepository.listenForAllChats(

            deviceId = deviceId,

            onResult = { result ->

                if (!cleared) {

                    chats = result
                    chatsLoading = false
                    chatError = null

                    result.forEach { chat ->

                        loadUserName(chat.ownerId)

                        loadUserName(chat.entrantId)

                        loadCampsiteName(chat.campsiteId)
                    }
                }
            },

            onListening = { registration ->

                if (cleared) {
                    registration.remove()
                } else {
                    chatListener = registration
                }
            },

            onFail = { exception ->

                if (!cleared) {

                    chatError = exception.message?: "Unable to load chat logs"

                    chatsLoading = false
                }
            }
        )
    }

    private fun loadUserName(deviceId: String?) {

        if (deviceId.isNullOrBlank()) {
            return
        }

        if (!requestedUserIds.add(deviceId)) {
            return
        }

        UserRepository.getUser(

            deviceId = deviceId,

            onResult = {user ->

                if (!cleared) {

                    val name = user?.name

                    if (!name.isNullOrBlank()) {

                        userNames = userNames + (deviceId to name)

                    }
                }
            },

            onFail = {}

        )
    }

    /**
     * Retrieves the name of a campsite associated with a log.
     */
    private fun loadCampsiteName(campsiteId: String?) {

        if (campsiteId.isNullOrBlank()) {
            return
        }

        if (!requestedCampsiteIds.add(campsiteId)) {
            return
        }

        FirebaseFirestore.getInstance()
            .collection("campsites")
            .document(campsiteId)
            .get()
            .addOnSuccessListener { document ->

                val name = document.getString("name")

                if (!cleared &&!name.isNullOrBlank()) {

                    campsiteNames = campsiteNames + (campsiteId to name)

                }
            }
    }

    /**
     * Removes active Firebase listener when the
     * controller is destroyed.
     */
    override fun onCleared() {
        cleared = true
        notificationListener?.remove()
        chatListener?.remove()
        super.onCleared()
    }

}
