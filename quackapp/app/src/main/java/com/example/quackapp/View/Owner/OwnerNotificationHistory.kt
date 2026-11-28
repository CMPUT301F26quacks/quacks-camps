package com.example.quackapp.View.Owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.OwnerNotificationHistoryController
import com.example.quackapp.Controller.SentAnnouncement
import java.text.DateFormat
import java.util.Date


/**
 * Displays all previously sent notifications for a campsite owner.
 *
 * Supports US 02.10.02.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerNotificationHistory(
    navController: NavController,
    viewModel: OwnerNotificationHistoryController = viewModel()
) {

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("Sent Notifications")
                },

                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when {

                viewModel.isLoading -> {

                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                viewModel.errorMessage != null -> {

                    Text(
                        text = viewModel.errorMessage ?: "Unable to load notifications",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                viewModel.announcements.isEmpty() -> {

                    Text(
                        text = "YOu haven't sent any notifications yet",
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
                            items = viewModel.announcements,

                            key = { it.notification.announcementId ?: it.notification.id }

                        ) { announcement ->

                            AnnouncementCard(
                                announcement = announcement,
                                campsiteName = announcement.notification.relatedCampsiteId
                                    ?.let { viewModel.campsiteNames[it] }
                            )
                        }
                    }
                }
            }
        }
    }
}

    /**
     * Displays a single announcement and its recipients.
     */
    @Composable
    private fun AnnouncementCard(
        announcement: SentAnnouncement,
        campsiteName: String?
    ) {

        val notification = announcement.notification

        var expanded by remember { mutableStateOf(false) }

        val formattedDate = remember(notification.timestamp) {

            DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(Date(notification.timestamp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("announcementCard"),

            shape = RoundedCornerShape(12.dp),

            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),

                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = campsiteName
                        ?: notification.relatedCampsiteId
                        ?: "Unknown Campsite",

                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

                Text(notification.message)

                TextButton(
                    onClick = {
                        expanded = !expanded
                    },

                    modifier = Modifier.testTag("viewRecipients")

                ) {

                    Text(
                        if (expanded) {
                            "Hide Recipients"
                        } else {
                            "Sent to ${announcement.recipientIds.size} entrants"
                        }
                    )
                }

                if (expanded) {

                    Text(
                        text = "Recipient device IDs:",
                        fontWeight = FontWeight.Bold
                    )

                    announcement.recipientIds.forEach { recipientId ->

                        Text(
                            text = recipientId,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }