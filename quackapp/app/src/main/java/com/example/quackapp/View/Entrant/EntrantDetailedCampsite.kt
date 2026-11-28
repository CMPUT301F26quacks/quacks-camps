package com.example.quackapp.View.Entrant

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quackapp.Controller.DeviceIdGetter
import com.example.quackapp.Controller.EntrantDetailedCampsiteController
import com.example.quackapp.Controller.UserRepository
import com.example.quackapp.R
import com.example.quackapp.View.MapPreviewCard
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.installations.FirebaseInstallations


/**
 * Campsite details screen for Entrant
 *
 * When an entrant clicks on an EntrantHomeCard, it sends them to this page
 * with information on the campsite and the ability to enter or leave
 * that campsites lottery waitlist
 *
 * [US 01.01.05] As an entrant, I want to see details of a campsite.
 * [US 01.01.06] As an entrant, I want to join an active lottery from
 * the campsite details screen.
 *
 * @param modifier mainly for padding
 * @param navController allows entrant to use navigation bar
 * @param controller uses EntrantDetailedCampsiteController to fetch the
 * corresponding campsite, all lotteries and use various other functions
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun EntrantDetailedCampsite(
    modifier: Modifier = Modifier,
    navController: NavController,
    controller: EntrantDetailedCampsiteController = viewModel()
) {

    //uses controller to fetch the campsite that was clicked on
    val pulledCampsite by controller.campsite.collectAsState()
    val pulledLotteries by controller.lotteries.collectAsState()

    //Creates a local version of the campsite and lotteries info
    val campsite = pulledCampsite
    val lotteries = pulledLotteries

    //Get device ID from firebase
    var deviceId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        FirebaseInstallations.getInstance().id
            .addOnSuccessListener { deviceId = it }
    }

    //Gets Campsite lottery ID
    val lotteryId = campsite?.lotteryId
    //local version
    val currentDeviceId = deviceId

    //Checks if the user is currently entered into the waitlist or not
    var enteredLottery by remember(
        lotteryId,
        lotteries,
        currentDeviceId)
    {
        mutableStateOf(controller.checkIfIdInLottery(
            lotteryId,
            currentDeviceId,
            lotteries)
        )
    }

    //variables for the user the input a rating
    var showRatingInput by remember {mutableStateOf(false)}
    var ratingInput by remember {mutableStateOf("")}
    var errorMessage by remember {mutableStateOf<String?>(null)}

    //variables for the user to input a review
    var showReviewInput by remember {mutableStateOf(false)}
    var reviewInput by remember {mutableStateOf("")}
    var reviewErrorMessage by remember {mutableStateOf<String?>(null)}

    //Fetches number of days left on that campsites lottery
    val bookingPeriodEnd = lotteries.find { it.lotteryID == campsite?.lotteryId }?.registrationPeriodEnd
    val now = System.currentTimeMillis()
    val numDaysLeftToEnterWaitlist: Long =
        bookingPeriodEnd?.let { ((it - now) / 86400000).coerceAtLeast(0) } ?: 0L

    var isEditingReview by remember { mutableStateOf(false) }

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
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            //Scrollable column of the campsite info
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 0.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box {
                        //If the campsite listing has an image, display it
                        if (campsite.optionalPromoImage != "") {
                            AsyncImage(
                                model = campsite.optionalPromoImage,
                                contentDescription = campsite.name,
                                modifier = Modifier
                                    .requiredWidth(screenWidth)
                                    .height(300.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.defaultcampsite),
                                contentDescription = "Blank Campsite",
                                modifier = Modifier
                                    .requiredWidth(screenWidth)
                                    .height(300.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .requiredWidth(screenWidth)
                                .height(34.dp)
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
                                )
                        )
                    }

                }
                item {
                    Text(
                        //Campsite Name
                        text = campsite.name,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFFF16B00),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("campsiteTitle")
                            .offset(y = (-12).dp)
                    )
                    Text(
                        text = controller.availabilityToText(
                            campsite,
                            LocalLocale.current.platformLocale
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFF2BA00),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Campsite Rating
                            Text(
                                text = "⭐ ${campsite.rating}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFF2BA00),
                                modifier = Modifier.testTag("rating")
                            )
                            Text(
                                text = "Reviews",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF2BA00),
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clickable { navController.navigate("EntrantCampsiteReviews/${campsite.campId}") }
                                    .testTag("showReviewsButton")
                            )
                            /*
                            //Show Reviews Button
                            OutlinedButton(
                                onClick = { navController.navigate("EntrantCampsiteReviews/${campsite.campId}") },
                                modifier = Modifier.testTag("showReviewsButton")
                            ) {
                                Text("Show Reviews")
                            }

                             */
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //User Make a Rating
                                OutlinedButton(
                                    onClick = { showRatingInput = !showRatingInput },
                                    modifier = Modifier.testTag("makeRating")
                                ) {
                                    Text(
                                        if (showRatingInput) "Close" else "Make a Rating"
                                    )
                                }
                            /*
                                //User Post Review Button
                                OutlinedButton(
                                    onClick = { showReviewInput = !showReviewInput },
                                    modifier = Modifier.testTag("makeReview")
                                ) {
                                    Text(
                                        if (showReviewInput) "Close Review" else "Post a Review"
                                    )
                                }

                             */
                                //User Post Review Button
                                OutlinedButton(
                                    onClick = {
                                        if (showReviewInput) {
                                            showReviewInput = false
                                        } else {
                                            DeviceIdGetter.getDeviceId(
                                                onSuccess = { deviceId ->
                                                    controller.getUserReview(
                                                        deviceId,
                                                        onSuccess = { existingReview ->
                                                            reviewInput = existingReview ?: ""
                                                            isEditingReview = existingReview != null
                                                            reviewErrorMessage = null
                                                            showReviewInput = true
                                                        },
                                                        onFail = {
                                                            reviewErrorMessage = "Couldn't load your review. Close and try again."
                                                            showReviewInput = true
                                                        }
                                                    )
                                                },
                                                onFail = {
                                                    reviewErrorMessage = "Couldn't load your review. Close and try again."
                                                    showReviewInput = true
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("makeReview")
                                ) {
                                    Text(
                                        if (showReviewInput) "Close Review" else "Post a Review"
                                    )
                                }
                            }
                            //if user wants to rate campsite
                            if (showRatingInput) {
                                OutlinedTextField(
                                    value = ratingInput,
                                    onValueChange = { input ->
                                        //only allow 1 number to be entered
                                        if (input.length <= 1) {
                                            ratingInput = input
                                            //ensure that the number is not outside of range
                                            val ratingNum = input.toIntOrNull()
                                            if (ratingNum == null || ratingNum < 0 || ratingNum > 5) {
                                                errorMessage = "Please input a valid rating"
                                            } else { //valid
                                                errorMessage = null
                                            }
                                        }
                                    },
                                    label = { Text("Enter rating (0-5)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                //showing the error message when invalid
                                errorMessage?.let { message ->
                                    Text(
                                        text = message,
                                        color = Color.Red
                                    )
                                }

                                //submit button if valid (double verification, there will be no mistakes)
                                val ratingNum = ratingInput.toIntOrNull()
                                if (ratingNum != null) {
                                    Button(
                                        onClick = {
                                            DeviceIdGetter.getDeviceId(
                                                onSuccess = { deviceId ->
                                                    controller.updateRating(
                                                        ratingNum.toDouble(),
                                                        deviceId
                                                    )
                                                    showRatingInput = false
                                                    ratingInput = "" //set empty
                                                },
                                                onFail = {
                                                    errorMessage =
                                                        "Rating failed to submit. Please try again."
                                                }
                                            )
                                        },
                                    ) {
                                        Text("Submit Rating")
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))



                            //if user wants to post a review
                            if (showReviewInput) {
                                OutlinedTextField(
                                    value = reviewInput,
                                    onValueChange = {
                                        reviewInput = it
                                        reviewErrorMessage =
                                            if (it.isBlank()) "Review cannot be empty" else null
                                    },
                                    label = { Text("Write your review") },
                                    singleLine = false,
                                    isError = reviewErrorMessage != null,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                reviewErrorMessage?.let { message ->
                                    Text(
                                        text = message,
                                        color = Color.Red
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (reviewInput.isNotBlank()) {
                                            DeviceIdGetter.getDeviceId(
                                                onSuccess = { deviceId ->
                                                    if (isEditingReview) {
                                                        controller.updateReview(
                                                            reviewInput,
                                                            deviceId)
                                                    } else {
                                                        controller.submitReview(
                                                            reviewInput,
                                                            deviceId)
                                                    } //changed
                                                    showReviewInput = false
                                                    reviewInput = ""
                                                },
                                                onFail = {
                                                    reviewErrorMessage =
                                                        "Review failed to submit. Please try again."
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("submitReviewButton")
                                ) {
                                    Text(if (isEditingReview) "Update Review" else "Submit Review")
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        //Campsite Location
                        Text(
                            text = "\uD83D\uDCCC ${campsite.location}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00),
                            modifier = Modifier.testTag("location")
                        )
                        //Campsite price per night
                        Text(
                            text = "$${campsite.price} per night",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00),
                            modifier = Modifier.testTag("pricePerNight")
                        )
                        //Campsite capacity
                        Text(
                            text = if(campsite.capacity > 1) "Up to ${campsite.capacity} people"
                            else "Up to ${campsite.capacity} person",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00),
                            modifier = Modifier.testTag("capacity")
                        )
                        //Days left on campsite lottery
                        if (lotteryId != null && campsite.availability.isNotEmpty()) {
                            Text(
                                text =
                                    if(numDaysLeftToEnterWaitlist.toInt() != 1)
                                        "$numDaysLeftToEnterWaitlist days left on lottery"
                                    else
                                        "$numDaysLeftToEnterWaitlist day left on lottery" ,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFF2BA00),
                                modifier = Modifier.testTag("lotteryDaysLeft")
                            )
                        }

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
                                color = Color(0xFFF16B00),
                                modifier = Modifier.testTag("feature")
                            )
                            //Displays all Campsite Features in bullet point
                            features.forEach { feature ->
                                Text(
                                    text = "\u2022 $feature",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFFF2BA00),
                                    modifier = Modifier.testTag("featureItem")
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
                                    color = Color(0xFFF2BA00),
                                    modifier = Modifier.testTag("ruleItem")
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Map",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFF16B00)
                    )
                }
                item {
                    if (campsite.latitude != 0.00 && campsite.longitude != 0.00) {
                        MapPreviewCard(
                            location = LatLng(campsite.latitude, campsite.longitude),
                            title = "Map",
                            onMapClick = {},
                        )
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        thickness = 2.dp,
                        color = Color.LightGray
                    )
                }

                //Message the owner
                item {
                    Column (verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Campsite Owner",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFF16B00)
                        )

                        OwnerSection(ownerId = campsite.ownerId)

                        /*
                        Text(
                            text = "\u2022 $rule",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF2BA00),
                            modifier = Modifier.testTag("ruleItem")
                        )

                         */
                    }
                }
            }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    .testTag("backButton")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back button",
                    tint = Color.Black)
            }



            //If campsite is in a lottery, then display button
            if (campsite.lotteryId != null) {
                //Enter or Leave Lottery Waitlist Button
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF248AFF),
                        contentColor = Color.White,
                    ),
                    //When Button is clicked, enter or leave the lottery
                    onClick = {
                        if (lotteryId != null && currentDeviceId != null) {
                            if (enteredLottery) controller.leaveLottery(lotteryId, currentDeviceId)
                            else controller.enterLottery(lotteryId, currentDeviceId)
                            enteredLottery = !enteredLottery
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    modifier = modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .testTag("inLotteryStatus")

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

}



@Composable
fun OwnerSection(ownerId: String, modifier: Modifier = Modifier) {
    var imageUrl by remember(ownerId) { mutableStateOf("") }
    var name by remember(ownerId) { mutableStateOf("") }

    LaunchedEffect(ownerId) {
        if (ownerId.isNotBlank()) {
            UserRepository.getUser(
                deviceId = ownerId,
                onResult = { user ->
                    imageUrl = user?.profileImageUrl ?: ""
                    name = user?.name ?: "Unknown owner"
                           },
                onFail = { imageUrl = "" }
            )
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if(imageUrl == ""){
            Image(
                painter = painterResource(id = R.drawable.defaultpfp),
                contentDescription = "Blank profile pic",
                modifier = modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }
        else{
        AsyncImage(
            model = imageUrl,
            contentDescription = "Owner profile pic",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
            }
        Column(){
            Text(
                text = name,
                color = Color(0xFFF2BA00),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Message",
                textDecoration = TextDecoration.Underline,
                color = Color(0xFFF2BA00),
                style = MaterialTheme.typography.titleLarge
            )
        }


    }
}



