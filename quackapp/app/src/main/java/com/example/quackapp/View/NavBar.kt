package com.example.quackapp.View

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quackapp.Model.NavBarItem
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Creates Navigation bar for bottom of a screen
 *
 * Adds the inputted List<NavBarItem> to the created Navigation bar in listed order.
 *
 * @param navController Used to switch screens when an icon is clicked.
 * @param items a list of NavBarItem's to be displayed in the navigation bar.
 */
@Composable
fun NavBar(navController: NavController, items: List<NavBarItem>) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar{
        items.forEach { item ->
            AddItem(item, currentRoute, navController)
        }
    }

}

/**
 * Creates the NavigationBar item
 *
 * Gets the properties of a NavBarItem and formats it to an item ready for NavigationBar.
 *
 * @param screen the NavBarItem to be displayed
 * @param currentRoute Contains the route of the screen being shown.
 * @param navController Used to navigate to the screen inscribed in the clicked icon
 */
@Composable
private fun RowScope.AddItem(
    screen: NavBarItem,
    currentRoute: String?,
    navController: NavController
) {
    //
    val isSelected = currentRoute == screen.sendsTo ||
            (screen == NavBarItem.OwnerHome && currentRoute?.startsWith("CampsiteOwnerHome") == true)

    NavigationBarItem(
        label = { Text(screen.name) },
        icon = { Icon(
            imageVector = screen.image,
            contentDescription = screen.name,
            tint = Color(0xFFF16B00)
        ) },
        selected = isSelected,
        alwaysShowLabel = true,
        onClick = {
            var returned = navController.popBackStack(screen.sendsTo, inclusive = false)
            if (!returned && screen == NavBarItem.OwnerHome) {
                returned = navController.popBackStack("CampsiteOwnerHome/{ownerId}", inclusive = false) //make sure that the backstack includes the owner's Id so it doesn't reroute to a page with no campsiteowner
            }
            if (!returned) {
                navController.navigate(screen.sendsTo) {
                    popUpTo("UserStart") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    )
}