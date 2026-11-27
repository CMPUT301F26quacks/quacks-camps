package com.example.quackapp

import com.example.quackapp.Controller.reviewWillBeEmpty
import com.example.quackapp.Model.Review
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for when an admin removal deletes the whole review doc.
 *
 * US 03.09.01 (remove ratings) and US 03.10.01 (remove reviews).
 */
class RemoveReviewTest {

    @Test
    fun testRemovingRatingKeepsDocIfThereIsFeedback() {
        val review = Review(rating = 4F, feedback = "nice camp")
        assertFalse(reviewWillBeEmpty(review, "rating"))
    }


    @Test
    fun testRemovingFeedbackKeepsDocIfThereIsRating() {
        val review = Review(rating = 4F, feedback = "nice camp")
        assertFalse(reviewWillBeEmpty(review, "feedback"))
    }


    /** rating only review, removing the rating leaves nothing so the doc gets deleted */
    @Test
    fun testRemovingOnlyRatingDeletesDoc() {
        val review = Review(rating = 2F, feedback = "")
        assertTrue(reviewWillBeEmpty(review, "rating"))
    }


    /** feedback only review (rating 0 means no rating) */
    @Test
    fun testRemovingOnlyFeedbackDeletesDoc() {
        val review = Review(rating = 0F, feedback = "bad words")
        assertTrue(reviewWillBeEmpty(review, "feedback"))
    }


    /** rating already removed, now removing the review too should delete the whole doc */
    @Test
    fun testRemovingRatingThenReviewDeletesDoc() {
        val review = Review(rating = 5F, feedback = "spam")
        assertFalse(reviewWillBeEmpty(review, "rating"))

        // after the rating is gone the screen gets the review back with rating 0
        val afterRatingRemoved = Review(rating = 0F, feedback = "spam")
        assertTrue(reviewWillBeEmpty(afterRatingRemoved, "feedback"))
    }


    /** feedback thats just spaces counts as empty */
    @Test
    fun testBlankFeedbackCountsAsEmpty() {
        val review = Review(rating = 3F, feedback = "   ")
        assertTrue(reviewWillBeEmpty(review, "rating"))
    }
}
