package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quackapp.Controller.ChatRepository
import com.example.quackapp.Controller.DeviceIdGetter
import com.example.quackapp.Model.Chat
import com.example.quackapp.View.Entrant.EntrantScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChatList (
    navController: NavController,
) {

    var deviceId by remember { mutableStateOf<String?>(null) }
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    val filteredChats = chats.filter {
        it.lastMessage.contains(query, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        DeviceIdGetter.getDeviceId(
            onSuccess = { id -> deviceId = id },
            onFail = { exception ->
                errorMessage = exception.message ?: "Can't get device ID"
            }
        )
    }

    DisposableEffect(deviceId) {
        val listener = deviceId?.let { id ->
            ChatRepository.listenForChats(
                deviceId = id,
                onResult = { result ->
                    chats = result
                    errorMessage = null
                },
                onFail = { exception ->
                    errorMessage = exception.message ?: "Can't load chats"
                }
            )
        }

        onDispose { listener?.remove() }
    }


    EntrantScaffold(navController) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchBar(
                        query = query,
                        whenQueryChanges = { query = it },
                        searchBarText = "Search...",
                        searchBarColor = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            deviceId?.let { id ->
                items(filteredChats, key = { it.chatId }) { chat ->
                    UserChatCard(
                        chat = chat,
                        deviceId = id,
                        onClick = {
                            //navController.navigate("chat/${chat.chatId}")
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }

        }
    }
}