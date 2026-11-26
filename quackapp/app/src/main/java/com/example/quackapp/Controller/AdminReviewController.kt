package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Review
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Controller for the admin "Ratings & Reviews" screen of one campsite.
 *
 * One document in the "reviews" collection holds an entrant's star `rating` and/or written
 * `feedback` for a campsite. An admin can remove either part separately; when nothing is left
 * the whole document is deleted. Removing a rating recalculates the campsite's average.
 *
 * Supports:
 * US 03.09.01 - remove campsite ratings that violate app policy.
 * US 03.10.01 - remove campsite reviews that violate app policy.
 *
 * @property savedStateHandle holds the "campId" route argument.
 */
class AdminReviewController(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val db = Firebase.firestore
    private val campId: String = checkNotNull(savedStateHandle["campId"])

    var campsiteName by mutableStateOf("")
        private set
    var actionError by mutableStateOf<String?>(null)
        private set

    /** Every rating/review left on this campsite, live. A [Review.rating] of 0 means "no rating". */
    val reviews: StateFlow<List<Review>> = callbackFlow {
        val listener = db.collection("reviews")
            .whereEqualTo("campsiteID", campId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Review(
                        rating = doc.getDouble("rating")?.toFloat() ?: 0F,
                        feedback = doc.getString("feedback") ?: "",
                        reviewID = doc.id,
                        campsiteID = campId,
                        entrantID = doc.getString("entrantID") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                campsiteName = db.collection("campsites").document(campId).get().await()
                    .getString("name") ?: ""
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actionError = e.message ?: "Couldn't load campsite"
            }
        }
    }

    /**
     * Removes the star rating from a review and recalculates the campsite's average rating.
     * US 03.09.01
     *
     * @param review the review whose rating violates app policy.
     */
    fun removeRating(review: Review) {
        actionError = null
        viewModelScope.launch {
            try {
                removeField(review, field = "rating", otherPartEmpty = review.feedback.isBlank())
                CampsiteRatingUpdater.recompute(campId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actionError = e.message ?: "Couldn't remove rating"
            }
        }
    }

    /**
     * Removes the written review text but keeps the entrant's star rating.
     * US 03.10.01
     *
     * @param review the review whose text violates app policy.
     */
    fun removeReview(review: Review) {
        actionError = null
        viewModelScope.launch {
            try {
                removeField(review, field = "feedback", otherPartEmpty = review.rating == 0F)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actionError = e.message ?: "Couldn't remove review"
            }
        }
    }

    /** Deletes [field] from the review document, or the whole document if nothing else is left in it. */
    private suspend fun removeField(review: Review, field: String, otherPartEmpty: Boolean) {
        val doc = db.collection("reviews").document(review.reviewID)
        if (otherPartEmpty) {
            doc.delete().await()
        } else {
            doc.update(field, FieldValue.delete()).await()
        }
    }
}
