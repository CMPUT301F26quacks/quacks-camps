package com.example.quackapp.Controller

import android.util.Log
import com.example.quackapp.Model.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * Reads and stores User profile information from Firestore.
 */

object UserRepository {
    fun getUser(
        deviceId: String,
        onResult: (User?) -> Unit,
        onFail: (Exception) -> Unit
    ) {
        val database = FirebaseFirestore.getInstance()

        // Look for the profile whose document ID matches this device ID.
        database.collection("users")
            .document(deviceId)
            .get(Source.SERVER) // forces Firebase to check the server instead of local cache, which can still have recently deleted profiles

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

    fun createUser(
        user: User,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val database = FirebaseFirestore.getInstance()

        database.collection("users")
            .document(user.deviceId)
            .set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                exception -> onFail(exception)
            }
    }

    fun updateUser(
        deviceId: String,
        name: String,
        email: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val database = FirebaseFirestore.getInstance()

        database.collection("users")
            .document(deviceId)
            .update(
                mapOf( // creates a temporary map containing a list of key-value pairs for updating
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber
                )
            )

            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFail(exception)
            }

    }


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
}