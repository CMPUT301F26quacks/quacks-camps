package com.example.quackapp.Model

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ManageAccounts
sealed class NavBarItem(
    val name: String,
    val image: ImageVector,

){
    object Home:
        NavBarItem("Home",Icons.Filled.Home)

    object ActiveCampsites:
        NavBarItem("Active Campsites", Icons.Filled.Bookmark)

    object Profile:
        NavBarItem("Profile", Icons.Filled.ManageAccounts)

}


