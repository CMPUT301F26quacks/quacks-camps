package com.example.quackapp.View

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.quackapp.Controller.DeviceIdGetter
import com.example.quackapp.Controller.UserRepository
import com.example.quackapp.Model.User
import com.example.quackapp.R

/**
 * Starting screen for all unregistered users.
 *
 * Gives user the option to register their device as either an Entrant or Campsite Owner.
 *
 * @param navController Allows user to navigate to Entrant or Campsite Home.
 */
@Composable
fun UserStart(navController: NavController) {

    // Checks for deviceID to determine if new user at app start
    //var currentUser by remember {
    //    mutableStateOf<Boolean?>(null) // null means Firebase is still checking
    //}

    // Stores error message if Firebase cannot complete the search
    var userSearchErrorMessage by remember { mutableStateOf<String?>(null) }

    var currentDeviceId by remember { mutableStateOf<String?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    var userSearchFinished by remember { mutableStateOf<Boolean>(false) }

    //
    LaunchedEffect(Unit) {

        DeviceIdGetter.getDeviceId(

            onSuccess = {
                deviceId ->

                currentDeviceId = deviceId // saves FID to use for directing to registration

                UserRepository.getUser(
                    deviceId = deviceId,
                    onResult = { user ->
                        currentUser = user
                        userSearchFinished = true
                    },

                    onFail = { exception ->
                        userSearchErrorMessage = "${exception.javaClass.simpleName}: ${exception.message}"
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
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Checking device ID...")
                CircularProgressIndicator(
                    modifier = Modifier.width(16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        currentUser != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Signing in...")
                CircularProgressIndicator(
                    modifier = Modifier.width(16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }


        else -> {

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                currentUser?.let { Text("Welcome back ${it.deviceId}") }

                Image(
                    painter = painterResource(id = R.drawable.duck),
                    contentDescription = "Logo",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "quackSites",
                    fontSize = 24.sp,
                    color = Color(0xFFF2BA00)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("CreateEntrantProfile")

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
                        navController.navigate("CreateOwnerProfile")
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
    }

    LaunchedEffect(currentUser) {

        val user = currentUser ?: return@LaunchedEffect

        when {
            "ENTRANT" in user.roles && "OWNER" !in user.roles -> {
                navController.navigate("EntrantHome") {
                    popUpTo("UserStart") {
                        inclusive = true
                    }
                }
            }

            "OWNER" in user.roles && "ENTRANT" !in user.roles -> {
                navController.navigate("CampsiteOwnerHome") {
                    popUpTo("UserStart") {
                        inclusive = true
                    }
                }
            }
        }

    }
}