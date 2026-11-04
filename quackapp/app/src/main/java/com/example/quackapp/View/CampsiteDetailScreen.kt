package com.example.quackapp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quackapp.Model.Campsite

/**
 * Campsite Detail Screen
 * [US 02.01.04] As a campsite owner, I want to view details of my campsite.
 *
 * TO-DO: figure out how it will work if there is no image...
 * @param navController allows you to move between screens using navigation bar
 * @param campsite knowing which campsite to show details of
 */
@Composable
fun CampsiteDetailScreen (navController: NavController, campsite: Campsite) {
    val scrollState =
        rememberScrollState() //im almost certain with the amount of details it will go off the page so i need to include this to make the page scrollable
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //upper row: icon in left corner, back button on right corner
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //TODO: add icon later
            //title text
            Text(
                "Details",
                fontSize = 40.sp
            )

            //back button
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

        Spacer(modifier = Modifier.height(16.dp))

        //optional image
        Spacer(modifier = Modifier.height(16.dp))

        //middle col:  the text details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Campsite Title & Location
            Text(   //campsite title: im assuming ts is not empty since its a mandatory field so no checking... :')
                text = campsite.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text( //campsite location
                text = campsite.location,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Info Boxes: Price, Capacity, Rating (details that are not lists)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(   //price
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF248AFF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$${campsite.price} / night",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }

                Card(   //capacity
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF248AFF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Capacity: ${campsite.capacity} guests",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }

                Card(   //rating
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF248AFF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Rating: ${campsite.rating}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // features (description - amenities)
            if (campsite.features.isNotEmpty()) {
                Text("Description: ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = campsite.features.joinToString(", "),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tags
            if (campsite.tags.isNotEmpty()) {
                Text("Tags", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = campsite.tags.joinToString("  ") { tag -> if (tag.startsWith("#")) tag else "#$tag" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Campsite Rules
            if (campsite.rules.isNotEmpty()) {
                Text("Campsite Rules", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                campsite.rules.forEach { rule ->
                    Text("• $rule", fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Cancellation Policy
            if (campsite.cancellationPolicy.isNotBlank()) {
                Text("Cancellation Policy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    campsite.cancellationPolicy,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // lottery details
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

                    val currentLottery = campsite.lottery
                    if (currentLottery != null) {
                        Text(
                            "Status: ${currentLottery.status.name}"
                        )
                        Text(
                            "Registration: ${currentLottery.registrationPeriodStart} to ${currentLottery.registrationPeriodEnd}",
                            fontSize = 12.sp
                        )
                        Text(
                            "Booking Period: ${currentLottery.bookingPeriodStart} to ${currentLottery.bookingPeriodEnd}",
                            fontSize = 12.sp
                        )
                        Text(
                            "Winners to Draw: ${currentLottery.numLotteryWinners}",
                            fontSize = 12.sp
                        )
                        currentLottery.optionalWaitlistCapacity?.let { cap ->
                            Text("Waitlist Capacity: $cap", fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            "No active lottery created for this campsite.",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
