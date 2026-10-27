package com.example.quackapp.Model

import android.app.Notification
import android.companion.DeviceId

class Entrant(
    deviceId: String,
    name: String,
    email: String,
    phoneNumber: String,
    notificationsEnabled: Boolean

): User(deviceId, name, email, phoneNumber, notificationsEnabled) {
}