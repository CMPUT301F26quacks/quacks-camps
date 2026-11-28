package com.example.quackapp.Controller

import android.util.Log
import androidx.compose.ui.platform.LocalLocale
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Provides campsite and lottery data from firebase as well as useful functions
 *
 * Pulls campsite from campsite database for entrant
 * Pulls all lotteries from lottery database for entrant
 * Functions enterLottery and leaveLottery for adding and removing userId from firebase
 *
 * @param savedStateHandle Contains the camp id
 */
class EntrantDetailedCampsiteController(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val db = Firebase.firestore
    private val campId: String = checkNotNull(savedStateHandle["campId"])

    val campsite: StateFlow<Campsite?> = callbackFlow {
        //Looks for matching campId
        val listener = db.collection("campsites")
            .document(campId)
            .addSnapshotListener { snapshot, error ->
                //If there's an error then stop
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                //Otherwise, it will be ready to be collected
                trySend(snapshot?.toObject(Campsite::class.java))
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val lotteries: StateFlow<List<Lottery>> = callbackFlow {
        //Looks for lottery
        val listener = db.collection("lotteries")
            .addSnapshotListener { snapshot, error ->
                //If there's an error then stop
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                //Otherwise, it will be ready to be collected
                trySend(snapshot?.toObjects(Lottery::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    /**
     * Enters the entrant into the lottery waitlist
     * [US 01.02.01] As an entrant, I want to join the waiting list
     * for a campsite lottery.
     *
     * @param lotteryId the id of the lottery being entered
     * @param deviceId the id of the device entering that lottery's waitlist
     */
    fun enterLottery(lotteryId: String, deviceId: String) {
        Firebase.firestore.collection("lotteries").document(lotteryId)
            .update("waitlist", FieldValue.arrayUnion(deviceId))
    }

    /**
     * Removes the entrant's  from the lottery waitlist
     * [US 01.02.02] As an entrant, I want to leave the waiting list
     * for a campsite lottery.
     *
     * @param lotteryId the id of the lottery being left
     * @param deviceId the id of the device leaving that lottery's waitlist
     */
    fun leaveLottery(lotteryId: String, deviceId: String) {
        Firebase.firestore.collection("lotteries").document(lotteryId)
            .update("waitlist", FieldValue.arrayRemove(deviceId))
    }

    /**
     * Checks if user is in a particular lottery
     * [US 01.02.02] As an entrant, I want to leave the waiting list
     * for a campsite lottery.
     *
     * @param lotteryId the id of the lottery being left
     * @param currentDeviceId the id of the device leaving that lottery's waitlist
     * @param lotteries all the lotteries
     */
    fun checkIfIdInLottery(
        lotteryId: String?,
        currentDeviceId: String?,
        lotteries: List<Lottery>
    ): Boolean {
        return lotteries.any { lottery ->
            lottery.lotteryID == lotteryId &&
                    lottery.waitlist.contains(currentDeviceId)
        }
    }


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

                if (!querySnapshot.isEmpty) {
                    //update existing review for this user and campsite
                    val existingReview = querySnapshot.documents[0].id
                    reviewsRef.document(existingReview).update("rating", updatedRating).await()
                } else {   //create new review for this user and this campsite
                    val newReview = mapOf(
                        "campsiteID" to campId,
                        "entrantID" to entrantId,
                        "rating" to updatedRating
                    )
                    reviewsRef.add(newReview).await()
                }

                //recalculate the overall campsite rating
                CampsiteRatingUpdater.recompute(campId)
            } catch (e: Exception) {
                //handle error
                Log.e(
                    "EntrantDetailedCampsiteController",
                    "Failed to update rating for campId: $campId",
                    e
                )
            }
        }
    }

    /**
     * Submit an entrant's written review for a campsite.
     * Uses the same "reviews" collection in Firebase.
     *
     * @param feedback The written review text
     * @param entrantId Entrant's ID
     */
    fun submitReview(feedback: String, entrantId: String) {
        viewModelScope.launch {
            try {
                val newReview = mapOf(
                    "campsiteID" to campId,
                    "entrantID" to entrantId,
                    "feedback" to feedback
                )
                db.collection("reviews").add(newReview).await()
            } catch (e: Exception) {
                Log.e(
                    "EntrantDetailedCampsiteController",
                    "Failed to submit review feedback for campId: $campId",
                    e
                )
            }
        }
    }


    /**
     * Edit an entrant's existing  review od a campsite.
     *
     * @param updatedReview The updated review text
     * @param entrantId Entrant's ID
     */
    fun updateReview(updatedReview: String, entrantId: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("reviews")
                    .whereEqualTo("campsiteID", campId)
                    .whereEqualTo("entrantID", entrantId)
                    .get()
                    .await()

                //edit the same document that getUserReview loads
                querySnapshot.documents
                    .firstOrNull { it.getString("feedback") != null }
                    ?.reference
                    ?.update("feedback", updatedReview)
                    ?.await()
            } catch (e: Exception) {
                Log.e(
                    "EntrantDetailedCampsiteController",
                    "Failed to update review for campId: $campId",
                    e
                )
            }
        }
    }


    /**
     * Get an entrant's existing review for this campsite.
     *
     * @param entrantId Entrant's ID
     * @param onSuccess Gives the review text, or null if there isn't one
     * @param onFail Called if the review couldn't be loaded
     */
    fun getUserReview(
        entrantId: String,
        onSuccess: (String?) -> Unit,
        onFail: () -> Unit
    ) {
        viewModelScope.launch {
            try {

                val querySnapshot = db.collection("reviews")
                    .whereEqualTo("campsiteID", campId)
                    .whereEqualTo("entrantID", entrantId)
                    .get()
                    .await()

                val existingFeedback = querySnapshot.documents
                    .firstNotNullOfOrNull { it.getString("feedback") }

                onSuccess(existingFeedback)
            } catch (e: Exception) {
                Log.e(
                    "EntrantDetailedCampsiteController",
                    "Failed to load review for campId: $campId",
                    e
                )
                onFail()
            }
        }
    }


    /**
     * Takes a campsite's available dates and converts it to text.
     *
     * Ex: 12373297327, 213761325479, 31264913277 to December 24th - December 26th
     *
     * @param campsite campsite whose availability is formatted
     * @param locale locale used for month names
     * @return the formatted range, or "No dates available" if there are no dates
     */
    fun availabilityToText(campsite: Campsite, locale: Locale): String {
        val start = campsite.availability.minOrNull() ?: return "No dates available"
        val end = campsite.availability.maxOrNull() ?: return "No dates available"

        fun format(millis: Long, pattern: String): String =
            SimpleDateFormat(pattern, locale)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date(millis))

        return when {
            //Ex: Dec 5, 2026
            format(start, "yyyyMMdd") == format(end, "yyyyMMdd") ->
                format(start, "MMM d, yyyy")

            //Ex: Dec 5-8, 2026
            format(start, "yyyyMM") == format(end, "yyyyMM") ->
                "${format(start, "MMM d")} - ${format(end, "d, yyyy")}"

            //Ex: Nov 29 - Dec 3, 2026
            format(start, "yyyy") == format(end, "yyyy") ->
                "${format(start, "MMM d")} - ${format(end, "MMM d, yyyy")}"

            //Ex: Dec 30, 2026 - Jan 3, 2027
            else ->
                "${format(start, "MMM d, yyyy")} - ${format(end, "MMM d, yyyy")}"
        }
    }
}