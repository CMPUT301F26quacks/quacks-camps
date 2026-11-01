package com.example.quackapp.View

import android.R.id.input
import com.example.quackapp.View.RegisterCampsiteScreen
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController

@Composable
fun RegisterCampsiteScreen(navController: NavController) {
    var name: String by remember {mutableStateOf("")}
    var location: String by remember {mutableStateOf("")}
    var price: Int by remember { mutableIntStateOf(0) }
    var capacity: Int by remember { mutableIntStateOf(0) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ){

        // title text
        Text(
            text = "Register Campsite",
            fontSize = 30.sp
        )

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Campsite Name") },
//            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
//            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = price.toString(),
            onValueChange = { input ->
                if (input.isDigitsOnly()) {
                    price = input.toInt()
                }
            },
            label = { Text("Price") },
//            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = capacity.toString(),
            onValueChange = { input ->
                if (input.isDigitsOnly()) {
                    capacity = input.toInt()
                }
            },
            label = { Text("Max Capacity") },
//            modifier = Modifier.weight(1f)
        )

    }
}