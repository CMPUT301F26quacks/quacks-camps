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
        NavBarItem.EntrantProfile
    )

    //Campsite Owner list of icons wanted for Owner Nav Bar to-do

    /*
    val campsiteOwner = listOf(
        #TO-DO
    )
     */


    //Admin list of icons wanted for Admin Nav Bar to-do

    /*
    val admin = listOf(
        #TO-DO
    )
     */
}