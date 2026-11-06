package com.example.quackapp.Controller

import com.google.firebase.installations.FirebaseInstallations

/**
 * Gets the Firebase-Installation ID (FID) for this installation of the app, for use in linking to a User.
 * Users with new IDs will be prompted to create a User Profile, while known devices are automatically signed back in.
 * Allows users to be identfied without a username and password, supporting US 01.09.01
 */
object DeviceIdGetter {
    /**
     * Gets the Firebase Installation ID for the current app installation.
     *
     * @param onSuccess called with the FID if Firebase successfully retrieves it.
     * @param onFail called with the exception if the ID cannot be retrieved.
     */
    fun getDeviceId(
        onSuccess: (String) -> Unit,
        onFail: (Exception) -> Unit
    ) {
        FirebaseInstallations.getInstance().id
            .addOnSuccessListener {
                deviceId -> onSuccess(deviceId)
            }
            .addOnFailureListener {
                exception -> onFail(exception)
            }

    }
}