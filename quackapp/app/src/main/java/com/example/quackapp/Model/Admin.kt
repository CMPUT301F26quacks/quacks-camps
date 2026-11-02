package com.example.quacksites.model

class Admin: User(
    var deviceId: String,
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var notificationsEnabled: Boolean = true
){

}