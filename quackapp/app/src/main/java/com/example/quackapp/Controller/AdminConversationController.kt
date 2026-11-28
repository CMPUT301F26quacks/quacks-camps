package com.example.quackapp.Controller

import android.health.connect.datatypes.Device
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.core.os.registerForAllProfilingResults
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quackapp.Model.Admin
import com.example.quackapp.Model.Message
import com.google.firebase.firestore.ListenerRegistration


/**
 * Retrieves individual messages within a conversation
 * for read-only admin review.
 *
 * Supports US 03.12.01.
 */
class AdminConversationController(
    savedStateHandle: SavedStateHandle
) : ViewModel {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private var listener: ListenerRegistration? = null

    private var cleared = false

    var messages by mutableStateOf<List<Message>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userNames by mutableStateOf<Map<String, String>>(emptyMap())

    private val requestedUserIds = mutableSetOf<String>()

    init { loadConversation() }

    /**
     * Identifies the current device and admin access.
     */
    private fun loadConversation() {

        DeviceIdGetter.getDeviceId(

            onSuccess = { deviceId ->

                if (!cleared) {

                    UserRepository.getUser(

                        deviceId = deviceId,

                        onResult = { user ->

                            if (!cleared) {

                                if (user != null &&
                                    Admin.isAdmin(user.roles)
                                    ) {
                                    loadMessages(deviceId)
                                } else {
                                    handleError(Exception("Admin access required."))
                                }
                            }
                        },

                        onFail = { exception -> handleError(exception) }

                    )
                }
            },

            onFail = {exception -> handleError(exception)}

        )
    }

    /**
     * Listens for all messages within the selected conversation.
     *
     * @param deviceId Device ID of the admin.
     */
    private fun loadMessages(deviceId: String) {

        ChatRepository.listenForMessages(

            chatId = chatId,

            deviceId = deviceId,

            onResult = { result ->

                if (!cleared) {

                    messages = result

                    isLoading = false

                    errorMessage = null

                    result.forEach { message ->

                        loadUserName(message.senderId)
                        loadUserName(message.recipientId)
                    }
                }
            },

            onListening = { registration ->

                if (!cleared) {
                    registration.remove()
                } else {
                    listener = registration
                }
            },

            onFail = {exception -> handleError(exception)}

        )

    }

}