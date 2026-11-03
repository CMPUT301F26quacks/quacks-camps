package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.quackapp.Controller.DeviceIdGetter
import com.example.quackapp.Controller.UserRepository

@Composable
fun UserStart(navController: NavController) {

    var userExists by remember {
        mutableStateOf<Boolean?>(null) // null means Firebase is still checking
    }

    // Stores error message if Firebase cannot complete the search
    var userSearchErrorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Checks the device to get the deviceId when the first screen starts
    LaunchedEffect(Unit) {

        DeviceIdGetter.getDeviceId(

            onSuccess = {
                deviceId -> UserRepository.userExists(
                    deviceId = deviceId,
                    onResult = {
                        exists -> userExists = exists
                    },

                    onFail = {
                        exception -> userSearchErrorMessage = exception.message ?: "Unable to find user profile"
                    }
                )
            },

            onFail = {
                exception -> userSearchErrorMessage = exception.message ?: "Unable to identify this device"
            }
        )
        }
    when {
        userSearchErrorMessage != null -> {
            Text("Error: $userSearchErrorMessage")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "quackSites",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("EntrantHome")
            }

        ) {
            Text("I want to register this \n" +
                    "device as an Entrant ")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("CampsiteOwnerHome")
            }

        ) {
            Text("I want to register \n" +
                    "this device as a \n" +
                    "Campsite Owner")
        }
    }
}