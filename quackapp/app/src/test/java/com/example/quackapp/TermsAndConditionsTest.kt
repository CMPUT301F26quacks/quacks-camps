package com.example.quackapp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

//Unit tests for campsite rules and policy confirmation before reservation.

class TermsAndConditionsTest {

    private fun canConfirmReservation(selectedDateRange: String, policyAccepted: Boolean): Boolean {
        return selectedDateRange.isNotBlank() && policyAccepted
    }

    @Test
    fun testCannotConfirmWhenPolicyBoxIsUnchecked() {
        assertFalse(canConfirmReservation("2026-12-01 to 2026-12-03", policyAccepted = false))
    }

    @Test
    fun testCanConfirmAfterCheckingPolicyBox() {
        assertTrue(canConfirmReservation("2026-12-01 to 2026-12-03", policyAccepted = true))
    }

    @Test
    fun testUncheckingPolicyBoxBlocksConfirmationAgain() {
        var policyAccepted = true
        assertTrue(canConfirmReservation("2026-12-01 to 2026-12-03", policyAccepted))

        policyAccepted = false
        assertFalse(canConfirmReservation("2026-12-01 to 2026-12-03", policyAccepted))
    }
}