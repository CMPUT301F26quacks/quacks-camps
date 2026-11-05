package com.example.quackapp.Controller

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quackapp.Model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException


class EditCampsiteController(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val campsiteId: String = checkNotNull(savedStateHandle["campsiteId"])
    private val campsiteRef = Firebase.firestore.collection("campsites").document(campsiteId)
    private val imgStorage = Firebase.storage

    var name by mutableStateOf("")
    var location by mutableStateOf("")
    var price by mutableStateOf("")
    var capacity by mutableStateOf("")
    var features by mutableStateOf(listOf(""))
        private set
    var rules by mutableStateOf(listOf(""))
    var cancellationPolicy by mutableStateOf("")
    var tags by mutableStateOf(listOf("Pet Friendly", "Smoke Free", "Luxurious", "Back Camping"))
    var selectedTags by mutableStateOf(setOf<String>())
        private set

    // Newly picked image, or null if the user hasn't changed it.
    var imageUri by mutableStateOf<Uri?>(null)

    // Image already saved on the campsite, shown until a new one is picked.
    var existingImageUrl by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    private val _status = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val status: StateFlow<SaveStatus> = _status

    init {
        viewModelScope.launch {
            try {
                val campsite = campsiteRef.get().await().toObject(Campsite::class.java)
                if (campsite != null) {
                    name = campsite.name
                    location = campsite.location
                    price = campsite.price.toString()
                    capacity = campsite.capacity.toString()
                    existingImageUrl = campsite.optionalPromoImage
                    features = campsite.features
                    rules = campsite.rules
                    cancellationPolicy = campsite.cancellationPolicy
                    selectedTags = campsite.tags.toSet()
                } else {
                    _status.value = SaveStatus.Error("Campsite not found")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Couldn't load campsite")
            } finally {
                isLoading = false
            }
        }

    }

    fun saveChanges() {
        if (name.isBlank() || location.isBlank() || capacity.toInt() <= 0 || price.toInt() <= 0) {
            _status.value = SaveStatus.Error("Fill in all fields and capacity and price must be non-zero")
            return
        }

        viewModelScope.launch {
            _status.value = SaveStatus.Saving
            try {
                // Only upload if the user picked a new image; otherwise keep the old URL
                val newUri = imageUri
                val imageUrl = if (newUri != null) {
                    val imageRef = imgStorage.reference.child("campsites/$campsiteId.jpg")
                    imageRef.putFile(newUri).await()
                    imageRef.downloadUrl.await().toString()
                } else {
                    existingImageUrl
                }

                // update() changes only these fields and leaves the rest of the document alone
                campsiteRef.update(
                    mapOf(
                        "name" to name.trim(),
                        "location" to location.trim(),
                        "price" to price.toInt(),
                        "capacity" to capacity.toInt(),
                        "optionalPromoImage" to imageUrl,
                        "cancellationPolicy" to cancellationPolicy,
                        "features" to features,
                        "rules" to rules,
                        "tags" to selectedTags.toList()
                    )
                ).await()

                _status.value = SaveStatus.Success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }
        }
    }
    /**
     * Adds another element to the amentities/features list
     */
    fun addFeature() { features = features + "" }
    /**
     * Updates an element of the amentities/features list at the index
     * @param index Used to find the element to update
     * @param value The string that will replace the element in the list
     */
    fun updateFeature(index: Int, value: String) {
        features = features.toMutableList().also { it[index] = value }
    }
    /**
     * Deletes an element of the amentities/features list at the index
     * @param index Used to find the element to delete
     */
    fun removeFeature(index: Int) {
        features = features.toMutableList().also { it.removeAt(index) }
    }
    /**
     * Adds another element to the rules list
     */
    fun addRule() { rules = rules + "" }
    /**
     * Updates an element of the rules list at the index
     * @param index Used to find the element to update
     * @param value The string that will replace the element in the list
     */
    fun updateRule(index: Int, value: String) {
        rules = rules.toMutableList().also { it[index] = value }
    }
    /**
     * Deletes an element of the rules list at the index
     * @param index Used to find the element to delete
     */
    fun removeRule(index: Int) {
        rules = rules.toMutableList().also { it.removeAt(index) }
    }
    /**
     * Toggles the selection of a tag
     * @param tag Identifies the element to toggle
     */
    fun toggleTag(tag: String) {
        selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
    }
    /**
     * Adds a custom Tag to the tags list
     * @param raw Name of the new tag
     */
    fun addCustomTag(raw: String) {
        val tag = raw.trim()
        if (tag.isEmpty()) return
        // Case-insensitive duplicate check
        val existing = tags.firstOrNull { it.equals(tag, ignoreCase = true) }
        if (existing == null) {
            tags = tags + tag
            selectedTags = selectedTags + tag   // auto-select new tags
        } else {
            selectedTags = selectedTags + existing
        }
    }
}