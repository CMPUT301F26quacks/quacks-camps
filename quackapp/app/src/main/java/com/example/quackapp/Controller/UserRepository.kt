package com.example.quackapp.Controller

import com.example.quackapp.Model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * Repository responsible for storing and retrieving User profiles in Firestore.
 *
 * User profiles are stored in the "users" collection and are indexed with the user's FID.
 */

object UserRepository {
    /**
     * Retrieves the user profile associated with a Firebase Installation ID.
     *
     * The server is checked directly so a recently deleted profile is not
     * accidentally loaded from the local Firestore cache.
     *
     * @param deviceId FID used as the user's Firestore document ID.
     * @param onResult called with the matching [User], or null if no profile exists.
     * @param onFail called if Firestore cannot complete the request.
     */
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

    /**
     * Creates a new user profile in Firestore.
     *
     * @param user complete [User] profile to store.
     * @param onSuccess called after the profile is successfully created.
     * @param onFail called if the profile cannot be created.
     */
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

    /**
     * Updates editable information for an existing user profile.
     *
     * The user's device ID and assigned roles are intentionally left unchanged.
     *
     * Supports US 01.07.03 and US 01.10.01.
     *
     * @param deviceId FID identifying the profile to update.
     * @param name updated user name.
     * @param email updated email address.
     * @param phoneNumber updated phone number.
     * @param notificationsEnabled updated notification preference.
     * @param onSuccess called when the update succeeds.
     * @param onFail called when the update fails.
     */
    fun updateUser(
        deviceId: String,
        name: String,
        email: String,
        phoneNumber: String,
        notificationsEnabled: Boolean,
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
                    "phoneNumber" to phoneNumber,
                    "notificationsEnabled" to notificationsEnabled
                )
            )

            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFail(exception)
            }

    }

    /**
     * Deletes the user's profile and personal information from Firestore.
     *
     * The Firebase Installation ID itself remains assigned to the app installation.
     * If the app is reopened, no matching profile will be found and the user can
     * register again.
     *
     * Supports US 01.08.01.
     *
     * @param deviceId FID identifying the profile to delete.
     * @param onSuccess called when deletion succeeds.
     * @param onFail called when deletion fails.
     */
    fun deleteUser(
        deviceId: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {

        val database = FirebaseFirestore.getInstance()

        database.collection("users")
            .document(deviceId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                exception -> onFail(exception)
            }

    }
}