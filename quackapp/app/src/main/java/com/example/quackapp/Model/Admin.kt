package com.example.quackapp.model

import com.example.quackapp.Model.User

class Admin(
    deviceId: String,
    name: String = "",
    email: String = "",
    phoneNumber: String = "",
    notificationsEnabled: Boolean = true
): User(deviceId, name, email, phoneNumber, notificationsEnabled){

}