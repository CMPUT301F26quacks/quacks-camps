package com.example.quackapp.View

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.EditCampsiteController
import com.example.quackapp.Controller.SaveStatus

@Composable
fun EditCampsiteScreen(
    navController: NavController,
    viewModel: EditCampsiteController = viewModel()
) {
    var newTag: String by remember { mutableStateOf("") }
    val status by viewModel.status.collectAsState()
    // launcher for the photo selector
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.imageUri = uri }
    // when the screen is rebuilt, if there was a successful save, go back to previous screen
    LaunchedEffect(status) {
        if (status is SaveStatus.Success){
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,

        ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ){
            Text(
                text = "Edit Campsite",
                fontSize = 32.sp
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
        Text("* signifies required field", color = Color.Red)
        Text(text = buildAnnotatedString {
            append("Name ")
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        }
        )
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Campsite Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("nameField")
        )
        Text(text = buildAnnotatedString {
            append("Location ")
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        }
        )
        OutlinedTextField(
            value = viewModel.location,
            onValueChange = { viewModel.location = it },
            label = { Text("Location") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("locationField")
        )
        Text(text = buildAnnotatedString {
            append("Price ")
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        }
        )
        OutlinedTextField(
            value = viewModel.price,
            onValueChange = { input ->
                if (input.isDigitsOnly() && input.isNotEmpty()) {
                    viewModel.price = input
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Price per night") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("priceField")
        )
        Text(text = buildAnnotatedString {
            append("Capacity ")
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        }
        )
        OutlinedTextField(
            value = viewModel.capacity,
            onValueChange = { input ->
                if (input.isDigitsOnly() && input.isNotEmpty()) {
                    viewModel.capacity = input
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Max Capacity") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("capacityField")
        )
        Text("Cancellation Policy")
        OutlinedTextField(
            value = viewModel.cancellationPolicy,
            onValueChange = { viewModel.cancellationPolicy = it },
            label = { Text("Policy") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Text("Amenities/Features")
        Column() {
            viewModel.features.forEachIndexed { index, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { viewModel.updateFeature(index, it) },
                        label = { Text("Amenity #${index + 1}") },
                        modifier = Modifier.testTag("amenity${index + 1}")
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.removeFeature(index) },
                        enabled = viewModel.features.size > 1,
                        colors = ButtonDefaults.buttonColors(Color(0xFFFF0000)),
                        modifier = Modifier.testTag("deleteAmenity${index + 1}")
                    )
                    { Text("X") }
                }
            }
            Button(
                onClick = {
                    viewModel.addFeature()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("addAmenity"),
                colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
            ) {
                Text("Add Amenity")
            }
        }
        Text("Rules")
        Column() {
            viewModel.rules.forEachIndexed { index, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { viewModel.updateRule(index, it) },
                        label = { Text("Rule #${index + 1}") },
                        modifier = Modifier.testTag("rule${index+1}")
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.removeRule(index) },
                        enabled = viewModel.rules.size > 1,
                        colors = ButtonDefaults.buttonColors(Color(0xFFFF0000)),
                        modifier = Modifier.testTag("removeRule${index+1}")
                    ) { Text("X") }
                }
            }
            Button(
                onClick = {
                    viewModel.addRule()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("addRule"),
                colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
            ) {
                Text("Add Rule")
            }
        }
        Text("Promotional Image (Optional)")
        // Show the newly picked image if there is one, otherwise the saved one
        val preview: Any? = viewModel.imageUri ?: viewModel.existingImageUrl.ifBlank { null }
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
            if (preview != null) {
                AsyncImage(
                    model = preview,
                    contentDescription = "Promo image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Text("Tap to choose an image")
            }
        }
        Text("Tags")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.tags.forEach { tag ->
                Tag(
                    modifier = Modifier.testTag("tag${tag}"),
                    tag,
                    tag in viewModel.selectedTags,
                    onClick = { viewModel.toggleTag(tag) }
                )
            }
        }
        OutlinedTextField(
            value = newTag,
            onValueChange = { newTag = it },
            label = { Text("Add your own tag") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                viewModel.addCustomTag(newTag)
                newTag = ""
            }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("newTagField")
        )
        (status as? SaveStatus.Error)?.let {
            Text(
                it.message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp
            )
        }
        Button(
            onClick = { viewModel.saveChanges() },
            enabled = status !is SaveStatus.Saving,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("saveButton"),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
        ) {
            if (status is SaveStatus.Saving) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Save")
            }
        }
    }
}