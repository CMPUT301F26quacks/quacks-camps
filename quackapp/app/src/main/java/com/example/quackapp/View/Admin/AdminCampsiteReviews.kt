package com.example.quackapp.View.Admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quackapp.Controller.AdminReviewController
import com.example.quackapp.Model.Review

/**
 * Admin screen listing every rating and review on one campsite, with buttons to remove them.
 *
 * Supports:
 * US 03.09.01 - remove campsite ratings that violate app policy.
 * US 03.10.01 - remove campsite reviews that violate app policy.
 *
 * @param navController used by the admin nav bar.
 * @param viewModel loads the reviews and performs removals.
 */
@Composable
fun AdminCampsiteReviews(
    navController: NavController,
    viewModel: AdminReviewController = viewModel()
) {
    val reviews by viewModel.reviews.collectAsState()
    val context = LocalContext.current

    // Let the admin know if a removal didn't go through
    LaunchedEffect(viewModel.actionError) {
        viewModel.actionError?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    AdminScaffold(navController, title = "Reviews") { innerPadding ->
        AdminCampsiteReviewsContent(
            modifier = Modifier.padding(innerPadding),
            campsiteName = viewModel.campsiteName,
            reviews = reviews,
            onRemoveRating = { viewModel.removeRating(it) },
            onRemoveReview = { viewModel.removeReview(it) }
        )
    }
}

/** Which part of a review the admin is about to remove. */
private enum class RemoveTarget { RATING, REVIEW }

/**
 * Stateless body of the reviews screen so it can be intent tested with fake reviews.
 *
 * @param campsiteName name shown under the title.
 * @param reviews reviews on this campsite. A rating of 0 means the entrant left no rating.
 * @param onRemoveRating called after the admin confirms removing a rating.
 * @param onRemoveReview called after the admin confirms removing written review text.
 */
@Composable
fun AdminCampsiteReviewsContent(
    modifier: Modifier = Modifier,
    campsiteName: String,
    reviews: List<Review>,
    onRemoveRating: (Review) -> Unit,
    onRemoveReview: (Review) -> Unit
) {
    var pending by remember { mutableStateOf<Pair<Review, RemoveTarget>?>(null) }

    Column(modifier.fillMaxSize()) {
        Text(
            campsiteName,
            fontSize = 18.sp,
            color = AdminOrange,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (reviews.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ratings or reviews yet")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reviews, key = { it.reviewID }) { review ->
                    ReviewCard(
                        review = review,
                        onRemoveRating = { pending = review to RemoveTarget.RATING },
                        onRemoveReview = { pending = review to RemoveTarget.REVIEW }
                    )
                }
            }
        }
    }

    pending?.let { (review, target) ->
        val what = if (target == RemoveTarget.RATING) "rating" else "review"
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Remove $what?") },
            text = { Text("These actions can't be undone!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (target == RemoveTarget.RATING) onRemoveRating(review) else onRemoveReview(review)
                        pending = null
                    },
                    modifier = Modifier.testTag("confirmRemove")
                ) { Text("Remove", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Blue card for one review: star rating, written review, and remove buttons for each part present.
 */
@Composable
private fun ReviewCard(review: Review, onRemoveRating: () -> Unit, onRemoveReview: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminBlue, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Entrant ${review.entrantID.take(8)}", color = Color.White, fontSize = 12.sp)
        if (review.rating > 0F) {
            Text("★ %.1f".format(review.rating), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (review.feedback.isNotBlank()) {
            Text(review.feedback, color = Color.White)
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            if (review.rating > 0F) {
                TextButton(onClick = onRemoveRating) { Text("Remove rating", color = Color.White) }
            }
            if (review.feedback.isNotBlank()) {
                TextButton(onClick = onRemoveReview) { Text("Remove review", color = Color.White) }
            }
        }
    }
}
