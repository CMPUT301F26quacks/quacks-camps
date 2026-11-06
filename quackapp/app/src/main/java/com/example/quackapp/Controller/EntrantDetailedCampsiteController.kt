package com.example.quackapp.Controller

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
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

/**
 * Pulls campsite from campsite database for entrant
 *
 * @param savedStateHandle Contains the camp id
 */
class EntrantDetailedCampsiteController(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val db = Firebase.firestore
    private val campId: String = checkNotNull(savedStateHandle["campId"])

    val campsite: StateFlow<Campsite?> = callbackFlow {
        //Looks for matching campId
        val listener = db.collection("campsites")
            .whereEqualTo("campId", campId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                //If there's an error then stop
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                //Otherwise, it will be ready to be collected
                trySend(snapshot?.toObjects(Campsite::class.java)?.firstOrNull())
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Save or update the entrant's rating for a campsite
     * Recalculates the average/rating of a campsite
     * [US 01.13.01] As an entrant, I want to give a rating for a campsite.
     *
     * @param updatedRating The new rating to be saved
     * @param entrantId Entrant's ID
     */
    fun updateRating(updatedRating: Double, entrantId: String) {
        viewModelScope.launch {
            try {
                val reviewsRef = db.collection("reviews")

                val querySnapshot = reviewsRef
                    .whereEqualTo("campsiteID", campId)
                    .whereEqualTo("entrantID", entrantId)
                    .get()
                    .await()

                if(!querySnapshot.isEmpty){
                    //update existing review for this user and campsite
                    val existingReview = querySnapshot.documents[0].id
                    reviewsRef.document(existingReview).update("rating", updatedRating).await()
                }
                else{   //create new review for this user and this campsite
                    val newReview = mapOf(
                        "campsiteID" to campId,
                        "entrantID" to entrantId,
                        "rating" to updatedRating
                    )
                    reviewsRef.add(newReview).await()
                }

                //get all reviews for this campsite
                val allReviews = reviewsRef
                    .whereEqualTo("campsiteID", campId)
                    .get()
                    .await()

                val ratings = allReviews.documents.mapNotNull { it.getDouble("rating") }
                if(ratings.isNotEmpty()){
                    val averageRating = ratings.average()

                    //update overall campsite rating field
                    db.collection("campsites")
                        .document(campId)
                        .update("rating", averageRating)
                        .await()
                }
            } catch (e: Exception) {
                //handle error
                Log.e("EntrantDetailedCampsiteController", "Failed to update rating for campId: $campId", e)
            }
        }
    }
}