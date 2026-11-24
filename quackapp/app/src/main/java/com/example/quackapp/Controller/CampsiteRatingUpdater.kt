package com.example.quackapp.Controller

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Keeps a campsite's overall `rating` field in sync with its documents in the "reviews" collection.
 *
 * Used when an entrant rates a campsite (US 01.13.01) and when an admin removes a rating
 * (US 03.09.01), so both places calculate the average the same way.
 */
object CampsiteRatingUpdater {

    /**
     * Average of the given ratings.
     *
     * @param ratings every star rating left on a campsite.
     * @return the mean, or 0.0 when there are no ratings left.
     */
    fun averageRating(ratings: List<Double>): Double =
        if (ratings.isEmpty()) 0.0 else ratings.average()

    /**
     * Reads all reviews for a campsite and writes the new average to `campsites/{campId}.rating`.
     * Review documents without a rating (e.g. rating removed by an admin) are ignored.
     *
     * @param campId ID of the campsite to update.
     */
    suspend fun recompute(campId: String) {
        val db = Firebase.firestore
        val reviews = db.collection("reviews")
            .whereEqualTo("campsiteID", campId)
            .get()
            .await()
        val ratings = reviews.documents.mapNotNull { it.getDouble("rating") }
        db.collection("campsites")
            .document(campId)
            .update("rating", averageRating(ratings))
            .await()
    }
}
