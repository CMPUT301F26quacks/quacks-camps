package com.example.quackapp.View

import android.util.Log
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
import com.example.quackapp.Model.User

@Composable
fun UserStart(navController: NavController) {

    // Checks for deviceID to determine if new user at app start
    //var currentUser by remember {
    //    mutableStateOf<Boolean?>(null) // null means Firebase is still checking
    //}

    // Stores error message if Firebase cannot complete the search
    var userSearchErrorMessage by remember { mutableStateOf<String?>(null) }

    var currentUser by remember { mutableStateOf<User?>(null) }

    var userSearchFinished by remember { mutableStateOf<Boolean>(false) }

    //
    LaunchedEffect(Unit) {

        DeviceIdGetter.getDeviceId(

            onSuccess = {

                deviceId ->
                Log.d("USER_CHECK", "Looking for device ID: $deviceId")
                UserRepository.getUser(
                    deviceId = deviceId,
                    onResult = { user ->
                        currentUser = user
                        userSearchFinished = true
                    },

                    onFail = { exception ->
                        Log.e("USER_CHECK", "Failed to load user", exception)
                        userSearchErrorMessage = exception.message ?: "Unable to load user profile"
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

        !userSearchFinished -> {
            Text("Checking device ID...")
        }

        currentUser != null -> {
            Text("Welcome back ${currentUser!!.name}\n" + "TEST: ${currentUser!!.roles}")
        }

        else -> {
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
    }
}