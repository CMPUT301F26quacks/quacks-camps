package com.example.quackapp

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.quackapp.Controller.EntrantDetailedCampsiteController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.View.Entrant.EntrantDetailedCampsite
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EntrantDetailedCampsiteTest {
    @get:Rule
    val composeRule = createComposeRule()

    //Setup Test Data
    val camp = Campsite(
        campId = "1",
        name = "The best campsite",
        location = "rock town",
        price = 30,
        capacity = 8,
        optionalPromoImage = "",
        features = listOf("hot tub", "Waterfall", "Outdoor Sauna"),
        rules = listOf("No littering","Have Fun"),
        cancellationPolicy = "Never",
        tags = listOf("Awesome Time!", "Dog Friendly"),
        ownerId = "",
        longitude = 0.00,
        latitude = 0.00,
    ).apply {
        rating = 0.00
        qrCode = ""
        lotteryId = null

    }

    private val controller = mockk<EntrantDetailedCampsiteController>(relaxed = true)

    @Before
    fun setUp() {
        every { controller.campsite } returns MutableStateFlow(camp)
        every { controller.lotteries } returns MutableStateFlow(emptyList())

        composeRule.setContent {
            EntrantDetailedCampsite(
                navController = rememberNavController(),
                controller = controller
            )
        }
    }

    //Tests to see if everything is initialized to the UI properly
    @Test
    fun checkCampsiteNameShowing() {
        composeRule.onNodeWithTag("campsiteTitle").assertIsDisplayed()
    }
    @Test
    fun checkRatingShowing() {
        composeRule.onNodeWithTag("rating").assertIsDisplayed()
    }
    @Test
    fun checkRatingButtonShowing() {
        composeRule.onNodeWithTag("makeRating").assertIsDisplayed()
    }
    @Test
    fun checkLocationShowing() {
        composeRule.onNodeWithTag("location").assertIsDisplayed()
    }
    @Test
    fun checkPriceShowing() {
        composeRule.onNodeWithTag("pricePerNight").assertIsDisplayed()
    }
    @Test
    fun checkCapacityShowing() {
        composeRule.onNodeWithTag("capacity").assertIsDisplayed()
    }
    @Test
    fun checkCountdownShowing() {
        composeRule.onNodeWithTag("lotteryDaysLeft").assertIsDisplayed()
    }
    @Test
    fun checkImageShowing() {
        composeRule.onNodeWithTag("lotteryDaysLeft").assertIsDisplayed()
    }
    @Test
    fun checkFeaturesShowing() {
        composeRule.onAllNodesWithTag("featureItem")
            .assertCountEquals(camp.features.count { it.isNotBlank() })
    }





}