package com.example.quackapp.Model

import com.example.quackapp.Controller.SaveStatus
/**
 * Represents the save state of data to the Firestore, mostly for the optional
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