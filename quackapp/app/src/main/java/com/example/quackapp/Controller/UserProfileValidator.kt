package com.example.quackapp.Controller

import android.util.Patterns

/**
 * Validates user-entered profile information, ensuring a name is entered and phone numbers and
 * emails are properly formatted.
 */
object UserProfileValidator {
    /**
     * Ensures the user has entered a name.
     *
     * @return returns an error message if name is invalid.
     */
    fun validateName(name:String): String? {
        return if (name.trim().isEmpty()) {
            "Please enter your name"
        } else {
            null
        }
    }

    /**
     * Ensures the user has entered a valid email.
     *
     * @return returns an error message if email is invalid.
     */
    fun validateEmail(email: String): String? {
        return if (
            !Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()
        ) {
            "Please enter a valid email address"
        } else {
            null
        }
    }

    /**
     * Ensures phone numbers contain exactly 10 digits.
     *
     * Characters like dashes, brackets, and spaces are ignored for validation.
     *
     * @return returns an error if phone number is invalid.
     */
    fun validatePhoneNumber(phoneNumber: String): String? {

        val digits = phoneNumber.filter {it.isDigit()}

        val validCharacters = phoneNumber.all {
            it in "0123456789()- "
        }

        return if (digits.length != 10 || !validCharacters) {
            "Please enter a valid 10-digit phone number"
        } else {
            null
        }
    }
}