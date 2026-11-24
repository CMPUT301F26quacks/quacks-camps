package com.example.quackapp.View.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.AdminController
import com.example.quackapp.Model.Campsite

/**
 * Admin "Manage Campsites" screen: a searchable list of every campsite in the app.
 * Tapping a campsite opens its ratings and reviews for moderation.
 *
 * Supports US 03.04.01 (browse campsites) as the entry point to US 03.09.01 / 03.10.01.
 *
 * Outstanding: the "X" remove-campsite button from the mockup belongs to US 03.01.01
 * and can be added to [AdminCampsiteCard].
 *
 * @param navController used to open a campsite's reviews and by the nav bar.
 * @param viewModel provides every campsite.
 */
@Composable
fun AdminCampsites(
    navController: NavController,
    viewModel: AdminController = viewModel()
) {
    val campsites by viewModel.campsites.collectAsState()

    AdminScaffold(navController, title = "Campsites") { innerPadding ->
        AdminCampsitesContent(
            modifier = Modifier.padding(innerPadding),
            campsites = campsites,
            onCampsiteClick = { navController.navigate("AdminCampsiteReviews/${it.campId}") }
        )
    }
}

/**
 * Stateless list body so it can be intent tested with fake campsites.
 *
 * @param campsites every campsite.
 * @param onCampsiteClick called when the admin taps a campsite card.
 */
@Composable
fun AdminCampsitesContent(
    modifier: Modifier = Modifier,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val shown = campsites.filter { it.name.contains(query.trim(), ignoreCase = true) }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search...") },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminYellow,
                unfocusedBorderColor = AdminYellow
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
        if (shown.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No campsites found")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.campId }) { camp ->
                    AdminCampsiteCard(camp, onClick = { onCampsiteClick(camp) })
                }
            }
        }
    }
}

/**
 * Blue campsite card from the mockup: name, location and price per night.
 *
 * @param campsite campsite to show.
 * @param onClick called when the card is tapped.
 */
@Composable
fun AdminCampsiteCard(campsite: Campsite, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminBlue, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(campsite.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "${campsite.location}    $${campsite.price} per night",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
