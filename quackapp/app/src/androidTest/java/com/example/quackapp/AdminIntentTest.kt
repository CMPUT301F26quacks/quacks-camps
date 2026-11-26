package com.example.quackapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Review
import com.example.quackapp.View.Admin.AdminCampsiteReviewsContent
import com.example.quackapp.View.Admin.AdminCampsitesContent
import com.example.quackapp.View.Admin.AdminHomeContent
import com.example.quackapp.View.Admin.AdminImageGalleryContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Intent (UI) tests for the admin screens.
 *
 * Composables are given fake data, so tests check the UI flow without reading or writing to the real Firestore database.
 *
 * Covers US 03.06.01, 03.09.01, 03.10.01 and 03.11.01.
 */
class AdminIntentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val tunnelMountain = Campsite(
        campId = "camp-banff-1", name = "Tunnel Mountain", location = "Banff, AB",
        price = 65, capacity = 8, optionalPromoImage = "https://example.com/tunnel.jpg"
    )
    private val whistlers = Campsite(
        campId = "camp-jasper-1", name = "Whistlers", location = "Jasper, AB",
        price = 48, capacity = 6, optionalPromoImage = "https://example.com/whistlers.jpg"
    )
    private val noImageCamp = Campsite(
        campId = "camp-hinton-1", name = "Hinton Hideaway", location = "Hinton, AB",
        price = 30, capacity = 4, optionalPromoImage = ""
    )

    @Test
    fun adminHomeShowsStatsAndOpensImageGallery() {
        var opened = ""
        composeRule.setContent {
            AdminHomeContent(
                campCount = 3,
                userCount = 12,
                onManageCampsites = { opened = "campsites" },
                onManageMedia = { opened = "media" },
                onSwitchProfile = {}
            )
        }
        composeRule.onNodeWithText("System Stats Overview").assertIsDisplayed()
        composeRule.onNodeWithText("3 Camps", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("12 Users", substring = true).assertIsDisplayed()

        composeRule.onNodeWithText("Manage Uploaded Media").assertIsEnabled().performClick()
        assertEquals("media", opened)
        composeRule.onNodeWithText("Manage Campsites").performClick()
        assertEquals("campsites", opened)
    }

    @Test
    fun adminHomeToolsWithoutScreensAreDisabled() {
        composeRule.setContent {
            AdminHomeContent(campCount = 0, userCount = 0, onSwitchProfile = {})
        }
        composeRule.onNodeWithText("Manage Messages").assertIsNotEnabled()
        composeRule.onNodeWithText("All Notifications").assertIsNotEnabled()
    }

    /** US 03.11.01: admin switches to an entrant profile. */
    @Test
    fun switchProfileToEntrant() {
        var route = ""
        composeRule.setContent {
            AdminHomeContent(campCount = 0, userCount = 0, onSwitchProfile = { route = it })
        }
        composeRule.onNodeWithText("Switch Profile").performClick()
        composeRule.onNodeWithText("Entrant").performClick()
        assertEquals("EntrantHome", route)
    }

    /** US 03.11.01: admin switches to a campsite owner profile. */
    @Test
    fun switchProfileToCampsiteOwner() {
        var route = ""
        composeRule.setContent {
            AdminHomeContent(campCount = 0, userCount = 0, onSwitchProfile = { route = it })
        }
        composeRule.onNodeWithText("Switch Profile").performClick()
        composeRule.onNodeWithText("Campsite Owner").performClick()
        assertEquals("OwnerHome", route)
    }

    /** US 03.06.01: browse images, open one, delete it, and it disappears from the gallery. */
    @Test
    fun galleryBrowseOpenAndDeleteImage() {
        composeRule.setContent {
            var camps by remember { mutableStateOf(listOf(tunnelMountain, whistlers, noImageCamp)) }
            AdminImageGalleryContent(
                campsites = camps,
                onDeleteImage = { deleted ->
                    camps = camps.map { if (it.campId == deleted.campId) noImage(it) else it }
                }
            )
        }
        composeRule.onNodeWithText("Tunnel Mountain").assertIsDisplayed()
        composeRule.onNodeWithText("Whistlers").assertIsDisplayed()
        composeRule.onNodeWithText("Hinton Hideaway").assertDoesNotExist()

        composeRule.onNodeWithContentDescription("Image of Tunnel Mountain").performClick()
        composeRule.onNodeWithContentDescription("Full image of Tunnel Mountain").assertIsDisplayed()
        composeRule.onNodeWithText("Delete image").performClick()
        composeRule.onNodeWithTag("confirmDeleteImage").performClick()

        composeRule.onNodeWithText("Image Gallery").assertIsDisplayed()
        composeRule.onNodeWithText("Tunnel Mountain").assertDoesNotExist()
        composeRule.onNodeWithText("Whistlers").assertIsDisplayed()
    }

    /** US 03.06.01: cancelling the confirmation keeps the image. */
    @Test
    fun galleryCancelDeleteKeepsImage() {
        var deleted: Campsite? = null
        composeRule.setContent {
            AdminImageGalleryContent(campsites = listOf(whistlers), onDeleteImage = { deleted = it })
        }
        composeRule.onNodeWithContentDescription("Image of Whistlers").performClick()
        composeRule.onNodeWithText("Delete image").performClick()
        composeRule.onNodeWithText("Cancel").performClick()
        assertEquals(null, deleted)
        composeRule.onNodeWithContentDescription("Full image of Whistlers").assertIsDisplayed()
    }

    @Test
    fun gallerySearchFiltersByName() {
        composeRule.setContent {
            AdminImageGalleryContent(campsites = listOf(tunnelMountain, whistlers), onDeleteImage = {})
        }
        composeRule.onNodeWithTag("gallerySearch").performTextInput("whist")
        composeRule.onNodeWithText("Whistlers").assertIsDisplayed()
        composeRule.onNodeWithText("Tunnel Mountain").assertDoesNotExist()
    }

    /** US 03.06.01: search also works with the location, and clearing it shows everything again */
    @Test
    fun gallerySearchByLocationThenClear() {
        composeRule.setContent {
            AdminImageGalleryContent(campsites = listOf(tunnelMountain, whistlers), onDeleteImage = {})
        }
        composeRule.onNodeWithTag("gallerySearch").performTextInput("banff")
        composeRule.onNodeWithText("Tunnel Mountain").assertIsDisplayed()
        composeRule.onNodeWithText("Whistlers").assertDoesNotExist()

        composeRule.onNodeWithTag("gallerySearch").performTextClearance()
        composeRule.onNodeWithText("Tunnel Mountain").assertIsDisplayed()
        composeRule.onNodeWithText("Whistlers").assertIsDisplayed()
    }

    @Test
    fun manageCampsitesOpensCampsite() {
        var clicked: Campsite? = null
        composeRule.setContent {
            AdminCampsitesContent(campsites = listOf(tunnelMountain, whistlers), onCampsiteClick = { clicked = it })
        }
        composeRule.onNodeWithText("Whistlers").performClick()
        assertEquals("camp-jasper-1", clicked?.campId)
    }

    @Test
    fun manageCampsitesDeleteCampsite() {   //test whether campsite was deleted
        composeRule.setContent {
            var camps by remember { mutableStateOf(listOf(tunnelMountain)) }
            AdminCampsitesContent(
                campsites = camps,
                onCampsiteClick = {},
                onDeleteCampsite = { deleted ->
                    camps = camps.filterNot { it.campId == deleted.campId }
                }
            )
        }
        composeRule.onNodeWithText("Tunnel Mountain").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Remove campsite").performClick()
        composeRule.onNodeWithText("Delete campsite?").assertIsDisplayed()
        composeRule.onNodeWithTag("confirmDelete").performClick()
        composeRule.onNodeWithText("Tunnel Mountain").assertDoesNotExist()
    }

    @Test
    fun manageCampsitesCancelDeleteKeepsCampsite() {
        var deleted: Campsite? = null
        composeRule.setContent {
            AdminCampsitesContent(
                campsites = listOf(tunnelMountain),
                onCampsiteClick = {},
                onDeleteCampsite = { deleted = it }
            )
        }
        composeRule.onNodeWithContentDescription("Remove campsite").performClick()
        composeRule.onNodeWithText("Cancel").performClick()
        assertEquals(null, deleted)
        composeRule.onNodeWithText("Tunnel Mountain").assertIsDisplayed()
    }

    /** US 03.09.01: remove an abusive rating. */
    @Test
    fun removeRatingFromReview() {
        composeRule.setContent {
            var reviews by remember { mutableStateOf(sampleReviews()) }
            AdminCampsiteReviewsContent(
                campsiteName = "Tunnel Mountain",
                reviews = reviews,
                onRemoveRating = { r -> reviews = reviews.filterNot { it.reviewID == r.reviewID } },
                onRemoveReview = {}
            )
        }
        composeRule.onNodeWithText("★ 1.0").assertIsDisplayed()

        // second review (rating only, no text) has a single "Remove rating" button
        composeRule.onNodeWithText("★ 5.0").assertIsDisplayed()
        composeRule.onAllNodesWithText("Remove rating")[0].performClick()
        composeRule.onNodeWithText("These actions can't be undone!").assertIsDisplayed()
        composeRule.onNodeWithTag("confirmRemove").performClick()

        composeRule.onNodeWithText("★ 1.0").assertDoesNotExist()
        composeRule.onNodeWithText("★ 5.0").assertIsDisplayed()
    }

    /** US 03.10.01: remove written review text but keep the rating. */
    @Test
    fun removeReviewTextKeepsRating() {
        composeRule.setContent {
            var reviews by remember { mutableStateOf(sampleReviews()) }
            AdminCampsiteReviewsContent(
                campsiteName = "Tunnel Mountain",
                reviews = reviews,
                onRemoveRating = {},
                onRemoveReview = { r ->
                    reviews = reviews.map { if (it.reviewID == r.reviewID) Review(it.rating, "", it.reviewID, it.campsiteID, it.entrantID) else it }
                }
            )
        }
        composeRule.onNodeWithText("Worst campsite ever, owner is a scam!!!").assertIsDisplayed()
        composeRule.onNodeWithText("Remove review").performClick()
        composeRule.onNodeWithTag("confirmRemove").performClick()

        composeRule.onNodeWithText("Worst campsite ever, owner is a scam!!!").assertDoesNotExist()
        composeRule.onNodeWithText("★ 1.0").assertIsDisplayed()
    }

    @Test
    fun reviewsEmptyState() {
        composeRule.setContent {
            AdminCampsiteReviewsContent(campsiteName = "Whistlers", reviews = emptyList(), onRemoveRating = {}, onRemoveReview = {})
        }
        composeRule.onNodeWithText("No ratings or reviews yet").assertIsDisplayed()
    }

    private fun sampleReviews() = listOf(
        Review(1F, "Worst campsite ever, owner is a scam!!!", "rev-1", "camp-banff-1", "entrant-aaaa1111"),
        Review(5F, "", "rev-2", "camp-banff-1", "entrant-bbbb2222")
    )

    private fun noImage(c: Campsite) = Campsite(
        campId = c.campId, name = c.name, location = c.location, price = c.price,
        capacity = c.capacity, optionalPromoImage = ""
    )
}
