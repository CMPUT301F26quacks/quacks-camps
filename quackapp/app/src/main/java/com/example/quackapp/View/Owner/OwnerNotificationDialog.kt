package com.example.quackapp.View.Owner


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.quackapp.Controller.OwnerCampDetailsController

/**
 * Dialog allowing campsite owners to create custom notifications for
 * entrants on their lottery waitlist.
 *
 * Supports US 01.10.01.
 */
@Composable
fun OwnerNotificationDialog(
    viewModel: OwnerCampDetailsController,
    entrantCount: Int,
    onDismiss: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val sentCount = viewModel.notificationSentCount
    val isSending = viewModel.notificationSending

    AlertDialog(

        onDismissRequest = {
            if (!isSending) {
                onDismiss()
            }
        },

        icon = {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null
            )
        },

        title = {
            Text(
                if (sentCount != null) {
                    "Quack!"
                } else {
                    "Send Notification"
                }
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (sentCount != null) {

                    Text(
                        if (sentCount == 0) {
                            "No eligible entrants received the notification"
                        } else {
                            "Notification sent to $sentCount entrant(s)"
                        }
                    )
                } else {

                    Text("$entrantCount entrant(s) on the lottery waitlist")

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        enabled = !isSending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notificationTitle")
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        minLines = 3,
                        maxLines = 5,
                        enabled = !isSending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notificationMessage")
                    )

                    Text(
                        text = "Only entrants with notifications enabled" +
                                " will receive this announcement",
                        style = MaterialTheme.typography.bodySmall
                    )

                    viewModel.notificationError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },

        confirmButton = {

            if (sentCount != null) {

                Button(onClick = onDismiss) {
                    Text("Done")
                }
            } else {

                Button(
                    onClick = {
                        viewModel.sendWaitlistNotification(
                            title = title,
                            message = message
                        )
                    },

                    enabled = title.isNotBlank() &&
                            message.isNotBlank() &&
                            !isSending &&
                            entrantCount > 0,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ),

                    modifier = Modifier.testTag("sendNotificationButton")
                ) {

                    if (isSending) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send")
                    }
                }
            }
        },

        dismissButton = {

            if (sentCount == null) {

                TextButton(
                    onClick = onDismiss,
                    enabled = !isSending
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}