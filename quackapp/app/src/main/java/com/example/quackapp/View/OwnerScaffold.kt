package com.example.quackapp.View

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.quackapp.Model.NavBarLists

/**
 * Provides Navigation Bar to all Owner Screens that use it
 *
 * Adds the navigation bar (NavBarLists.owner) to the bottom of screens that use OwnerScaffold.
 *
 * @param navController Used to allow navigation between screens
 * @param content Applies Padding
 */
@Composable
fun OwnerScaffold(
    navController: NavController,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = { NavBar(navController, NavBarLists.owner) },
        content = content
    )
}