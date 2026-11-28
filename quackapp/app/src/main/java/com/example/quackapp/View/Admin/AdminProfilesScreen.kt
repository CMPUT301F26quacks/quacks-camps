package com.example.quackapp.View.Admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
    viewModel: AdminController = viewModel(),
    onDeleteProfile: (User) -> Unit = { viewModel.deleteProfile(it) }
) {
    val profiles by viewModel.profiles.collectAsState()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var userToRemove by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var adminUserWarning by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(viewModel.deleteError) {
        viewModel.deleteError?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(viewModel.deleteSuccess) {
        viewModel.deleteSuccess?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AdminBlue, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = user.name.ifBlank { "Unnamed User" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(text = user.roles.joinToString(", "))
                            }
                            IconButton(onClick = {
                                if (user.roles.any { it.equals("ADMIN", ignoreCase = true) }) {
                                    adminUserWarning = user
                                } else {
                                    userToDelete = user
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove profile",
                                    tint = Color.White
                                )
                            }
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
                            userToRemove = user
                            selectedUser = null
                        }
                    ) {
                        Text("Remove Owner", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
    // Confirmation dialog before removing owner
    userToRemove?.let { user ->
        AlertDialog(
            onDismissRequest = { userToRemove = null },
            title = { Text("Remove Campsite Owner?") },
            text = {
                Text(
                    "Are you sure you want to remove ${user.name.ifBlank { "this user" }} as a campsite owner? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeOwner(user.deviceId)
                        userToRemove = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog before deleting profile
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete profile?") },
            text = {
                Text("\"${user.name.ifBlank { "This user" }}\" will be permanently deleted. This can't be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(user)
                        userToDelete = null
                    },
                    modifier = Modifier.testTag("confirmDeleteProfile")
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Warning dialog when trying to delete an admin user
    adminUserWarning?.let { user ->
        AlertDialog(
            onDismissRequest = { adminUserWarning = null },
            title = { Text("Cannot delete admin") },
            text = { Text("You cannot delete an admin user.") },
            confirmButton = {
                TextButton(onClick = { adminUserWarning = null }) {
                    Text("OK")
                }
            }
        )
    }
}
