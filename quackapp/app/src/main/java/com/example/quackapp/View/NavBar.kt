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

    NavigationBar {
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
    NavigationBarItem(
        label = { Text(screen.name) },
        icon = { Icon(
            imageVector = screen.image,
            contentDescription = screen.name,
            tint = Color(0xFFF16B00)
        ) },
        selected = currentRoute == screen.sendsTo,
        alwaysShowLabel = true,
        onClick = {
            val returned = navController.popBackStack(screen.sendsTo, inclusive = false)
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