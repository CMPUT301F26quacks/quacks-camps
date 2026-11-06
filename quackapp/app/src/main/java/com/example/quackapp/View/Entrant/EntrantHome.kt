package com.example.quackapp.View.Entrant
import android.net.Uri
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
 * Contains the EntrantHomeList and Navigation Bar
 *
 * @param navController allows you to move between screens using navigation bar
 * @param controller brings in all the campsites to be displayed for the Entrant
 */
@Composable
fun EntrantHome(
    navController: NavController,
    controller: EntrantHomeController = viewModel()
) {

    val campsites by controller.camps.collectAsState()

    //EntrantScaffold provides the Entrant Navigation Bar
    EntrantScaffold(navController) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //Takes all the campsites from the firebase and inputs them into EntrantHomeList
            EntrantHomeList(
                campsites = campsites,
                //When a campsite card is clicked, send them to the campsite details screen
                campsiteCardClicked = { campsite ->
                    if (campsite.campId.isNotBlank()) {
                        navController.navigate(
                            "EntrantDetailedCampsite/${Uri.encode(campsite.campId)}"
                        )
                    }
                },
            )
        }
    }
}

/**
 * Handle scanned QR codes and navigate to the corresponding campsite
 * @param scannedCode: link of the scanned QR Code
 * @param navController: allows you to move between screens using navigation bar
 */
fun scannedQRCode(scannedCode: String, navController: NavController){
    val cleanedCode = scannedCode.trim()
    val campId = when {
        cleanedCode.contains("campsite/") -> cleanedCode.substringAfter("campsite/")
        else -> cleanedCode
    }

    if(campId.isNotBlank()) {
        navController.navigate("EntrantDetailedCampsite/${Uri.encode(campId)}")
    }

}