package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.example.quackapp.Controller.UserRepository
import com.example.quackapp.Model.Chat
import java.text.SimpleDateFormat

@Composable
fun UserChatCard(
    chat: Chat,
    deviceId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val otherUserId = if (deviceId == chat.ownerId) chat.entrantId else chat.ownerId

    var name by remember(otherUserId) { mutableStateOf("") }

    LaunchedEffect(otherUserId) {
        UserRepository.getUser(
            deviceId = otherUserId,
            onResult = { user ->
                name = user?.name ?: ""
            },
            onFail = { name = "" }
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF248AFF),
            contentColor = Color.White,
        ),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        //Chat Card Summary Details
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Name of Chat
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineLarge
                )

                //Last Message timestamp sent in chat
                chat.lastMessageTimestamp?.let { timestamp ->
                    val formatted =
                        SimpleDateFormat("h:mm a", LocalLocale.current.platformLocale)
                            .format(timestamp.toDate())
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
                //Last Message sent in chat
                Text(
                    text = "${name}: ${chat.lastMessage}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 6.dp)
                )

        }
    }

}