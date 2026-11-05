package com.example.quackapp.View

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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




        composable(
            route = "CampsiteOwnerHome/{ownerId}",
            arguments = listOf(navArgument("ownerId") { type = NavType.StringType })
        ) {
            OwnerHome(navController)
        }
        composable("RegisterCampsite") { RegisterCampsiteScreen(navController) }
        composable(
            route = "CampsiteDetails/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType }))
        {
            DetailedCampsiteScreen(navController)
        }
        composable(
            route = "EditCampsite/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
        ) {
            EditCampsiteScreen(navController)
        }

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

