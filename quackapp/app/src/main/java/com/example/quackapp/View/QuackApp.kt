package com.example.quackapp.View

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quackapp.Model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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

        composable("EntrantCampsiteDetailScreen/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")?: ""  //get campsite id
            var campsite by remember {mutableStateOf<Campsite?>(null)}  //find the campsite

            LaunchedEffect(campsiteId){ //look for the campsite with firebase!
                if(campsiteId.isNotBlank()){
                    Firebase.firestore.collection("campsites")
                        .document(campsiteId)
                        .get()
                        .addOnSuccessListener { document ->
                            campsite=document.toObject(Campsite::class.java)
                        }
                }
            }
            //show entrant campsite detail screen only if campsite not null aka campsite found
            campsite?.let {loadedCampsite ->
                EntrantCampsiteDetailScreen(navController, campsite = loadedCampsite)
            }

        }

    }
}

