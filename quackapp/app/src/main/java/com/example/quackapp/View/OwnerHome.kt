package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.CampsiteRepository
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.tasks.await

@Composable
fun OwnerHome(
    navController: NavController,
    viewModel: OwnerHomeController = viewModel()
) {
    val camps by viewModel.camps.collectAsState()

    val testCamp = Campsite(
        campId = "LlBvRnNrvbH1N0xrw0sJ",
        cancellationPolicy = "",
        capacity = 2,
        features = listOf("Water"),
        location = "lo",
        name = "Test",
        optionalPromoImage = "",
        price = 12,
        rules = listOf(""),
        tags = listOf("Smoke Free")
    )
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ){

        // title text
        Text(
            text = "Campsites",
            fontSize = 40.sp
        )

        FloatingActionButton(
            containerColor = Color(0xFFFF9800),
            contentColor = Color.White,
            onClick = { navController.navigate("RegisterCampsite") }
        ) {
            Text(
                "+",
                fontSize = 18.sp,

            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (camps.isEmpty()){
            Text("No campsites registered")
        }
        else {
            // TODO: modify this to accomodate our campsite model class
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                camps.forEach { campsite ->
                    CampRow(campsite)
                }
            }
        }
        Button(
            onClick = {
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
            modifier = Modifier.testTag("backButton") // for testing
        ) {
            Text("Back")
        }
    }
}

// TODO: Make a function to display each campsite as a row according to the UI mockup

@Composable
fun CampRow(campsite: Campsite){
    Row(modifier = Modifier.fillMaxWidth()) {
        if (campsite.optionalPromoImage != ""){
            AsyncImage(
                model = campsite.optionalPromoImage,
                contentDescription = campsite.name,
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp),
                contentScale = ContentScale.Crop
            )
        }
        else{
            Box(modifier = Modifier
                .height(50.dp)
                .width(50.dp)){
                Text("No image provided")
            }
        }
        Text(
            campsite.name,
            fontSize = 24.sp
            )
    }
}