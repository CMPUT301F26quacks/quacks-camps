package com.example.quackapp.View

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quackapp.OwnerHome

@Composable
fun QuackApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "UserStart") {
        composable("UserStart") { UserStart(navController) }
        composable("EntrantHome") { EntrantHome(navController) }
        composable("OwnerHome") { OwnerHome(navController) }

    }
}

