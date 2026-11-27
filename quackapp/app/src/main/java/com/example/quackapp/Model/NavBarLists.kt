package com.example.quackapp.Model


/**
 * An object containing lists of icons that form a navigation bar.
 *
 * Inside the object, there are different lists that can be called on to bring in a set of icons.
 * Each NavBarItem already has a predetermined screen it will send you to upon being clicked.
 */
object NavBarLists {

    val entrant = listOf(
        NavBarItem.EntrantHome,
        NavBarItem.EntrantActiveCampsites,
        NavBarItem.UserChats,
        NavBarItem.EntrantProfile
    )

    val owner = listOf(
        NavBarItem.OwnerHome,
        NavBarItem.OwnerReservationHistory,
        NavBarItem.OwnerProfile
    )

    // Admin message/notification logs (US 03.08.01, 03.12.01) can add a middle icon here
    val admin = listOf(
        NavBarItem.AdminHome,
        NavBarItem.AdminProfile
    )
}