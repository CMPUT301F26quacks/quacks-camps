package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quackapp.Controller.DeviceIdGetter
import com.example.quackapp.Controller.UserRepository
import com.example.quackapp.Model.User

import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton

/**
 * Shared screen for creating and editing a user profile.
 *
 * During registration, the screen collects personal information and creates a
 * Firestore profile for the selected role. In edit mode, it loads the existing
 * profile and allows the user to update name, email, phone number, and notification
 * preference. Users can also view the app policy and delete their profile.
 *
 * Supports:
 * US 01.07.03 - Update personal information.
 * US 01.08.01 - Delete profile.
 * US 01.10.01 - Opt out of notifications.
 * US 01.11.01 - View app policy and guidelines.
 *
 * @param navController controls navigation after profile actions.
 * @param selectedRole role assigned when registering a new user.
 * @param isEditing true when editing an existing profile instead of creating one.
 */
@Composable
fun UserProfile(
    navController: NavController,
    selectedRole: String? = null,
    isEditing: Boolean = false
) {

    var name by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var phoneNumber by remember { mutableStateOf("") }

    var deviceId by remember { mutableStateOf<String?>(null) }

    var notificationsEnabled by remember { mutableStateOf<Boolean>(true) }

    var userRoles by remember { mutableStateOf<List<String>>(emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var policyExpanded by remember { mutableStateOf<Boolean>(false) }

    LaunchedEffect(Unit) {

        DeviceIdGetter.getDeviceId(

            onSuccess = {
                id -> deviceId = id

                if (isEditing) {

                    UserRepository.getUser(

                        deviceId = id,

                        onResult = { user ->
                            if (user != null) {
                                name = user.name
                                email = user.email
                                phoneNumber = user.phoneNumber
                                notificationsEnabled = user.notificationsEnabled
                                userRoles = user.roles
                            } else {
                                errorMessage = "Unable to find User Profile"
                            }
                        },

                        onFail = {
                            exception -> errorMessage = exception.message ?: "Unable to load User Profile"
                        }

                    )

                }
            },

            onFail = {
                exception -> errorMessage = exception.message ?: "Unable to identify this device"
            }

        )

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = if (isEditing) {
                "Edit User Profile"
            } else {
                "Create User Profile"
            }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text("Name")
            }
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text("Email")
            }
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = {
                Text("Phone Number")
            }
        )

        Button(
            onClick = {
                notificationsEnabled = !notificationsEnabled
            },
                colors = if (notificationsEnabled) {
                    ButtonDefaults.buttonColors(containerColor = Color.Green)
                } else {
                    ButtonDefaults.buttonColors(containerColor = Color.Red)
            }
        ) {
            Text(
                if (notificationsEnabled) {
                    "Notifications Enabled"
                } else {
                    "Notifications Disabled"
                }
            )
        }

        Button(
            onClick = {

                val id = deviceId

                if (id == null) {
                    errorMessage = "Unable to identify this device"
                } else if (isEditing) {

                    UserRepository.updateUser(
                        deviceId = id,
                        name = name,
                        email = email,
                        phoneNumber = phoneNumber,
                        notificationsEnabled = notificationsEnabled,
                        onSuccess = {
                            navController.popBackStack()
                        },
                        onFail = {
                            exception -> errorMessage = exception.message ?: "Unable to update User Profile"
                        }
                    )
                } else if (selectedRole != null) {

                    val user = User(
                        deviceId = id,
                        name = name,
                        email = email,
                        phoneNumber = phoneNumber,
                        notificationsEnabled = notificationsEnabled,
                        roles = listOf(selectedRole)
                    )

                    UserRepository.createUser(

                        user = user,

                        onSuccess = {

                            if (selectedRole == "ENTRANT") {
                                navController.navigate("EntrantHome") {

                                    popUpTo("EditProfile") {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }

                            if (selectedRole == "OWNER") {
                                navController.navigate("CampsiteOwnerHome/${deviceId}") {

                                    popUpTo("EditProfile") {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }
                        },

                        onFail = { exception ->
                            errorMessage = exception.message ?: "Unable to create User Profile"
                        }
                    )

                }
            }
        ) {
            Text(
                if (isEditing) {
                    "Save Changes"
                } else {
                    "Create Profile"
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = {
                policyExpanded = !policyExpanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (policyExpanded) {
                    "Hide App Policy and Guidelines"
                } else {
                    "View App Policy and Guidelines"
                }
            )
        }

        if (policyExpanded) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = """
                        
                        Thank you for using QuackCamps!
                        The only rule is to have fun!
                        
                    """.trimIndent(),
                    modifier = Modifier.padding(12.dp)
                )
            }

        }

        Spacer(Modifier.height(80.dp))

        if (isEditing) {

            Button (
                onClick = {

                    val id = deviceId

                    if (id != null) {

                        UserRepository.deleteUser(

                            deviceId = id,

                            onSuccess = {
                                navController.navigate("UserStart") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true

                                }
                            },

                            onFail = {
                                exception -> errorMessage = exception.message ?: "Unable to delete User Profile"
                            }

                        )

                    }

                },

                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)

            ) {
                Text("Delete Profile")
            }
        }

    }

}

// [US 01.10.01] As an entrant, I want to opt out of receiving notification
// [US 01.11.01] As an entrant, I want to see the app policy and guidelines
