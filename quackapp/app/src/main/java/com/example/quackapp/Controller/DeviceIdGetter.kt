package com.example.quackapp.Controller

import com.google.firebase.installations.FirebaseInstallations

/**
 * Gets the Firebase-Installation ID (FID) for this installation of the app, linking it to a User.
 * Users with new IDs will be prompted to create a User Profile, while known devices are automatically signed back in.
 */
object DeviceIdGetter {
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