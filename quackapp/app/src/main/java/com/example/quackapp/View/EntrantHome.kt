package com.example.quackapp.View

package com.example.quackapp.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController


/**
 * Entrant Home Screen
 *
 * TO-DO: Has list of campsites you view, filter, or search for a campsite.
 *
 * @param navController allows you to move between screens using navigation bar
 */
@Composable
fun EntrantHome(navController: NavController) {

    EntrantScaffold(navController) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text("Entrant Home")

        }
    }
}