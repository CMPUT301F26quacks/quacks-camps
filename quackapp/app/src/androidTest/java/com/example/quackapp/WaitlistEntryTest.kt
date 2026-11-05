package com.example.quackapp

import junit.framework.TestCase.assertTrue
import org.junit.Test

class WaitlistEntryTest {

    @Test
    fun TestValidEntry(){
        val entry = WaitlistEntry(
            entryId = "e_1",
            lotteryId = "l_1",
            entrantId = "user_1"
        )
        assertTrue(entry.isValid())
        //assertTrue(entry.isWaitListed())
        //assertTrue(entry.isSelected())

    }

}