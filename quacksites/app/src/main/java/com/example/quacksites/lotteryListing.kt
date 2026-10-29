package com.example.quacksites

// the owner creates the lottery listing
class lotteryListing(
    val lotteryId: String,
    val campsiteId: String,
    val ownerId: String,
    val registrationStartDate: String,
    val registrationEndDate: String,
    val bookingStartDate: String,
    val bookingEndDate: String,
    val waitListCapacity: Int,
    val status: String,
    val maxWinners: Int
) {

    // validating all values
    fun validateListing(): validateResult {
        if (campsiteId.isBlank()) {
            return validateResult(false, "Campsite ID cannot be empty.")
        }
        if (ownerId.isBlank()) { // should be same as the user logged in
            return validateResult(false, "Owner ID cannot be empty.")
        }

        if(waitListCapacity == 0 || waitListCapacity < -1){
            return validateResult(false, "Waitlist must be greater than 0")
        }

        if(registrationStartDate.isBlank() || registrationEndDate.isBlank()){
            return validateResult(false, "Registration start and end period are required.")
        }

        if(bookingStartDate.isBlank() || bookingEndDate.isBlank()){
            return validateResult(false, "Booking start and end period are required.")
        }

        if(maxWinners < 1){
            return validateResult(false, "Max Winners must be atleast 1.")
        }
        return validateResult(true, "Valid")

    }

    // checks if registration is open for entrants

    fun isRegistrationOpen(): Boolean {
        return status.equals("Open", true)
    }

    // checks if the waitlist is full
    fun isWaitlistFull(currentCount: Int): Boolean{
        if(waitListCapacity == -1) return false
        return currentCount >= waitListCapacity
    }
}
data class validateResult(
    val isValid: Boolean,
    val errorMessage: String
)