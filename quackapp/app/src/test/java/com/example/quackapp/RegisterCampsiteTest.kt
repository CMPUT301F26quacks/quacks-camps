package com.example.quackapp

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.quackapp.Controller.DeviceIdGetter
import com.example.quackapp.Controller.UserRepository
import org.junit.Assume
import org.junit.Before
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RegisterCampsiteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity
    private var isOwner = false
    @Before
    fun checkIfUserIsOwner() {
        val latch = CountDownLatch(1)

        DeviceIdGetter.getDeviceId(
            onSuccess = { id ->
                UserRepository.getUser(
                    deviceId = id,
                    onResult = { user ->
                        //checks if the user is an entrant
                        if (user != null && user.roles.contains("OWNER")) {
                            isOwner = true
                        }
                        latch.countDown()
                    },
                    onFail = { latch.countDown() }
                )
            },
            onFail = { latch.countDown() }
        )

        latch.await(10, TimeUnit.SECONDS)

        //Checks if user is an entrant
        Assume.assumeTrue("User is not registered as an ENTRANT", isEntrant)

        //Give android studio time to load the user in
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onNodeWithText("Campsites")
                .fetchSemanticsNodes()
                .exists()
        }
    }
    @Test
    fun registerCampsiteReturnHome(){
        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()
        composeTestRule
            .onNodeWithText("Register Campsite")
            .assertExists()
        composeTestRule
            .onNodeWithTag("nameField")
            .performTextInput("AutoTest")
        composeTestRule
            .onNodeWithTag("locationField")
            .performTextInput("Alberta")
        composeTestRule
            .onNodeWithTag("priceField")
            .performTextInput("20")
        composeTestRule
            .onNodeWithTag("capacityField")
            .performTextInput("10")
        composeTestRule
            .onNodeWithTag("cancelPolicyField")
            .performTextInput("Don't")
        composeTestRule
            .onNodeWithTag("amenity1")
            .performTextInput("Free Water")
        composeTestRule
            .onNodeWithTag("addAmenity")
            .performClick()
        composeTestRule
            .onNodeWithTag("amenity2")
            .assertExists()
        composeTestRule
            .onNodeWithTag("amenity2")
            .performTextInput("Outlets")
        composeTestRule
            .onNodeWithTag("deleteAmenity1")
            .performClick()
        composeTestRule
            .onNodeWithTag("amenity2")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("amenity1")
            .assertTextEquals("Outlets")
        composeTestRule
            .onNodeWithTag("rule1")
            .performTextInput("delete me")
        composeTestRule
            .onNodeWithTag("addRule")
            .performClick()
        composeTestRule
            .onNodeWithTag("rule2")
            .assertExists()
        composeTestRule
            .onNodeWithTag("rule2")
            .performTextInput("Be cool")
        composeTestRule
            .onNodeWithTag("removeRule1")
            .performClick()
        composeTestRule
            .onNodeWithTag("rule2")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag("rule1")
            .assertTextEquals("Be cool")
        composeTestRule
            .onNodeWithTag("submitButton")
            .performClick()
        composeTestRule
            .onNodeWithTag("backButton")
            .performClick()
        composeTestRule
            .onNodeWithText("Campsites")
            .assertExists()
        composeTestRule
            .onNodeWithText("AutoTest")
            .assertExists()
    }

}