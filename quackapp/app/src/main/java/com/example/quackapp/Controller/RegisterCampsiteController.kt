package com.example.quackapp.Controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.quackapp.Model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Represents the state of the campsite registration, mostly for the optional
 * promo image upload.
 *
 * The UI observes this to decide what to display (for example, a progress
 * indicator while [Saving] or an error message for [Error]).
 */
sealed interface SaveStatus {
    object Idle : SaveStatus
    object Saving : SaveStatus
    object Success : SaveStatus
    data class Error(val message: String) : SaveStatus
}

/**
 * Controller for the "Register Campsite" screen.
 *
 * Holds the form state, validates it, and saves a new [Campsite] to Firestore.
 * If the user picked a promo image, it is uploaded to Firebase Storage first and
 * its download URL is stored on the [Campsite] document.
 *
 */
class RegisterCampsiteController: ViewModel(){
    // Firestore database instance
    private val db = Firebase.firestore

    // Reference to the campsites collection in Firestore
    private val campsitesRef = db.collection("campsites")

    // Firebase Storage instance used for promo image uploads
    private val imgStorage = Firebase.storage

    var name by mutableStateOf("")
    var location by mutableStateOf("")
    var price by mutableIntStateOf(0)
    var capacity by mutableIntStateOf(0)
    var imageUri by mutableStateOf<Uri?>(null)
    var features by mutableStateOf(listOf(""))
        private set
    var rules by mutableStateOf(listOf(""))
    var cancellationPolicy by mutableStateOf("")
    var tags by mutableStateOf(listOf("Pet Friendly", "Smoke Free", "Luxurious", "Back Camping"))
    var selectedTags by mutableStateOf(setOf<String>())
        private set

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
    private val _status = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val status: StateFlow<SaveStatus> = _status

    /**
     * Validates and adds a new Campsite object to the Firestore.
     * Campsite objects may have images which are stored into Storage and can be linked to their campsite object via unique ID
     */
    fun addCampsite(){
        // get the uploaded image's uri
        val uri = imageUri
        // Validate necessary fields
        if (name.isBlank()) {
            _status.value = SaveStatus.Error("Campsite must have a name")
            return
        }
        else if (location.isBlank()) {
            _status.value = SaveStatus.Error("Campsite must have a location")
            return
        }
        else if (price <= 0) {
            _status.value = SaveStatus.Error("Price must be non-zero")
            return
        }
        else if (capacity <= 0) {
            _status.value = SaveStatus.Error("Price must be non-zero")
            return
        }


        // Start a coroutine that allows code to pause and await without freezing UI
        viewModelScope.launch {
            // Change status to saving
            _status.value = SaveStatus.Saving
            try {
                // Create database references, any image and campsite object will be linked by their ids (document name)
                val docRef = campsitesRef.document()
                val imageRef = imgStorage.reference.child("campsites/${docRef.id}.jpg")

                // Upload the image if one was selected
                var url = ""
                if (uri != null) {
                    imageRef.putFile(uri).await()
                    // get the download url
                    url = imageRef.downloadUrl.await().toString()
                }
                // Make a new campsite object to store
                val campsite = Campsite(
                    campId = docRef.id,
                    name = name.trim(),
                    location = location.trim(),
                    optionalPromoImage = url,
                    price = price,
                    capacity = capacity,
                    cancellationPolicy = cancellationPolicy,
                    features = features,
                    rules = rules,
                    tags = selectedTags.toList()
                )
                // upload to firestore
                docRef.set(campsite).await()
                _status.value = SaveStatus.Success
            }
            catch (e: Exception) {
                _status.value = SaveStatus.Error(e.message ?: "Something went wrong")
            }

        }

    }

    /**
     * Clears all form fields back to their defaults.
     * Typically called after a successful save so the form is ready for a new entry.
     */
    fun resetForm() {
        name = ""
        location = ""
        imageUri = null
        price = 0
        capacity = 0
        cancellationPolicy = ""
        features = listOf("")
        rules = listOf("")
        selectedTags = setOf<String>()
        _status.value = SaveStatus.Idle
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
}