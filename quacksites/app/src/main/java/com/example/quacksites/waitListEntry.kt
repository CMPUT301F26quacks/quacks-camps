package com.example.quacksites

class waitListEntry(
    val entryId: String,
    val lotteryId: String,
    val entrantId: String,
    val status: String,
    val latitude: Double,
    val longitude: Double
) {

    // validate is all info is valid
    fun isValid(): Boolean{
        return entryId.isNotBlank() && lotteryId.isNotBlank() && entrantId.isNotBlank()
    }
    //waitlist status
    fun isWaitListed(): Boolean{
        return status.equals("Waitlisted", true )
    }
    fun isSelected(): Boolean{
        return status.equals("Selected", true)
    }
    //if entrant wants to cancel/winthdraw from the list?
}