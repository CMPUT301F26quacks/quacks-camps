package com.example.quackapp

import com.example.quackapp.Controller.CampsiteRatingUpdater
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the campsite average rating calculation.
 *
 * US 01.13.01 (entrant rates a campsite) and US 03.09.01 (admin removes a rating)
 * both rely on this average.
 */
class CampsiteRatingTest {

    @Test
    fun testAverageOfSeveralRatings() {
        assertEquals(4.0, CampsiteRatingUpdater.averageRating(listOf(5.0, 4.0, 3.0)), 0.0001)
    }

    @Test
    fun testSingleRating() {
        assertEquals(2.5, CampsiteRatingUpdater.averageRating(listOf(2.5)), 0.0001)
    }

    /** US 03.09.01: after an admin removes the last rating, the campsite goes back to 0. */
    @Test
    fun testNoRatingsLeftIsZero() {
        assertEquals(0.0, CampsiteRatingUpdater.averageRating(emptyList()), 0.0001)
    }

    /** Removing one bad rating raises the average back up. */
    @Test
    fun testRemovingOutlierChangesAverage() {
        val before = CampsiteRatingUpdater.averageRating(listOf(5.0, 5.0, 1.0))
        val after = CampsiteRatingUpdater.averageRating(listOf(5.0, 5.0))
        assertEquals(3.6667, before, 0.001)
        assertEquals(5.0, after, 0.0001)
    }
}
