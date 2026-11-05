package com.example.quackapp

enum class WaitlistStatus {WAITING, SELECTED, NOT_SELECTED, CANCELLED, ACCEPTED, DECLINED}

class WaitlistEntry(
    val entryId: String,
    val lotteryId: String,
    val entrantId: String,
    var status: WaitlistStatus = WaitlistStatus.WAITING
    // location
) {

    // validate is all info is valid
    fun isValid(): Boolean{
        return entryId.isNotBlank() && lotteryId.isNotBlank() && entrantId.isNotBlank()
    }
    //waitlist status
    fun isWaitListed(): Boolean{
        return status == WaitlistStatus.WAITING
    }

    fun isSelected(): Boolean{
        return status == WaitlistStatus.SELECTED
    }
    //if entrant wants to cancel/withdraw from the list
    fun cancelEntry() {
        status = WaitlistStatus.CANCELLED
    }

    // get a notification when they enter the waitlist

}