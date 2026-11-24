package com.example.quackapp.View

import android.R.attr.onClick
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.DetailedCampsiteController
import com.google.android.gms.maps.model.LatLng

/**
 * Owner-side UI for viewing the details of their campsite
 * @param navController navController object used to navigate to this screen
 * @param viewModel the controller used with this screen. Default [DetailedCampsiteController]
 */
@Composable
fun DetailedCampsiteScreen(
    navController: NavController,
    viewModel: DetailedCampsiteController = viewModel()
) {
    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = viewModel.name, fontSize = 36.sp)
                    IconButton(
                        modifier = Modifier.testTag("backButton"),
                        colors = iconButtonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "On click go back"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val preview = viewModel.imageUrl
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (preview != "") {
                    AsyncImage(
                        model = preview,
                        contentDescription = "Promo image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("No Image")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            //rating?
            Text(text = "Rating: ", fontSize = 30.sp)
            Text(text = viewModel.rating, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(   //location
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF248AFF)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(text = "Location:", fontSize = 24.sp, color = Color.White)
                        Text(text = viewModel.location, fontSize = 18.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Card(      //price
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF248AFF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(text = "Cost:", fontSize = 24.sp, color = Color.White)
                        Text(
                            text = "$${viewModel.price} per night",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Card(   //capacity
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF248AFF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(text = "Capacity:", fontSize = 24.sp, color = Color.White)
                        Text(
                            text = "${viewModel.capacity} people",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // If statement here because the Map loads before location data is ready
            // meaning it would default to (0.00, 0.00) instead of the real location
            if (viewModel.latitude != 0.00 && viewModel.longitude != 0.00) {
                MapPreviewCard(
                    location = LatLng(viewModel.latitude, viewModel.longitude),
                    title = viewModel.name,
                    onMapClick = {},
                )
            }

            Text(text = "Cancellation Policy:", fontSize = 30.sp)
            if (viewModel.cancellationPolicy == "") {
                Text(text = "No cancellation policy", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(text = viewModel.cancellationPolicy, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Amenities:", fontSize = 30.sp)
            Column() {
                if (viewModel.features == listOf("")) {
                    Text(text = "No listed amenities/features", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    viewModel.features.forEachIndexed { index, value ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = value,
                                fontSize = 24.sp,
                                modifier = Modifier.testTag("amenity${index + 1}")
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }
            }
            Text(text = "Rules:", fontSize = 30.sp)
            Column() {
                if (viewModel.rules == listOf("")) {
                    Text(text = "No listed amenities/features", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    viewModel.rules.forEachIndexed { index, value ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "\tRule ${index + 1}: $value",
                                fontSize = 24.sp,
                                modifier = Modifier.testTag("rule${index + 1}")
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.selectedTags.forEach { tag ->
                    Tag(
                        modifier = Modifier.testTag("tag${tag}"),
                        tag,
                        tag in viewModel.selectedTags,
                        onClick = {}
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lottery Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    val currentLottery = viewModel.lottery
                    if (currentLottery != null) {
                        Text(
                            "Status: ${currentLottery.status.name}"
                        )
                        Text(
                            "Registration: ${currentLottery.registrationPeriodStart?.toDate()} to ${currentLottery.registrationPeriodEnd?.toDate()}",
                            fontSize = 16.sp
                        )
                        Text(
                            "Booking Period: ${currentLottery.bookingPeriodStart?.toDate()} to ${currentLottery.bookingPeriodEnd?.toDate()}",
                            fontSize = 16.sp
                        )
                        Text(
                            "Winners to Draw: ${currentLottery.numLotteryWinners}",
                            fontSize = 16.sp
                        )
                        currentLottery.optionalWaitlistCapacity?.let { cap ->
                            Text("Waitlist Capacity: $cap", fontSize = 14.sp)
                        }
                    } else {
                        Text(
                            "No active lottery created for this campsite.",
                            fontSize = 16.sp
                        )
                        // TEMPORARY
                        Button(onClick = { navController.navigate("ListCampInLotteryScreen/${viewModel.campsiteId}") }) {
                            Text("Create Lottery")
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(8.dp))

            //Currently Booked dates for the campsite
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Currently Booked Dates", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))


                    if (viewModel.bookedReservations.isEmpty()) {
                        Text(
                            text = "No dates are currently booked for this campsite.",
                            fontSize = 16.sp
                        )
                    } else {
                        viewModel.bookedReservations.forEachIndexed { index, reservation ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = "${reservation.dateRange} (${reservation.entrantName})",
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bookedDate${index + 1}")
                                )
                                Button(
                                    onClick = {
                                        val success = ReservationRepository.getInstance().rejectReservation(reservation.id)
                                        if (success) {
                                            ReservationRepository.getInstance()
                                                .getConfirmedReservations(viewModel.campsiteId) { updated ->
                                                    viewModel.bookedReservations = updated
                                                }
                                        }
                                    },colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("rejectReservation${index + 1}")
                                ) {
                                Text("Reject", color = Color.White)
                              }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            //Lottery waitlist for the campsite
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lottery Waitlist (${viewModel.waitlistEntrants.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (viewModel.lottery == null) {
                        Text(
                            text = "No active lottery for this campsite.",
                            fontSize = 16.sp
                        )
                    } else if (viewModel.waitlistEntrants.isEmpty()) {
                        Text(
                            text = "No entrants have joined the waitlist yet.",
                            fontSize = 16.sp
                        )
                    } else {
                        viewModel.waitlistEntrants.forEachIndexed { index, entrant ->
                            Text(
                                text = "${index + 1}. $entrant",
                                fontSize = 16.sp,
                                modifier = Modifier.testTag("waitlistEntrant${index + 1}")
                            )
                        }
                    }
                }
            }
        }
    }
}

