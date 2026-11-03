package com.example.quackapp.Model

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

/**
 * Base User class representing any user role in the Campsite Reservation Application.
 * Supports passwordless identification using the Android device identifier (US 01.09.01).
 *
 * @property deviceId Unique persistent identifier for device-based authentication.
 * @property name Full name of the user profile.
 * @property email Email address for correspondence and confirmations.
 * @property phoneNumber Contact phone number.
 * @property notificationsEnabled User preference for receiving in-app and system notifications.
 */
open class User(
    var deviceId: String,
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var notificationsEnabled: Boolean = true
) {

    /**
     * Retrieves the persistent hardware-scoped or app-scoped device identifier.
     * US 01.09.01: As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
     *
     * @param context Application or activity context used to access system settings.
     * @return Unique Android ID string.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return requireNotNull(
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        ) {
            "Unable to retrieve Device ID"
        }
    }
}
