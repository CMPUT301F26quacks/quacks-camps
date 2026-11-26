package com.example.quackapp.Model

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ManageAccounts

/**
 * A class of navigation bar objects.
 *
 * This class contains objects that can be added to a navigation bar on the bottom of a screen.
 *
 * @property name the name of the icon object.
 * @property image the displayed icon from material.icons.
 * @property sendsTo when this icon is clicked, it will send the user to the screen in sendsTo.
 */
sealed class NavBarItem(
    val name: String,
    val image: ImageVector,
    val sendsTo: String
){
    object EntrantHome:
        NavBarItem(
            "Home",
            Icons.Filled.Home,
            "EntrantHome")
    object EntrantActiveCampsites:
        NavBarItem(
            "Reservations",
            Icons.Filled.Bookmark,
            "EntrantActiveCampsites")
    object EntrantProfile:
        NavBarItem(
            "Profile",
            Icons.Filled.ManageAccounts,
            "EditProfile")

    object UserChats:
        NavBarItem(
            "Messages",
            Icons.AutoMirrored.Filled.Chat,
            "UserChats")

    object OwnerHome:
        NavBarItem("Home",
                   Icons.Filled.Home,
                   "OwnerHome")

    object OwnerReservationHistory:
        NavBarItem("Reservation History",
                   Icons.Filled.Bookmark,
                   "OwnerReservationHistory"
        )

    object OwnerProfile:
        NavBarItem("Profile",
                   Icons.Filled.ManageAccounts,
                   "OwnerProfile")

    object AdminHome:
        NavBarItem("Home",
                   Icons.Filled.Home,
                   "AdminHome")

    object AdminProfile:
        NavBarItem("Profile",
                   Icons.Filled.ManageAccounts,
                   "EditProfile")
}



