package com.example.quackapp.View.Entrant

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.EntrantReviewsController
import com.example.quackapp.Model.Review

/**
 * Screen where entrants can view all ratings and reviews for a campsite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrantCampsiteReviewsScreen(
    navController: NavController,
    viewModel: EntrantReviewsController = viewModel()
) {
    val reviews by viewModel.reviews.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews for ${viewModel.campsiteName}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (reviews.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ratings or reviews yet")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reviews, key = { it.reviewID }) { review ->
                        EntrantReviewCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun EntrantReviewCard(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF248AFF), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        /**
         * connect the id to the name of the entrant instead i think thats more intuitie
         */
        Text("Entrant: ${review.entrantID.take(8)}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        if (review.rating > 0F) {
            Text("⭐ %.1f".format(review.rating), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (review.feedback.isNotBlank()) {
            Text(review.feedback, color = Color.White, fontSize = 16.sp)
        }
    }
}
