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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.RegisterCampsiteController
import com.example.quackapp.Controller.SaveStatus

@Composable
fun RegisterCampsiteScreen(
    navController: NavController,
    viewModel: RegisterCampsiteController = viewModel()
) {
    // String version of these two fields since
    // displaying ints can crash the app when no number is there
    var priceAsString: String by remember { mutableStateOf("") }
    var capacityAsString: String by remember { mutableStateOf("") }

    var newTag: String by remember { mutableStateOf("") }
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
                text = "Register Campsite",
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
                .testTag("capacityField")
        )
        Text("Cancellation Policy")
        OutlinedTextField(
            value = viewModel.cancellationPolicy,
            onValueChange = { viewModel.cancellationPolicy = it },
            label = { Text("Policy") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cancelPolicyField")
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
        Box(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .testTag("imgUploadArea"),
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
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                viewModel.addCustomTag(newTag)
                newTag = ""
            },
            colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
            enabled = newTag.isNotBlank(),
            modifier = Modifier.testTag("addTagButton")
        ) {
            Text("Add")
        }

        (status as? SaveStatus.Error)?.let {
            Text(
                it.message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp
            )
        }
        Button(
            onClick = { viewModel.addCampsite() },
            enabled = status !is SaveStatus.Saving,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submitButton"),
            colors = ButtonDefaults.buttonColors(Color(0xFFFF9800)),
            ) {
            if (status is SaveStatus.Saving) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Submit")
            }
        }


    }
}
@Composable
fun Tag(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFFFFC107) else Color.Gray,
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}