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
 * Controller for Campsite Owners' Home screen.
 *
 *
 *
 */
class OwnerHomeController(savedStateHandle: SavedStateHandle): ViewModel(){
    private val db = Firebase.firestore
    private val ownerId: String = checkNotNull(savedStateHandle["ownerId"])

    val camps: StateFlow<List<Campsite>> = callbackFlow {
        val listener = db.collection("campsites")
            .whereEqualTo("ownerID", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Campsite::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}