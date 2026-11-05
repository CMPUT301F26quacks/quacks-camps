package com.example.quackapp.View.Entrant
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.EntrantHomeController


/**
 * Entrant Home Screen
 *
 * TO-DO: Has list of campsites you view, filter, or search for a campsite.
 *
 * @param navController allows you to move between screens using navigation bar
 */
@Composable
fun EntrantHome(
    navController: NavController,
    controller: EntrantHomeController = viewModel()) {

    val campsites by controller.camps.collectAsState()

    EntrantScaffold(navController) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            EntrantHomeList(
                campsites = campsites,
                campsiteCardClicked = { campsite ->

                }
            )
        }
    }
}