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

@Composable
private fun RowScope.AddItem(
    screen: NavBarItem,
    currentRoute: String?,
    navController: NavController
) {
    NavigationBarItem(
        label = { Text(screen.name) },
        icon = { Icon(imageVector = screen.image, contentDescription = screen.name) },
        selected = currentRoute == screen.sendsTo,
        alwaysShowLabel = true,
        onClick = {
            if (currentRoute != screen.sendsTo) {
                navController.navigate(screen.sendsTo) {
                    popUpTo("UserStart") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    )
}