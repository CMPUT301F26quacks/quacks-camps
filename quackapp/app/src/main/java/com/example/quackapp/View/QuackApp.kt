package com.example.quackapp.View

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quackapp.View.Entrant.EntrantHome
import androidx.navigation.navArgument
import com.example.quackapp.View.Entrant.EntrantDetailedCampsite
import com.example.quackapp.Model.ListCampInLotteryRoute
import com.example.quackapp.View.Entrant.EntrantActiveCampsites
import com.example.quackapp.View.Admin.AdminHome
import com.example.quackapp.View.Admin.AdminImageGallery
import com.example.quackapp.View.Admin.AdminCampsites
import com.example.quackapp.View.Admin.AdminCampsiteReviews
import com.example.quackapp.View.Owner.OwnerCampDetails
import com.example.quackapp.View.Owner.OwnerEditCamp
import com.example.quackapp.View.Owner.OwnerEditLottery
import com.example.quackapp.View.Owner.OwnerCreateLottery
import com.example.quackapp.View.Owner.OwnerHome
import com.example.quackapp.View.Owner.OwnerReservationHistory
import com.example.quackapp.View.Owner.OwnerCreateCamp
import com.example.quackapp.View.Admin.AdminProfilesScreen

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
            route = "EntrantDetailedCampsite/{campId}",
            arguments = listOf(navArgument("campId") { type = NavType.StringType })
        ) {
            EntrantDetailedCampsite(navController = navController)
        }

        composable("OwnerHome") {
            OwnerHome(
                navController
            )
        }

        composable(
            route = "CampsiteOwnerHome/{ownerId}",
            arguments = listOf(navArgument("ownerId") { type = NavType.StringType })
        ) { OwnerHome(navController) }

        composable(
            route = "RegisterCampsite/{ownerId}",
            arguments = listOf(navArgument("ownerId") { type = NavType.StringType })
        )
        { OwnerCreateCamp(navController) }

        composable(
            route = "CampsiteDetails/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType }))
        { OwnerCampDetails(navController) }

        composable(
            route = "EditCampsite/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
        ) { OwnerEditCamp(navController) }

        composable<ListCampInLotteryRoute> { entry ->
            OwnerCreateLottery(navController)
        }

        composable(
            route = "editLottery/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
        ) { OwnerEditLottery(navController) }



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
        composable("OwnerProfile") {
            UserProfile(
                navController = navController,
                isEditing = true
            )
        }
        composable("OwnerReservationHistory") {
            OwnerReservationHistory(
                navController = navController
            )
        }

        composable("AdminHome") { AdminHome(navController) }
        composable("AdminImageGallery") { AdminImageGallery(navController) }
        composable("AdminCampsites") { AdminCampsites(navController) }
        composable("AdminProfiles") { AdminProfilesScreen(navController) }
        composable(
            route = "AdminCampsiteReviews/{campId}",
            arguments = listOf(navArgument("campId") { type = NavType.StringType })
        ) {
            AdminCampsiteReviews(navController)
        }
    }
}

