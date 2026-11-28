package com.example.quackapp.View.Admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onVisibilityChangedNode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.test.platform.view.inspector.WindowInspectorCompat
import com.example.quackapp.Controller.AdminCommunicationLogsController
import com.example.quackapp.Model.AppNotification
import com.example.quackapp.Model.CampsiteOwner
import com.example.quackapp.Model.Chat
import java.text.DateFormat
import java.util.Date

/**
 * Allows admins to browse notification and message logs.
 *
 * Supports US 03.08.01 and US 03.12.01.
 *
 * @param navC
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCommunicationLogs(
    navController: NavController,
    initialTab: String = "notifications",
    viewModel: AdminCommunicationLogsController = viewModel()
) {

    var selectedTab by remember(initialTab) {
        mutableIntStateOf(
            if (initialTab == "messages") 1 else 0
        )
    }

    AdminScaffold(
        navController = navController,
        title = "Communication Logs"
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            PrimaryTabRow(
                selectedTabIndex = selectedTab
            ) {

                Tab(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    text = { Text("Notifications") },
                    modifier = Modifier.testTag("notificationLogsTab")
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Messages") },
                    modifier = Modifier.testTag("messageLogsTab")
                )
            }

            when (selectedTab) {

                0 -> NotificationLogs(viewModel)

                //1 -> ChatLogs(viewModel = viewModel, navController = navController)

            }
        }
    }
}

/**
 * Displays all recorded notifications.
 */
@Composable
private fun NotificationLogs(
    viewModel: AdminCommunicationLogsController
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when {

            viewModel.notificationsLoading -> {

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            viewModel.notificationError != null -> {

                Text(
                    text = viewModel.notificationError?: "Unable to load notifications",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            viewModel.notifications.isEmpty() -> {

                Text(
                    text = "No notification logs found.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = viewModel.notifications,
                        key = { it.id }
                    ) { notification ->

                        NotificationLogCard(
                            notification = notification,
                            userNames = viewModel.userNames,
                            campsiteNames = viewModel.campsiteNames
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays an individual notification and its
 * sender, recipient, and associated campsite.
 */
@Composable
private fun NotificationLogCard(
    notification: AppNotification,
    userNames: Map<String, String>,
    campsiteNames: Map<String, String>
) {

    val senderName = notification.senderDeviceId?.let {
        userNames[it] ?: it
    } ?: "System"

    val recipientName =
        userNames[notification.recipientDeviceId]?: notification.recipientDeviceId

    val campsiteName = notification.relatedCampsiteId?.let {
        campsiteNames[it] ?: it
    }

    val formattedDate = remember(notification.timestamp) {

        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        ).format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notificationLogCard"),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall
            )

            Text("From: $senderName")

            Text("To: $recipientName")

            if (campsiteName != null) {
                Text("Campsite: $campsiteName")
            }

            Text(
                text = "Type: ${notification.type.name.replace("_", "")}",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            Text(notification.message)
        }
    }
}

/**
 * Displays all conversations for admin review.
 */
@Composable
private fun ChatLogs(
    viewModel: AdminCommunicationLogsController,
    navController: NavController
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when {

            viewModel.chatsLoading -> {

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            viewModel.chatError != null -> {

                Text(
                    text = viewModel.chatError?: "Unable to load conversations",

                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            viewModel.chats.isEmpty() -> {

                Text(
                    text = "No conversations found",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = viewModel.chats,
                        key = { it.chatId }
                    ) { chat ->

                        ChatLogCard(
                            chat = chat,
                            userNames = viewModel.userNames,
                            campsiteNames = viewModel.campsiteNames,
                            onClick = {
                                navController.navigate("AdminConversation/${chat.chatId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a summary of a conversation between a campsite
 * owner and an entrant.
 */
@Composable
fun ChatLogCard(
    chat: Chat,
    userNames: Map<String, String>,
    campsiteNames: Map<String, String>,
    onClick: () -> Unit
) {

    val ownerName = userNames[chat.ownerId] ?: chat.ownerId

    val entrantName = userNames[chat.entrantId] ?: chat.entrantId

    val campsiteName = campsiteNames[chat.campsiteId] ?: chat.campsiteId

    val timestamp = chat.lastMessageTimestamp ?: chat.created

    val formattedDate = remember(timestamp) {

        timestamp?.let {
            DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(it.toDate())
        } ?: "Unknown date"
    }

    Card(
        onClick = onClick,

        modifier = Modifier
            .fillMaxWidth()
            .testTag("chatLogCard"),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Text(
            text = campsiteName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text("Owner: $ownerName")

        Text("Entrant: $entrantName")

        HorizontalDivider()

        Text(
            text = chat.lastMessage.ifBlank {
                "No messages yet"
            }
        )

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = "View Conversation",
            color = AdminOrange,
            fontWeight = FontWeight.Medium
        )
    }

}