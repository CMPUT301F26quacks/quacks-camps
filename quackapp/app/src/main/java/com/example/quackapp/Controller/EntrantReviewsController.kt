package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Review
import com.google.firebase.Firebase
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
 * Controller for entrants to view all ratings and reviews for a specific campsite.
 */
class EntrantReviewsController(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val db = Firebase.firestore
    private val campId: String = checkNotNull(savedStateHandle["campId"])

    var campsiteName by mutableStateOf("")
        private set

    /** list of all ratings and reviews */
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
                campsiteName = ""
            }
        }
    }
}
