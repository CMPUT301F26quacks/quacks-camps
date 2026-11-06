package com.example.quackapp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.CampsiteOwner

/**
 * UI for the home page that Campsite owners are directed to
 *
 * Uses [navController] to navigate to other screens
 * Uses [OwnerHomeController] to deal with user input
 */
@Composable
fun OwnerHome(
    navController: NavController,
    viewModel: OwnerHomeController = viewModel()
) {
    val camps by viewModel.camps.collectAsState()
    var campToDelete by remember { mutableStateOf<Campsite?>(null) }
    var campToShowQR by remember { mutableStateOf<Campsite?>(null) }    //track which campsite's QR code to display

    OwnerScaffold (
        navController = navController,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Campsites", fontSize = 40.sp)

                    FloatingActionButton(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White,
                        onClick = { navController.navigate("RegisterCampsite") },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Text("+", fontSize = 18.sp)
                    }
                }
            }

        }
    ) { innerPadding ->
        if (camps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No campsites registered")
            }
        }
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(camps) { campsite ->
                    CampRow(campsite, navController, onDeleteClick = { campToDelete = campsite }, onQRClick = {campToShowQR = campsite})
                }
            }
            // As soon as the campToDelete is not null anymore, create a popup to confirm deletion
            campToDelete?.let { camp ->
                AlertDialog(
                    onDismissRequest = { campToDelete = null },
                    title = { Text("Delete campsite?") },
                    text = { Text("\"${camp.name}\" will be permanently deleted. This can't be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteCampsite(camp)
                                campToDelete = null
                            },
                            modifier = Modifier.testTag("confirmDelete")
                        ) { Text("Delete", color = Color.Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { campToDelete = null }) { Text("Cancel") }
                    }
                )
            }

            campToShowQR?.let {camp ->
                val qrImageUrl = CampsiteOwner.generateQRCode(camp)

                AlertDialog(
                    onDismissRequest = {campToShowQR = null},
                    title = {Text("QR Code for ${camp.name}", fontWeight = FontWeight.Bold)},
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = qrImageUrl,
                                contentDescription = "Campsite QR Code",
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { campToShowQR = null }) {
                            Text("Close")
                        }
                    }
                )

            }
        }
    }
}

/**
 * UI for each [Campsite] element the user owns
 *
 */
@Composable
fun CampRow(
    campsite: Campsite,
    navController: NavController,
    onDeleteClick: () -> Unit,
    onQRClick: () -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(4.dp)
            .background(Color(0xFF2196F3), shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp)
    ) {
        if (campsite.optionalPromoImage != ""){
            AsyncImage(
                model = campsite.optionalPromoImage,
                contentDescription = campsite.name,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .height(80.dp)
                    .width(100.dp),
                contentScale = ContentScale.Crop
            )
        }
        else{
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(80.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ){
                Text("No Image", textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                campsite.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Edit",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.clickable(onClick = {navController.navigate("EditCampsite/${campsite.campId}")})
                )
                Text(
                    text = "Details",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.clickable(onClick = {navController.navigate("CampsiteDetails/${campsite.campId}")})
                )
                Text(
                    text = "Delete",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(onClick = onDeleteClick)
                )
                Text(
                    text = "QR Code",
                    fontSize = 18.sp,
                    color=Color.White,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(onClick = onQRClick)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

    }
}