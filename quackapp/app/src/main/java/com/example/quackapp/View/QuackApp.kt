package com.example.quackapp.View

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Sets up navigation between screens for whole app
 *
 * The app starts on UserStart. It contains routes that route buttons or clickable objects to
 * different screens.
 */
@Composable
fun QuackApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "UserStart") {
        composable("UserStart") { UserStart(navController) }

        composable("EntrantHome") { EntrantHome(navController) }
        composable("EntrantActiveCampsites") { EntrantActiveCampsites(navController) }

        composable("CampsiteOwnerHome") { OwnerHome(navController) }
        composable("RegisterCampsite") { RegisterCampsiteScreen(navController) }

        composable("CreateEntrantProfile") {
            UserProfile(
                navController = navController,
                selectedRole = "ENTRANT"
            )
        }
        composable("CreateOwnerProfile") {
            UserProfile(
                navController = navController,
                selectedRole = "OWNER"
            )
        }
        composable("EditProfile") {
            UserProfile(
                navController = navController,
                isEditing = true
            )
        }



    }
}

