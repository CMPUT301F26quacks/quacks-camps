package com.example.quackapp.View.Admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.AdminController
import com.example.quackapp.Model.User

/**
 * Screen for searching entrant and owner profiles by name, viewing their details,
 * and removing owner privileges when an owner violates app policy.
 *
 * @param navController navigation controller for [AdminScaffold].
 * @param viewModel controller providing the live list of [User] profiles and owner removal.
 */
@Composable
fun AdminProfilesScreen(
    navController: NavController,
    viewModel: AdminController = viewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    var search by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val filtered = profiles.filter {
        search.isBlank() || it.name.contains(search.trim(), ignoreCase = true)
    }

    AdminScaffold(navController = navController, title = "Browse Profiles") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search by name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedUser = user }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = user.name.ifBlank { "Unnamed User" },
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = user.roles.joinToString(", "))
                        }
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        val isOwner = user.roles.any { it.equals("OWNER", ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { selectedUser = null },
            title = { Text(user.name.ifBlank { "Unnamed User" }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Email: ${user.email.ifBlank { "None" }}")
                    Text("Phone: ${user.phoneNumber.ifBlank { "None" }}")
                    Text("Roles: ${user.roles.joinToString(", ").ifBlank { "None" }}")
                    Text("Device ID: ${user.deviceId}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedUser = null }) {
                    Text("Close")
                }
            },
            dismissButton = {
                if (isOwner) {
                    TextButton(
                        onClick = {
                            viewModel.removeOwner(user.deviceId)
                            selectedUser = null
                        }
                    ) {
                        Text("Remove Owner", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}