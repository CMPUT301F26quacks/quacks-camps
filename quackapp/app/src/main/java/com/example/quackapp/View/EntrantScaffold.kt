package com.example.quackapp.View

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.quackapp.Model.NavBarLists

/**
 * Provides Navigation Bar to all Entrant Screens that use it
 *
 * Adds the navigation bar (NavBarLists.entrant) to the bottom of screens that use EntrantScaffold.
 *
 * @param navController Used to allow navigation between screens
 * @param topBar Used in EntrantActiveCampsites at the top of the screen
 * @param snackbarHost Used in EntrantActiveCampsites to display multiple clickable tabs
 * @param content Applies Padding
 */
@Composable
fun EntrantScaffold(
    navController: NavController,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        snackbarHost = snackbarHost,
        bottomBar = { NavBar(navController, NavBarLists.entrant) },
        content = content
    )
}