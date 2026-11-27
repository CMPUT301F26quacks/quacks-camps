package com.example.quackapp.Controller

import android.util.Log
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

                //recalculate the overall campsite rating
                CampsiteRatingUpdater.recompute(campId)
            } catch (e: Exception) {
                //handle error
                Log.e("EntrantDetailedCampsiteController", "Failed to update rating for campId: $campId", e)
            }
        }
    }
}