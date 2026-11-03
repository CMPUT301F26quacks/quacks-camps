package com.example.quackapp.View

import android.R.id.input
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.quackapp.View.RegisterCampsiteScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.RegisterCampsiteController
import com.example.quackapp.Controller.SaveStatus

// TODO: Find a way to detect who the owner is when registering campsites
@Composable
fun RegisterCampsiteScreen(
    navController: NavController,
    viewModel: RegisterCampsiteController = viewModel()
) {
    // String version of these two fields since
    // displaying ints can crash the app when no number is there
    var priceAsString: String by remember { mutableStateOf("") }
    var capacityAsString: String by remember { mutableStateOf("") }

    val status by viewModel.status.collectAsState()

    // launcher for the photo selector
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.imageUri = uri }
    // when the screen is rebuilt, if there was a successful upload
    // reset the form
    LaunchedEffect(status) {
        if (status is SaveStatus.Success){
            viewModel.resetForm()
            priceAsString = ""
            capacityAsString = ""
        }
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ){
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Campsite Name") },
            modifier = Modifier
                .fillMaxWidth()
//                .weight(1f)
        )
        OutlinedTextField(
            value = viewModel.location,
            onValueChange = { viewModel.location = it },
            label = { Text("Location") },
            modifier = Modifier
                .fillMaxWidth()
//                .weight(1f)
        )
        OutlinedTextField(
            value = priceAsString,
            onValueChange = { input ->
                if (input.isDigitsOnly()) {
                    priceAsString = input
                    if (input.isNotEmpty()){
                        viewModel.price = input.toInt()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Price per night") },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = capacityAsString,
            onValueChange = { input ->
                if (input.isDigitsOnly()) {
                    capacityAsString = input
                    if (input.isNotEmpty()){
                        viewModel.capacity = input.toInt()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Max Capacity") },
            modifier = Modifier
                .fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.imageUri != null) {
                AsyncImage(
                    model = viewModel.imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Text("Tap to upload an image")
                }
            }
        }
            Button(
                onClick = { viewModel.addCampsite() },
                enabled = status !is SaveStatus.Saving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
                ) {
                if (status is SaveStatus.Saving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit")
                }
            }

            (status as? SaveStatus.Error)?.let {
                Text(it.message, color = MaterialTheme.colorScheme.error)
            }
    }
}