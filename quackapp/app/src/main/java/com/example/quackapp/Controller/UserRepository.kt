package com.example.quackapp.Controller

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Reads and stores User profile information from Firestore.
 */

object UserRepository {

    fun userExists( //
        deviceId: String,
        onResult: (Boolean) -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val database = FirebaseFirestore.getInstance()

        database.collection("users") // Looks for a user profile with a matching deviceID
            .document(deviceId)
            .get()

            .addOnSuccessListener { // If matching user profile exists, return true
                document -> onResult(document.exists())
            }

            .addOnFailureListener { // If Firestore cannot complete the search, return error
                exception -> onFail(exception)
            }
    }
}