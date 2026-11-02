package com.example.quacksites.model

class CampsiteOwner: User(
    var deviceId: String,
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var notificationsEnabled: Boolean = true
){
    //notification preference
    fun toggleNotificationPreference() {
        notificationsEnabled = !notificationsEnabled
    }


}