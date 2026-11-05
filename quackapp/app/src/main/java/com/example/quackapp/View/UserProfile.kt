package com.example.quackapp.View

import androidx.compose.foundation.layout.Column
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

    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            .padding(20.dp)
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

                val id = deviceId

                if (id != null && selectedRole != null) {

                    val user = User(
                        deviceId = id,
                        name = name,
                        email = email,
                        phoneNumber = phoneNumber,
                        roles = listOf(selectedRole)
                    )

                    UserRepository.createUser(

                        user = user,

                        onSuccess = {

                            if (selectedRole == "ENTRANT") {
                                navController.navigate("EntrantHome")
                            }

                            if (selectedRole == "OWNER") {
                                navController.navigate("OwnerHome")
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
    }

}

// TODO: button to toggle notifications, delete profile, display app policy/guidelines
// [US 01.10.01] As an entrant, I want to opt out of receiving notification
// [US 01.11.01] As an entrant, I want to see the app policy and guidelines
