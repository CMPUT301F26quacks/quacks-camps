package com.example.quackapp

import com.example.quackapp.Controller.LotteryDraw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for picking lottery winners.
 *
 * US 02.05.01: As a campsite owner, I want the system to automatically draw winners after the registration period ends.
 */
class LotteryDrawTest {

    private val waitlist = listOf("a", "b", "c", "d", "e")

    @Test
    fun testPicksTheRequestedNumberOfWinnersFromTheWaitlist() {
        val winners = LotteryDraw.pickWinners(waitlist, 3, Random(1))
        assertEquals(3, winners.size)
        assertTrue(waitlist.containsAll(winners))
    }

    @Test
    fun testEveryoneWinsWhenWaitlistIsSmallerThanWinnerCount() {
        assertEquals(waitlist.toSet(), LotteryDraw.pickWinners(waitlist, 10, Random(1)).toSet())
    }

    @Test
    fun testIgnoresBlankAndDuplicateEntries() {
        val winners = LotteryDraw.pickWinners(listOf("", "a", "a", "b"), 5, Random(1))
        assertEquals(setOf("a", "b"), winners.toSet())
        assertEquals(2, winners.size)
    }

    @Test
    fun testEmptyWaitlistHasNoWinners() {
        assertTrue(LotteryDraw.pickWinners(emptyList(), 2).isEmpty())
    }
}
