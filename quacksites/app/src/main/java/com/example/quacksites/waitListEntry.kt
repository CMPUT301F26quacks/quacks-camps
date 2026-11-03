package com.example.quacksites

enum class waitListStatus {WAITING, SELECTED, NOT_SELECTED, CANCELLED}

class waitListEntry(
    val entryId: String,
    val lotteryId: String,
    val entrantId: String,
    var status: waitListStatus = waitListStatus.WAITING
    // location
) {

    // validate is all info is valid
    fun isValid(): Boolean{
        return entryId.isNotBlank() && lotteryId.isNotBlank() && entrantId.isNotBlank()
    }
    //waitlist status
    fun isWaitListed(): Boolean{
        return status == waitListStatus.WAITING
    }
    fun isSelected(): Boolean{
        return status == waitListStatus.SELECTED
    }
    //if entrant wants to cancel/withdraw from the list
    fun cancelEntry() {
        status = waitListStatus.CANCELLED
    }
}