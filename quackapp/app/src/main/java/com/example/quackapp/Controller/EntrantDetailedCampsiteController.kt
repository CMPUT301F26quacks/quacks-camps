package com.example.quackapp.Controller

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
}