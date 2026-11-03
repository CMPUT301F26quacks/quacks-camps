package com.example.quackapp.Controller

import com.example.quackapp.Model.User
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
                    document ->
                onResult(document.exists())
            }

            .addOnFailureListener { // If Firestore cannot complete the search, return error
                    exception ->
                onFail(exception)
            }
    }

    fun getUser(
        deviceId: String,
        onResult: (User?) -> Unit,
        onFail: (Exception) -> Unit
    ) {
        val database = FirebaseFirestore.getInstance()

        // Look for the profile whose document ID matches this device ID.
        database.collection("users")
            .document(deviceId)
            .get()

            .addOnSuccessListener { document ->

                if (document.exists()) {

                    // Convert the Firestore document into a User object.
                    val user = document.toObject(User::class.java)

                    onResult(user)

                } else {

                    // null means there is no profile registered to this ID.
                    onResult(null)
                }
            }

            .addOnFailureListener { exception ->
                onFail(exception)
            }
    }
}