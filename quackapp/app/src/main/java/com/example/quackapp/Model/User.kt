package com.example.quackapp.Model

/**
 * Base User class representing any user role in the Campsite Reservation Application.
 * Supports passwordless identification using the Android device identifier (US 01.09.01).
 *
 * @property deviceId Unique persistent identifier for device-based authentication.
 * @property name Full name of the user profile.
 * @property email Email address for correspondence and confirmations.
 * @property phoneNumber Contact phone number.
 * @property notificationsEnabled User preference for receiving in-app and system notifications.
 * @property roles A list of roles this user has been assigned, ENTRANT, OWNER, and/or ADMIN
 */
open class User(
    var deviceId: String = "",
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var notificationsEnabled: Boolean = true,
    var roles: List<String> = emptyList()
) {

}
