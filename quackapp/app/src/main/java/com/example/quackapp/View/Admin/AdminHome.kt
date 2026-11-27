package com.example.quackapp.View.Admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.AdminController
import com.example.quackapp.R

/**
 * Starting screen for admins (matches the "Admin Home" frame in the Halfway UI mockup).
 *
 * Shows system stats and buttons to each admin tool, plus "Switch Profile" so the admin
 * can use the app as an entrant or campsite owner with the same profile (US 03.11.01).
 *
 * Outstanding: "Manage User Profiles", "Manage Messages" and "All Notifications" are other
 * members' stories (US 03.02/03.05, 03.12, 03.08). They are disabled until those routes exist;
 * wire them by passing a lambda in [AdminHome].
 *
 * @param navController used to open admin tools and switch profiles.
 * @param viewModel provides campsite and user counts.
 */
@Composable
fun AdminHome(
    navController: NavController,
    viewModel: AdminController = viewModel()
) {
    val campsites by viewModel.campsites.collectAsState()
    val userCount by viewModel.userCount.collectAsState()

    AdminScaffold(navController) { innerPadding ->
        AdminHomeContent(
            modifier = Modifier.padding(innerPadding),
            campCount = campsites.size,
            userCount = userCount,
            onManageCampsites = { navController.navigate("AdminCampsites") },
            onManageMedia = { navController.navigate("AdminImageGallery") },
            onManageUsers = { navController.navigate("AdminProfiles") },
            onSwitchProfile = { route -> navController.navigate(route) }
        )
    }
}

/**
 * Stateless body of the Admin Home screen, so it can be intent tested without Firebase.
 *
 * @param campCount number of campsites in the app.
 * @param userCount number of user profiles in the app.
 * @param onManageCampsites opens Manage Campsites, or null to disable the button.
 * @param onManageUsers opens Manage User Profiles, or null to disable the button.
 * @param onManageMedia opens the Image Gallery, or null to disable the button.
 * @param onManageMessages opens message logs, or null to disable the button.
 * @param onAllNotifications opens notification logs, or null to disable the button.
 * @param onSwitchProfile called with the home route of the chosen profile ("EntrantHome" or "OwnerHome").
 */
@Composable
fun AdminHomeContent(
    modifier: Modifier = Modifier,
    campCount: Int,
    userCount: Int,
    onManageCampsites: (() -> Unit)? = null,
    onManageUsers: (() -> Unit)? = null,
    onManageMedia: (() -> Unit)? = null,
    onManageMessages: (() -> Unit)? = null,
    onAllNotifications: (() -> Unit)? = null,
    onSwitchProfile: (String) -> Unit
) {
    var showSwitchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.duck),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Crop
        )
        Text(text = "quackSites", fontSize = 24.sp, color = AdminYellow)

        // System Stats Overview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AdminYellow, RoundedCornerShape(24.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("System Stats Overview", color = Color.White, fontWeight = FontWeight.Bold)
            Text("$campCount Camps    $userCount Users", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        AdminMenuButton("Manage Campsites", onManageCampsites)
        AdminMenuButton("Manage User Profiles", onManageUsers)
        AdminMenuButton("Manage Uploaded Media", onManageMedia)
        AdminMenuButton("Manage Messages", onManageMessages)
        AdminMenuButton("All Notifications", onAllNotifications)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { showSwitchDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AdminBlue, contentColor = Color.White)
        ) {
            Text("Switch Profile")
        }
    }

    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = { Text("Switch Profile") },
            text = { Text("Use the app as an entrant or a campsite owner. You can switch back to Admin from your Profile page.") },
            confirmButton = {
                TextButton(onClick = {
                    showSwitchDialog = false
                    onSwitchProfile("EntrantHome")
                }) { Text("Entrant") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSwitchDialog = false
                    onSwitchProfile("OwnerHome")
                }) { Text("Campsite Owner") }
            }
        )
    }
}

/**
 * Orange, rounded admin menu button from the mockup.
 *
 * @param label button text.
 * @param onClick action, or null to show the button disabled.
 */
@Composable
private fun AdminMenuButton(label: String, onClick: (() -> Unit)?) {
    Button(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = AdminOrange, contentColor = Color.White)
    ) {
        Text(label)
    }
}
