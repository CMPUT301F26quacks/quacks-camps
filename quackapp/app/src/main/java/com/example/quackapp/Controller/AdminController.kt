package com.example.quackapp.Controller

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
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
 * Controller for the Admin screens (Admin Home, Manage Campsites, Image Gallery).
 *
 * Listens to every campsite and user in Firestore so the admin can see system stats,
 * browse all campsites, and browse/remove uploaded campsite images.
 *
 * Supports:
 * US 03.04.01 - browse campsites (list used by Manage Campsites).
 * US 03.06.01 - browse uploaded images so they can be removed.
 *
 * Outstanding: profile images (US 01.08.02) are not listed yet because profile pictures
 * are not stored on the user document on dev. Add them here once that field exists.
 */
class AdminController : ViewModel() {
    private val db = Firebase.firestore
    private val imgStorage = Firebase.storage

    var deleteError by mutableStateOf<String?>(null)
        private set

    /** Every campsite in the app, live. */
    val campsites: StateFlow<List<Campsite>> = callbackFlow {
        val listener = db.collection("campsites")
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

    /** Number of registered user profiles, live. Shown in the "System Stats Overview". */
    val userCount: StateFlow<Int> = callbackFlow {
        val listener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Removes a campsite's promotional image.
     *
     * Clears the image URL on the campsite document first (so the gallery updates right away),
     * then deletes the file from Firebase Storage.
     *
     * @param campsite the [Campsite] whose image should be removed.
     */
    fun deleteImage(campsite: Campsite) {
        viewModelScope.launch {
            try {
                db.collection("campsites").document(campsite.campId)
                    .update("optionalPromoImage", "").await()
                try {
                    imgStorage.reference.child("campsites/${campsite.campId}.jpg").delete().await()
                } catch (e: Exception) {
                    // A missing file shouldn't make the removal look like it failed
                    Log.w("AdminController", "Couldn't delete image file", e)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                deleteError = e.message ?: "Couldn't delete image"
            }
        }
    }

    fun deleteCampsite(campsite: Campsite) {
        viewModelScope.launch {
            try {
                // Delete the document first; the list updates automatically
                db.collection("campsites").document(campsite.campId).delete().await()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                deleteError = e.message ?: "Couldn't delete campsite"
            }
        }
    }
}

/**
 * Keeps only campsites that have an uploaded image and whose name or location match the search.
 *
 * @param campsites all campsites.
 * @param query text typed into the gallery search bar (case-insensitive, blank = everything).
 * @return campsites to show as tiles in the Image Gallery.
 */
fun imageGalleryItems(campsites: List<Campsite>, query: String): List<Campsite> =
    campsites.filter { camp ->
        camp.optionalPromoImage.isNotBlank() &&
            (camp.name.contains(query.trim(), ignoreCase = true) ||
                camp.location.contains(query.trim(), ignoreCase = true))
    }
