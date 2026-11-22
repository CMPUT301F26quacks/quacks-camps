package com.example.quackapp.Model

/**
 * Administrator actor in the Campsite Reservation System.
 *
 * Admins moderate the app (browse/remove campsites, profiles, images, ratings and reviews).
 * A user becomes an admin when "ADMIN" is in their [User.roles] list. There is no in-app
 * sign-up for admins; the role is granted in the Firestore console.
 *
 * An admin can also act as an entrant or campsite owner with the same profile (US 03.11.01),
 * because roles are a list and the same deviceId is used everywhere.
 *
 * @param deviceId Firebase Installation ID used to identify the admin's device.
 * @param name Full name of the admin.
 * @param email Contact email address.
 * @param phoneNumber Contact phone number.
 * @param notificationsEnabled Whether the admin receives notifications.
 * @param roles Roles held by this user, always including [ROLE].
 */
class Admin(
    deviceId: String,
    name: String = "",
    email: String = "",
    phoneNumber: String = "",
    notificationsEnabled: Boolean = true,
    roles: List<String> = listOf(ROLE)
) : User(deviceId, name, email, phoneNumber, notificationsEnabled, roles) {

    companion object {
        /** Role string stored in [User.roles] for administrators. */
        const val ROLE = "ADMIN"

        /**
         * Checks if a role list belongs to an admin.
         *
         * @param roles the roles stored on a [User].
         * @return true if the roles contain [ROLE].
         */
        fun isAdmin(roles: List<String>): Boolean = ROLE in roles
    }
}
