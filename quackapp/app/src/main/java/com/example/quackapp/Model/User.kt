package com.example.quackapp.Model

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

/**
 *
 * @property deviceId
 *
 *
 */

open class User(
    val deviceId: String,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val notificationsEnabled: Boolean
) {

    // temporary deviceId getter, as per US 01.09.01
    // will revisit after confirming with TA, Firebase-based approach might make more sense
    // FID (Firebase-installation ID)

    @SuppressLint("HardwareIds") // suppressing warning, Android documentation suggests app-scope identifier over device-scope
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