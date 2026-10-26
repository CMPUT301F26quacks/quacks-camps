package com.example.quackapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun quackApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "UserStart") {
        composable("UserStart") { UserStart(navController) }
        composable("EntrantHome") { EntrantHome(navController) }
        composable("CampsiteOwnerHome") { CampsiteOwnerHome(navController) }

    }
}

