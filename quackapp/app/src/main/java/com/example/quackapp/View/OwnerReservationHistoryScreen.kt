package com.example.quackapp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.quackapp.Model.ReservationRepository
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationStatus

/**
 * Reservation history tab on bottom navigation bar for campsite owner
 * [US 02.07.01] As a campsite owner, I want to view a list of active reservations.
 * [US 02.07.02] As a campsite owner, I want to view a list of past reservations.
 * [US 02.07.03] As a campsite owner, I want to view a list of cancelled reservations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReservationHistoryScreen(
    navController: NavController,
    repository: ReservationRepository = remember { ReservationRepository.getInstance() },
    ownerViewModel: OwnerHomeController = viewModel()   //get only the owner's campsites
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val reservations by repository.reservations.collectAsState()
    val camps by ownerViewModel.camps.collectAsState()

    //ids of campsites belonging to the campsite owner
    val ownerCampsiteIds = remember(camps) { camps.map { it.campId } }

    //filter reservations for this campsite owner
    val ownerReservations = remember(reservations, ownerCampsiteIds) {
        if (ownerCampsiteIds.isEmpty()) reservations
        else reservations.filter { it.campsiteId in ownerCampsiteIds }
    }


    // filtering the reservations by status (active,  past, cancelled)
    val activeReservations = remember(ownerReservations) {
        reservations.filter { it.status == ReservationStatus.CONFIRMED }
    }
    val pastReservations = remember(ownerReservations) {
        reservations.filter { it.status == ReservationStatus.COMPLETED }
    }
    val cancelledReservations = remember(ownerReservations) {
        reservations.filter { it.status == ReservationStatus.CANCELLED || it.status == ReservationStatus.DECLINED }
    }

    //var reservationToReject by remember { mutableStateOf<Reservation?>(null) } - ill add this once i implement the reject booking user story

    OwnerScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Campsite Reservations", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Owner History", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
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
                .padding(innerPadding)
        ) {
            // tabs for the status of the reservations
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Active (${activeReservations.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Past (${pastReservations.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            "Cancelled (${cancelledReservations.size})",
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            when (selectedTabIndex) {
                0 -> OwnerReservationsList(
                    reservations = activeReservations,
                    emptyMessage = "No Active Reservations"
                    //below is for when i implement the reject a reservation user story
                    // showRejectButton = true,
                    //onRejectClick = { res -> reservationToReject = res }

                )

                1 -> OwnerPastReservationsList(
                    reservations = pastReservations,
                    emptyMessage = "No Past Reservations",
                )

                2 -> OwnerCancelledReservationsList(
                    reservations = cancelledReservations,
                    emptyMessage = "No Cancelled Reservations"
                )
            }
        }
    }
}
/*AGAIN: will work on this more when needed
    // Reject Reservation Dialog (US 02.08.01)
    reservationToReject?.let { res ->
        AlertDialog(
            onDismissRequest = { reservationToReject = null },
            title = { Text("Reject Reservation?") },
            text = { Text("Are you sure you want to reject the reservation for ${res.entrantName} at ${res.campsiteName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.cancelReservation(res.id) // Cancels/rejects the reservation
                        reservationToReject = null
                    }
                ) {
                    Text("Reject Reservation")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToReject = null }) { Text("Cancel") }
            }
        )
    }*/


/**
 * UI Component displaying the list of active confirmed reservations 02.07.01.
 */
@Composable
fun OwnerReservationsList(  //active
    reservations: List<Reservation>,
    emptyMessage: String
    //showRejectButton: Boolean,
    //onRejectClick: (Reservation) -> Unit
){
    if (reservations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reservations, key = { it.id }) { res ->
                OwnerReservationCard(
                    reservation = res
                )
            }
        }
    }
}

@Composable
fun OwnerReservationCard(
    reservation: Reservation,
){
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
                Text(reservation.campsiteName, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFE8603C),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(reservation.status.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Booked by: ${reservation.entrantName}", fontWeight = FontWeight.Medium)
            Text("Dates: ${reservation.dateRange}", fontSize = 14.sp)
            Text("Guests: ${reservation.numGuests} • Total: $${String.format("%.2f", reservation.totalPrice)} CAD", fontSize = 14.sp)
        }
    }
}

    /**
     * Past reservations List (US 02.07.02)
      */
@Composable
fun OwnerPastReservationsList(
    reservations: List<Reservation>,
    emptyMessage: String
) {
    if(reservations.isEmpty()){
        Box(
            modifier= Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
    else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reservations, key = { it.id }) { res ->
                OwnerPastReservationCard(
                    reservation = res
                )
            }
        }
    }
}
    /**
     * Card for past reservation
     * */
    @Composable
fun OwnerPastReservationCard(
    reservation: Reservation
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
                Text(reservation.campsiteName, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFE8603C),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(reservation.status.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Booked by: ${reservation.entrantName}", fontWeight = FontWeight.Medium)
            Text("Dates: ${reservation.dateRange}", fontSize = 14.sp)
            Text("Guests: ${reservation.numGuests} • Total: $${String.format("%.2f", reservation.totalPrice)} CAD", fontSize = 14.sp)
        }
    }
}

@Composable
fun OwnerCancelledReservationsList(
    reservations: List<Reservation>,
    emptyMessage: String
) {
    if (reservations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
    else {
        LazyColumn(
            modifier=Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reservations, key = { it.id }) { res ->
                OwnerCancelledReservationCard(
                    reservation = res
                )
            }
        }
    }
}


@Composable
fun OwnerCancelledReservationCard(
    reservation: Reservation
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
                Text(reservation.campsiteName, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFE8603C),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(reservation.status.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Booked by: ${reservation.entrantName}", fontWeight = FontWeight.Medium)
            Text("Dates: ${reservation.dateRange}", fontSize = 14.sp)
            Text("Guests: ${reservation.numGuests} • Total: $${String.format("%.2f", reservation.totalPrice)} CAD", fontSize = 14.sp)
        }
    }
}