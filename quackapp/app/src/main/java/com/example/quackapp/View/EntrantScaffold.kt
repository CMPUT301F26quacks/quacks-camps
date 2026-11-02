package com.example.quackapp.View

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.quackapp.Model.NavBarLists

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