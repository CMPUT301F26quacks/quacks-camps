package com.example.quackapp.View.Entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.EntrantDetailedCampsiteController

/**
 * Campsite details screen for Entrant
 *
 * When an entrant clicks on an EntrantHomeCard, it sends them to this page
 * with information on the campsite and the ability to enter that campsites lottery
 *
 * @param modifier mainly for padding
 * @param navController allows entrant to use navigation bar
 * @param controller uses EntrantDetailedCampsiteController to fetch the
 * corresponding campsite
 */
@Composable
fun EntrantDetailedCampsite(
    modifier: Modifier = Modifier,
    navController: NavController,
    controller: EntrantDetailedCampsiteController = viewModel()
) {

    //uses controller to fetch the campsite that was clicked on
    val pulledCampsite by controller.campsite.collectAsState()

    //Creates a local version of the campsite info
    val campsite = pulledCampsite

    var enteredLottery by remember { mutableStateOf(false) }

    //variables for the user the input a rating later
    var showRatingInput by remember {mutableStateOf(false)}
    var ratingInput by remember {mutableStateOf("")}
    var errorMessage by remember {mutableStateOf<String?>(null)}

    //If campsite has not loaded in yet, wait
    if (campsite == null) {
        Text(
            "Loading campsite...",
            modifier = modifier.padding(16.dp)
        )
        return
    }

    //Provides bottom navigation bar
    EntrantScaffold(navController) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            //Scrollable column of the campsite info
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 32.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    Text(
                        //Campsite Name
                        text = campsite.name,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFFF16B00)
                    )
                }
                item {
                    Column {
                        //Campsite Rating
                        Text(
                            text = "⭐ ${campsite.rating}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00)
                        )
                        //User Make a Rating
                        OutlinedButton(
                            onClick = {showRatingInput = !showRatingInput}
                        ) {
                            Text(
                                if(showRatingInput) "Close" else "Make a Rating"
                            )
                        }

                        //if user wants to rate campsite
                        if(showRatingInput) {
                            OutlinedTextField(
                                value = ratingInput,
                                onValueChange = { input ->
                                    //only allow 1 number to be entered
                                    if (input.length<=1) {
                                        ratingInput = input
                                        //ensure that the number is not outside of range
                                        val ratingNum = input.toIntOrNull()
                                        if(ratingNum == null || ratingNum < 0 || ratingNum > 5) {
                                            errorMessage = "Please input a valid rating"
                                        }
                                        else{ //valid
                                            errorMessage = null
                                        }
                                    }
                                },
                                label = {Text("Enter rating (0-5)")},
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = errorMessage != null,
                                modifier = Modifier.fillMaxWidth()
                            )

                            //showing the error message when invalid
                            errorMessage?.let {message ->
                                Text(
                                    text=message,
                                    color=Color.Red
                                )
                            }

                            //submit button if valid (double verification, there will be no mistakes)
                            val ratingNum = ratingInput.toIntOrNull()
                            if(ratingNum != null){
                                Button(
                                    onClick = {
                                        com.example.quackapp.Controller.DeviceIdGetter.getDeviceId(
                                            onSuccess = {deviceId ->
                                                controller.updateRating(ratingNum.toDouble(), deviceId)
                                                showRatingInput = !showRatingInput
                                                ratingInput = "" //set empty
                                            },
                                            onFail = {
                                                errorMessage = "Rating failed to submit. Please try again."
                                            }
                                        )
                                    },
                                ) {
                                    Text("Submit Rating")
                                }
                            }
                        }


                        //Campsite Location
                        Text(
                            text = "\uD83D\uDCCC ${campsite.location}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00)
                        )
                        //Campsite price per night
                        Text(
                            text = "$${campsite.price} per night",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00)
                        )
                        //Campsite capacity
                        Text(
                            text = "Up to ${campsite.capacity} people",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00)
                        )
                        //Days left on campsite lottery
                        Text(
                            text = "(TO-DO) days left on lottery",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00)
                        )
                    }
                }
                item {
                    //If the campsite listing has an image, display it
                    if (campsite.optionalPromoImage != "") {
                        AsyncImage(
                            model = campsite.optionalPromoImage,
                            contentDescription = campsite.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.FillWidth
                        )

                    }
                }

                //Gets all the campsite's features that are not empty
                val features = campsite.features.filter { it.isNotBlank() }

                //Display features if there are any
                if (features.isNotEmpty()) {
                    item {
                        Column {
                            //Display Features
                            Text(
                                text = "Features",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFF16B00)
                            )
                            //Displays all Campsite Features in bullet point
                            features.forEach { feature ->
                                Text(
                                    text = "\u2022 $feature",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFFF2BA00)
                                )
                            }
                        }
                    }
                }

                //Gets all the campsite's rules that are not empty
                val rules = campsite.rules.filter { it.isNotBlank() }

                //Display rules if there are any
                if (rules.isNotEmpty()) {
                    item {
                        Column {
                            //Display Rules
                            Text(
                                text = "Rules",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFF16B00)
                            )

                            //Displays all Campsite Rules in bullet point
                            rules.forEach { rule ->
                                Text(
                                    text = "\u2022 $rule",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFFF2BA00)
                                )
                            }
                        }
                    }
                }


            }
                //Enter/Leave Lottery Button
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF248AFF),
                        contentColor = Color.White,
                    ),
                    //When Button is clicked, enter or leave the lottery
                    onClick = {
                        //TODO Enter or Leave lottery logic
                        enteredLottery = !enteredLottery
                    },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    modifier = modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)

                ) {
                    Text(
                        //Switches text to reflect click
                        text =
                            if (!enteredLottery) "Enter Lottery Waitlist"
                            else "Leave Lottery Waitlist",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                }

        }
    }

}
