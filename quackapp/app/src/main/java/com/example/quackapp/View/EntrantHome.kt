package com.example.quackapp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quackapp.Model.CampsiteSummary
import com.example.quackapp.Model.LotteryOffer
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationRepository
import kotlinx.coroutines.launch

/**
 * Main dashboard screen for the Entrant role.
 * Implements:
 * - US 01.06.01: View list of active reservations.
 * - US 01.04.01: View available dates upon winning lottery.
 * - US 01.04.02: Choose campsite in a multiple campsite lottery.
 * - US 01.04.03: Decline reservation offer.
 * - US 01.05.01: Receive reservation confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrantHome(
    navController: NavController,
    repository: ReservationRepository = remember { ReservationRepository.getInstance() }
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val reservations by repository.reservations.collectAsState()
    val offers by repository.offers.collectAsState()

    val activeReservations = remember(reservations) {
        repository.getActiveReservations()
    }
    val pendingOffers = remember(offers) {
        repository.getPendingOffers()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Confirmation dialog state (US 01.05.01)
    var confirmedReservationForDialog by remember { mutableStateOf<Reservation?>(null) }

    // Decline confirmation dialog state (US 01.04.03)
    var offerToDecline by remember { mutableStateOf<LotteryOffer?>(null) }

    // Cancellation dialog state
    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("QuackCamps", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Entrant Portal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { navController.navigate("UserStart") }) {
                        Text(
                            text = "← Back",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Active Reservations (${activeReservations.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Lottery Wins (${pendingOffers.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            when (selectedTabIndex) {
                0 -> ActiveReservationsList(
                    reservations = activeReservations,
                    onCancelClick = { res -> reservationToCancel = res },
                    onGoToLotteryWins = { selectedTabIndex = 1 }
                )
                1 -> LotteryOffersList(
                    offers = pendingOffers,
                    onDecline = { offer -> offerToDecline = offer },
                    onConfirm = { offer, campsite, chosenDate, numGuests ->
                        val res = repository.confirmReservation(
                            offerId = offer.id,
                            campsiteId = campsite.campsiteId,
                            chosenDate = chosenDate,
                            numGuests = numGuests,
                            deviceId = "my_device"
                        )
                        if (res != null) {
                            confirmedReservationForDialog = res
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Unable to confirm reservation. Please try again.")
                            }
                        }
                    }
                )
            }
        }
    }

    // Reservation Confirmation Dialog (US 01.05.01)
    confirmedReservationForDialog?.let { res ->
        AlertDialog(
            onDismissRequest = { confirmedReservationForDialog = null },
            title = {
                Text("Reservation Confirmed!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            },
            text = {
                Column {
                    Text("Congratulations! Your campsite spot has been successfully booked.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Campsite: ${res.campsiteName}", fontWeight = FontWeight.SemiBold)
                    Text("Location: ${res.campsiteLocation}")
                    Text("Stay Dates: ${res.dateRange}", fontWeight = FontWeight.Medium)
                    Text("Guests: ${res.numGuests}")
                    Text("Total Price: $${String.format("%.2f", res.totalPrice)} CAD")
                    Text("Confirmation ID: #${res.id.take(8)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can review or manage this anytime under 'Active Reservations'.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmedReservationForDialog = null
                        selectedTabIndex = 0 // Switch to active reservations view
                    }
                ) {
                    Text("View in My Reservations")
                }
            }
        )
    }

    // Decline Offer Confirmation Dialog (US 01.04.03)
    offerToDecline?.let { offer ->
        AlertDialog(
            onDismissRequest = { offerToDecline = null },
            title = { Text("Decline Reservation Offer?") },
            text = {
                Text(
                    "Are you sure you want to decline this reservation offer for \"${offer.lotteryTitle}\"? " +
                            "This cannot be undone, and the campsite will be offered to the next entrant on the waiting list."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.declineOffer(offer.id)
                        offerToDecline = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("You declined the reservation offer.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Decline Offer")
                }
            },
            dismissButton = {
                TextButton(onClick = { offerToDecline = null }) {
                    Text("Keep Offer")
                }
            }
        )
    }

    // Cancellation Dialog
    reservationToCancel?.let { res ->
        AlertDialog(
            onDismissRequest = { reservationToCancel = null },
            title = { Text("Cancel Reservation?") },
            text = {
                Text("Are you sure you want to cancel your stay at ${res.campsiteName} (${res.dateRange})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.cancelReservation(res.id)
                        reservationToCancel = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Reservation cancelled.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToCancel = null }) {
                    Text("Keep Booking")
                }
            }
        )
    }
}

/**
 * UI Component displaying the list of active confirmed reservations (US 01.06.01).
 */
@Composable
fun ActiveReservationsList(
    reservations: List<Reservation>,
    onCancelClick: (Reservation) -> Unit,
    onGoToLotteryWins: () -> Unit
) {
    if (reservations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No Active Reservations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You do not currently have any confirmed bookings.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoToLotteryWins) {
                    Text("Check Lottery Wins")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reservations, key = { it.id }) { res ->
                ReservationCard(reservation = res, onCancel = { onCancelClick(res) })
            }
        }
    }
}

/**
 * Card representing an individual active reservation.
 */
@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.campsiteName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reservation.status.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (reservation.campsiteLocation.isNotBlank()) {
                Text(
                    text = reservation.campsiteLocation,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Dates: ${reservation.dateRange}", fontWeight = FontWeight.Medium)
            Text(text = "Guests: ${reservation.numGuests}", fontSize = 14.sp)
            Text(
                text = "Total: $${String.format("%.2f", reservation.totalPrice)} CAD",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Booking ID: #${reservation.id.take(8)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel Reservation")
                }
            }
        }
    }
}

/**
 * UI Component displaying winning lottery invitations awaiting entrant booking (US 01.04.01, US 01.04.02, US 01.04.03).
 */
@Composable
fun LotteryOffersList(
    offers: List<LotteryOffer>,
    onDecline: (LotteryOffer) -> Unit,
    onConfirm: (offer: LotteryOffer, campsite: CampsiteSummary, chosenDate: String, numGuests: Int) -> Unit
) {
    if (offers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No Pending Lottery Wins",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "When you are selected in an active lottery, your booking invitation with available dates will appear here.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(offers, key = { it.id }) { offer ->
                LotteryOfferCard(
                    offer = offer,
                    onDecline = { onDecline(offer) },
                    onConfirm = { campsite, chosenDate, numGuests ->
                        onConfirm(offer, campsite, chosenDate, numGuests)
                    }
                )
            }
        }
    }
}

/**
 * Interactive card displaying a lottery win offer with date and campsite choices.
 */
@Composable
fun LotteryOfferCard(
    offer: LotteryOffer,
    onDecline: () -> Unit,
    onConfirm: (campsite: CampsiteSummary, chosenDate: String, numGuests: Int) -> Unit
) {
    var selectedCampsiteIndex by remember { mutableIntStateOf(0) }
    var selectedDateRange by remember {
        mutableStateOf(offer.availableDateRanges.firstOrNull() ?: "")
    }
    val currentCampsite = offer.campsites.getOrElse(selectedCampsiteIndex) { offer.campsites.first() }
    var guestCount by remember { mutableIntStateOf(2) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Winning Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = offer.lotteryTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LOTTERY WINNER",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Multi-campsite chooser (US 01.04.02)
            if (offer.campsites.size > 1) {
                Text(
                    text = "Select Campsite Option:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                offer.campsites.forEachIndexed { index, campsite ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCampsiteIndex = index }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedCampsiteIndex == index),
                            onClick = { selectedCampsiteIndex = index }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(campsite.name, fontWeight = FontWeight.Medium)
                            Text("${campsite.location} • $${campsite.pricePerNight}/night", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Text(
                    text = currentCampsite.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${currentCampsite.location} • $${currentCampsite.pricePerNight}/night",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                if (currentCampsite.description.isNotBlank()) {
                    Text(
                        text = currentCampsite.description,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Available Dates Selector (US 01.04.01)
            Text(
                text = "Available Dates for Stay (US 01.04.01):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            offer.availableDateRanges.forEach { dateRange ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDateRange = dateRange }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedDateRange == dateRange),
                        onClick = { selectedDateRange = dateRange }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dateRange, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Number of Guests selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Guests (Max ${currentCampsite.maxOccupancy}):", fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { if (guestCount > 1) guestCount-- },
                        enabled = guestCount > 1
                    ) {
                        Text("-")
                    }
                    Text(
                        text = "$guestCount",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    OutlinedButton(
                        onClick = { if (guestCount < currentCampsite.maxOccupancy) guestCount++ },
                        enabled = guestCount < currentCampsite.maxOccupancy
                    ) {
                        Text("+")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Estimated Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Total (2 nights):", fontWeight = FontWeight.Medium)
                Text(
                    "$${String.format("%.2f", currentCampsite.pricePerNight * 2)} CAD",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (US 01.04.03 Decline & US 01.05.01 Accept)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = {
                        if (selectedDateRange.isNotBlank()) {
                            onConfirm(currentCampsite, selectedDateRange, guestCount)
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    enabled = selectedDateRange.isNotBlank()
                ) {
                    Text("Accept & Reserve")
                }
            }
        }
    }
}
