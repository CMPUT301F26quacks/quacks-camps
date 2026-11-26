package com.example.quackapp.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 *  Controller for Campsite Owners' Home screen.
 *
 *  Contains logic for displaying only [Campsite] belonging to the logged in owner
 *  Contains functions related to editing existing [Campsite] and
 *  deleting campsites from the database
 *
 *  @property savedStateHandle a map which contains data passed from outside. Used here get ownerId
 */
class OwnerHomeController(savedStateHandle: SavedStateHandle): ViewModel(){
    private val db = Firebase.firestore
    var ownerId: String = savedStateHandle["ownerId"] ?: ""
        private set
    private val imgStorage = Firebase.storage
    var deleteError by mutableStateOf<String?>(null)
        private set
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    init {  //prevent the ownerId from ever being blank -> get the deviceId if that ever happens
        if (ownerId.isBlank()) {
            DeviceIdGetter.getDeviceId(
                onSuccess = { deviceId ->
                    ownerId = deviceId
                },
                onFail = { }
            )
        }
    }

    // define camps by going into the database in the campsites collection
    // and finding all documents that match the logged in userId
    val ownedCamps: StateFlow<List<Campsite>> = callbackFlow {
        if(ownerId.isNotBlank()){
            //if ownerId passed in route URL, use directly
            val listener = db.collection("campsites")
                .whereEqualTo("ownerId", ownerId)
                .addSnapshotListener { snapshot, error ->
                    if(error!=null){
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Campsite::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
        else{   //but if tapping on home button again, js use logged in device id
            var listener: ListenerRegistration? = null
            DeviceIdGetter.getDeviceId(
                onSuccess = {deviceId ->
                    listener = db.collection("campsites")
                        .whereEqualTo("ownerId", deviceId)
                        .addSnapshotListener {snapshot, error ->
                            if(error != null){
                                close(error)
                                return@addSnapshotListener
                            }
                            val list = snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    doc.toObject(Campsite::class.java)
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: emptyList()
                            trySend(list)
                        }
                },
                onFail = {
                    trySend(emptyList())
                }
            )
            awaitClose { listener?.remove() }
        }


    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val otherCamps: StateFlow<List<Campsite>> = callbackFlow {
        if(ownerId.isNotBlank()){
            //if ownerId passed in route URL, use directly
            val listener = db.collection("campsites")
                .whereNotEqualTo("ownerId", ownerId)
                .addSnapshotListener { snapshot, error ->
                    if(error!=null){
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Campsite::class.java)

                        } catch (_: Exception) {
                            null
                        }
                    }   ?.filter { it.lotteryId.isNullOrEmpty() }
                        ?: emptyList()

                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
        else{   //but if tapping on home button again, js use logged in device id
            var listener: ListenerRegistration? = null
            DeviceIdGetter.getDeviceId(
                onSuccess = {deviceId ->
                    listener = db.collection("campsites")
                        .whereNotEqualTo("ownerId", deviceId)
                        .addSnapshotListener {snapshot, error ->
                            if(error != null){
                                close(error)
                                return@addSnapshotListener
                            }
                            val list = snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    doc.toObject(Campsite::class.java)
                                } catch (_: Exception) {
                                    null
                                }
                            }   ?.filter { it.lotteryId.isNullOrEmpty() }
                                ?: emptyList()
                            trySend(list)
                        }
                },
                onFail = {
                    trySend(emptyList())
                }
            )
            awaitClose { listener?.remove() }
        }

    }   .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     *  Deletes a [Campsite] object from the database
     *
     *  What more is there really to say
     *
     *  @param campsite is the [Campsite] object we want to delete
     */
    fun deleteCampsite(campsite: Campsite) {
        viewModelScope.launch {
            try {
                // Delete the document first; the list updates automatically
                db.collection("campsites").document(campsite.campId).delete().await()

                // Then remove the image, if the campsite had one
                if (campsite.optionalPromoImage.isNotBlank()) {
                    try {
                        imgStorage.reference.child("campsites/${campsite.campId}.jpg").delete().await()
                    }
                    catch (e: Exception) {
                        // An orphaned or missing image shouldn't make the delete look like it failed
                        Log.w("OwnerHome", "Couldn't delete image", e)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                deleteError = e.message ?: "Couldn't delete campsite"
            }
        }
    }


    /**
     * Toggles the selection of the campsite with the given id
     * @param id firebase document id/campsite id
     */
    fun toggleSelection(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }

    /**
     * Clears all the selected campsites
     */
    fun clearSelection() { _selectedIds.value = emptySet() }

}