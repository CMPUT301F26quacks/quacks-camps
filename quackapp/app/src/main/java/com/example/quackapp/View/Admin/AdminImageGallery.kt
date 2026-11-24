package com.example.quackapp.View.Admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.AdminController
import com.example.quackapp.Controller.imageGalleryItems
import com.example.quackapp.Model.Campsite

/**
 * Admin "Image Gallery" screen (Manage Uploaded Media in the mockup).
 *
 * Shows every uploaded campsite image in a searchable two-column grid. Tapping an image opens
 * it full screen with a "Delete image" button.
 *
 * Supports US 03.06.01 - As an admin, I want to browse uploaded images so I can remove them if necessary.
 * Deleting uses [AdminController.deleteImage] (also covers removing images, US 03.03.01).
 *
 * @param navController used by the admin nav bar.
 * @param viewModel provides campsites and image deletion.
 */
@Composable
fun AdminImageGallery(
    navController: NavController,
    viewModel: AdminController = viewModel()
) {
    val campsites by viewModel.campsites.collectAsState()

    AdminScaffold(navController) { innerPadding ->
        AdminImageGalleryContent(
            modifier = Modifier.padding(innerPadding),
            campsites = campsites,
            onDeleteImage = { viewModel.deleteImage(it) }
        )
    }
}

/**
 * Stateless gallery body: grid view, or full-screen view of one image.
 * Kept separate from Firebase so it can be intent tested with fake campsites.
 *
 * @param campsites all campsites; only ones with an image are shown.
 * @param onDeleteImage called after the admin confirms deleting a campsite's image.
 */
@Composable
fun AdminImageGalleryContent(
    modifier: Modifier = Modifier,
    campsites: List<Campsite>,
    onDeleteImage: (Campsite) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Campsite?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val current = selected
    if (current == null) {
        val items = imageGalleryItems(campsites, query)
        Column(modifier = modifier.fillMaxSize()) {
            AdminHeader("Image Gallery")
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("search...") },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdminYellow,
                    unfocusedBorderColor = AdminYellow
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .testTag("gallerySearch")
            )
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No uploaded images")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.campId }) { camp ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selected = camp }
                        ) {
                            AsyncImage(
                                model = camp.optionalPromoImage,
                                contentDescription = "Image of ${camp.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text(camp.name, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    } else {
        // Full-screen view of one image
        Column(modifier = modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth()) {
                AdminHeader("Image Gallery")
                IconButton(
                    onClick = { selected = null },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AdminYellow)
                }
            }
            AsyncImage(
                model = current.optionalPromoImage,
                contentDescription = "Full image of ${current.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = { confirmDelete = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Delete image")
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Delete image?") },
                text = { Text("The image for \"${current.name}\" will be permanently deleted. This can't be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteImage(current)
                            confirmDelete = false
                            selected = null
                        },
                        modifier = Modifier.testTag("confirmDeleteImage")
                    ) { Text("Delete", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                }
            )
        }
    }
}
