package com.example.quackapp.View.Owner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.CampsiteOwner
import com.example.quackapp.Model.ListCampInLotteryRoute

/**
 * UI for the home page that Campsite owners are directed to
 *
 * @param navController navController object used to navigate to this screen
 * @param viewModel the controller used with this screen. Default [OwnerHomeController]
*/
@Composable
fun OwnerHome(
    navController: NavController,
    viewModel: OwnerHomeController = viewModel()
) {
    val camps by viewModel.ownedCamps.collectAsState()
    val otherCamps by viewModel.otherCamps.collectAsState()
    var campToDelete by remember { mutableStateOf<Campsite?>(null) }
    var campToShowQR by remember { mutableStateOf<Campsite?>(null) }    //track which campsite's QR code to display
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val selectionMode = selectedIds.isNotEmpty()
    var showOtherCamps by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.clearSelection()
    }

    OwnerScaffold(
        navController = navController,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column() {
                        Text(text = "Campsites", fontSize = 40.sp)
                        Text(
                            text = "Tap and hold to select",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    FloatingActionButton(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White,
                        onClick = { navController.navigate("RegisterCampsite/${viewModel.ownerId}") },
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .testTag("addCampsiteButton")
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
        } else {
            BackHandler(enabled = selectionMode) { viewModel.clearSelection() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = selectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            modifier = Modifier.testTag("createLotteryButton"),
                            onClick = { navController.navigate(ListCampInLotteryRoute(campsiteIds = selectedIds.toList())) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFFF3AE00))
                        ) {
                            Text("Create Lottery")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically,) {
                            Text(
                                text = "${selectedIds.size} selected",
                                textAlign = TextAlign.Right,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("selectedCount")
                            )
                            TextButton(
                                modifier = Modifier.testTag("clearSelected"),
                                onClick = { viewModel.clearSelection() },
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text("X")
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(if (showOtherCamps) 0.5f else 1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(camps, key = { it.campId }) { campsite ->
                        CampRow(
                            campsite,
                            navController,
                            owned = true,
                            onDeleteClick = { campToDelete = campsite },
                            onQRClick = { campToShowQR = campsite },
                            isSelected = campsite.campId in selectedIds,
                            selectionMode = selectionMode,
                            onClick = {
                                if (selectionMode) viewModel.toggleSelection(campsite.campId)
                                else navController.navigate("CampsiteDetails/${campsite.campId}")
                            },
                            onLongClick = { viewModel.toggleSelection(campsite.campId) }
                        )
                    }
                }
                Button(
                    onClick = {showOtherCamps = !showOtherCamps},
                    colors = if (!showOtherCamps) ButtonDefaults.buttonColors(Color(0xFF2196F3))
                             else ButtonDefaults.buttonColors(Color(0xFFF3AE00))
                ) {
                    Text("View other Owners' camps")
                }

                AnimatedVisibility(
                    visible = showOtherCamps,
                    modifier = Modifier.weight(0.5f),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(otherCamps, key = { it.campId }) { campsite ->
                            CampRow(
                                campsite,
                                navController,
                                owned = false,
                                onDeleteClick = { campToDelete = campsite },
                                onQRClick = { campToShowQR = campsite },
                                isSelected = campsite.campId in selectedIds,
                                selectionMode = selectionMode,
                                onClick = {
                                    if (selectionMode) viewModel.toggleSelection(campsite.campId)
                                    else navController.navigate("CampsiteDetails/${campsite.campId}")
                                },
                                onLongClick = { viewModel.toggleSelection(campsite.campId) }
                            )
                        }
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

                campToShowQR?.let { camp ->
                    val qrImageUrl = CampsiteOwner.generateQRCode(camp)

                    AlertDialog(
                        onDismissRequest = { campToShowQR = null },
                        title = { Text("QR Code for ${camp.name}", fontWeight = FontWeight.Bold) },
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
}

/**
 * UI for each [Campsite] element the user owns
 *
 */
@Composable
fun CampRow(
    campsite: Campsite,
    navController: NavController,
    owned: Boolean,
    onDeleteClick: () -> Unit,
    onQRClick: () -> Unit,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
){
    val haptics = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFFFF9800) else Color(0xFF2196F3)
            )
            .testTag("${campsite.name} row")
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
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
            var displayName: String = campsite.name
            if (displayName.length > 12){
                displayName = displayName.take(12) + "..."
            }
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(10.dp))
            if (owned) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .testTag("edit${campsite.name}")
                            .clickable(onClick = { navController.navigate("EditCampsite/${campsite.campId}") })
                    )
                    Text(
                        text = "Details",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .testTag("details${campsite.name}")
                            .clickable(onClick = { navController.navigate("CampsiteDetails/${campsite.campId}") })
                    )
                    // TODO delete campId from related lotteries
                    //  and maybe the lottery itself if the campIds list is empty
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(onClick = onDeleteClick)
                            .testTag("delete${campsite.name}")
                    )
                    Text(
                        text = "QR Code",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(onClick = onQRClick)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

    }
}