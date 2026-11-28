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
import com.example.quackapp.Model.Admin
import com.example.quackapp.Model.User
import androidx.compose.material3.TextButton

import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton

import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.clip

import coil.compose.AsyncImage
import com.example.quackapp.View.Admin.AdminScaffold
import com.example.quackapp.View.Entrant.EntrantScaffold

import com.example.quackapp.Controller.UserProfileValidator

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import com.example.quackapp.View.Owner.OwnerScaffold

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
 * US 03.11.01 - Admins see "Switch back to Admin" after switching to an entrant/owner profile.
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

    var profileImageUrl by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var policyExpanded by remember { mutableStateOf<Boolean>(false) }

    var nameError by remember { mutableStateOf<String?>(null) }

    var emailError by remember { mutableStateOf<String?>(null) }

    var phoneError by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {

        DeviceIdGetter.getDeviceId(

            onSuccess = {
                id -> deviceId = id

                if (isEditing) {

                    UserRepository.getUser(

                        deviceId = id,

                        onResult = { user ->
                            // loading an existing user
                            if (user != null) {
                                name = user.name
                                email = user.email
                                phoneNumber = user.phoneNumber
                                notificationsEnabled = user.notificationsEnabled
                                userRoles = user.roles
                                profileImageUrl = user.profileImageUrl
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

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->

        if (uri != null) {
            selectedImageUri = uri
        }

    }


    val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AsyncImage(
                model = selectedImageUri ?: profileImageUrl,
                contentDescription = "User profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(15.dp))
            Button(
                onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            ) {
                Text(
                    if (profileImageUrl.isBlank()) {
                        "Add Profile Picture"
                    } else {
                        "Change Profile Picture"
                    }
                )
            }

            Text(
                text = if (isEditing) {
                    "Edit User Profile"
                } else {
                    "Create User Profile"
                }
            )

            // US 03.11.01: an admin using the app as an entrant/owner can go back to admin tools
            if (isEditing && Admin.isAdmin(userRoles)) {
                TextButton(
                    onClick = {
                        navController.navigate("AdminHome") {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Switch back to Admin", color = Color(0xFF2E7D32))
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Name") },
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) }},
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError != null,
                supportingText = { emailError?.let {Text(it)}},
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    phoneError = null
                },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it) }},
                modifier = Modifier.fillMaxWidth()
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

                    nameError = UserProfileValidator.validateName(name)
                    emailError = UserProfileValidator.validateEmail(email)
                    phoneError = UserProfileValidator.validatePhoneNumber(phoneNumber)

                    if (
                        nameError != null ||
                        emailError != null ||
                        phoneError != null
                    ) {
                        return@Button
                    }

                    val id = deviceId

                    if (id == null) {
                        errorMessage = "Unable to identify this device"
                    } else if (isEditing) {

                        val imageUri = selectedImageUri

                        if (imageUri != null) {
                            // uploading a new image before saving the profile
                            UserRepository.uploadProfilePic(
                                deviceId = id,
                                imageUri = imageUri,

                                onSuccess = { downloadUrl ->

                                    UserRepository.updateUser(
                                        deviceId = id,
                                        name = name,
                                        email = email,
                                        phoneNumber = phoneNumber,
                                        notificationsEnabled = notificationsEnabled,
                                        profileImageUrl = downloadUrl,

                                        onSuccess = {
                                            navController.popBackStack()
                                        },

                                        onFail = { exception ->
                                            errorMessage =
                                                exception.message ?: "Unable to update User Profile"
                                        }
                                    )
                                },

                                onFail = { exception ->
                                    errorMessage =
                                        exception.message ?: "Unable to upload profile picture"
                                }
                            )

                        } else {
                            UserRepository.updateUser(
                                deviceId = id,
                                name = name,
                                email = email,
                                phoneNumber = phoneNumber,
                                notificationsEnabled = notificationsEnabled,
                                profileImageUrl = profileImageUrl,

                                onSuccess = {
                                    //navController.popBackStack()
                                },
                                onFail = { exception ->
                                    errorMessage =
                                        exception.message ?: "Unable to update User Profile"
                                }
                            )
                        }

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

            Spacer(Modifier.height(10.dp))


            if (isEditing) {
                Button(
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

                                onFail = { exception ->
                                    errorMessage =
                                        exception.message ?: "Unable to delete User Profile"
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

    //Gets the right Scaffold for the user editing their profile
    if (isEditing && selectedRole == "ENTRANT") {
        EntrantScaffold(navController) { content(it) }
    } else if (isEditing && selectedRole == "OWNER") {
        OwnerScaffold(navController) { content(it) }
    } else if (isEditing && selectedRole == "ADMIN") {
        AdminScaffold(navController) { content(it) }
    } else {
        Scaffold { content(it) }
    }
}