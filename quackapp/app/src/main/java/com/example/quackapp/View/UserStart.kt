package com.example.quackapp.View

import com.example.quackapp.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/**
 * Starting screen for all unregistered users.
 *
 * Gives user the option to register their device as either an Entrant or Campsite Owner.
 *
 * @param navController Allows user to navigate to Entrant or Campsite Home.
 */
@Composable
fun UserStart(navController: NavController) {


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.duck),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "quackSites",
            fontSize = 24.sp,
            color = Color(0xFFF2BA00)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("EntrantHome")
            },
            colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF2BA00),
                    contentColor = Color(0xFFFFFFFF)
            )

        ) {
            Text("I want to register this \n" +
                    "device as an Entrant ")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("CampsiteOwnerHome")
            },

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF16B00),
                contentColor = Color(0xFFFFFFFF)

            )

        ) {
            Text("I want to register \n" +
                    "this device as a \n" +
                    "Campsite Owner")
        }
    }
}