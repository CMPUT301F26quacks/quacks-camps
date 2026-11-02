package com.example.quacksites.model

//status of reservation - for filtering later yay
enum class ReservationStatus { PENDING_CONFIRMATION, CONFIRMED, REJECTED, CANCELLED, PAST, ACTIVE }

class Reservation(
    var reservationID: String = "",     //firebase db related

    var campsiteID: String = "",
    var entrantID: String = "",
    var campsiteOwnerID: String = "",

    var startDate: String = "",
    var endDate: String = "",
    var status: ReservationStatus = ReservationStatus.PENDING_CONFIRMATION
)