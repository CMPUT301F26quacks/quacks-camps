package com.example.quackapp.Controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/**
 * Fetches all campsites and lotteries for EntrantHome
 */
class EntrantHomeController: ViewModel(){
    private val db = Firebase.firestore

    val camps: StateFlow<List<Campsite>> = callbackFlow {
        //Looks for campsite
        val listener = db.collection("campsites")
            .addSnapshotListener { snapshot, error ->
                //If there's an error then stop
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                //Otherwise, it will be ready to be collected
                trySend(snapshot?.toObjects(Campsite::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


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
}