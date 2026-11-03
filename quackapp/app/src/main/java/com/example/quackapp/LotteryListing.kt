package com.example.quackapp

// the owner creates the lottery listing
class LotteryListing(
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
    fun ValidateListing(): ValidateResult {
        if (campsiteId.isBlank()) {
            return ValidateResult(false, "Campsite ID cannot be empty.")
        }
        if (ownerId.isBlank()) { // should be same as the user logged in
            return ValidateResult(false, "Owner ID cannot be empty.")
        }

        if(waitListCapacity == 0 || waitListCapacity < -1){
            return ValidateResult(false, "Waitlist must be greater than 0")
        }

        if(registrationStartDate.isBlank() || registrationEndDate.isBlank()){
            return ValidateResult(false, "Registration start and end period are required.")
        }

        if(bookingStartDate.isBlank() || bookingEndDate.isBlank()){
            return ValidateResult(false, "Booking start and end period are required.")
        }

        if(maxWinners < 1){
            return ValidateResult(false, "Max Winners must be atleast 1.")
        }
        return ValidateResult(true, "Valid")

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
data class ValidateResult(
    val isValid: Boolean,
    val errorMessage: String
)