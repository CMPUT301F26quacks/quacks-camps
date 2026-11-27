package com.example.quackapp

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.example.quackapp.Controller.OwnerCampDetailsController
import com.example.quackapp.Controller.OwnerEditLotteryController
import com.example.quackapp.Controller.OwnerCreateLotteryController
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Controller.RegisterCampsiteController
import com.example.quackapp.Model.CreateLotteryRoute
import com.example.quackapp.View.Owner.OwnerCampDetails
import com.example.quackapp.View.Owner.OwnerEditLottery
import com.example.quackapp.View.Owner.OwnerCreateLottery
import com.example.quackapp.View.Owner.OwnerHome
import com.example.quackapp.View.Owner.OwnerCreateCamp
import org.junit.Rule
import org.junit.Test

class CreateLotteryTests {
    @get:Rule
    val composeRule = createComposeRule()
    private lateinit var navController: TestNavHostController
    private val ownerId = "testOwner"

    private fun waitFor(text: String){
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForTag(tag: String){
        composeRule.waitUntil(10_000) {
            composeRule
                .onAllNodesWithTag(tag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private lateinit var lotteryVM: OwnerCreateLotteryController
    private lateinit var editLotteryVM: OwnerEditLotteryController

    private fun millis(y: Int, m: Int, d: Int) =
        java.time.LocalDate.of(y, m, d)
            .atStartOfDay(java.time.ZoneOffset.UTC)
            .toInstant().toEpochMilli()
    private fun launch() {
        val homeVM = OwnerHomeController(SavedStateHandle(mapOf("ownerId" to ownerId)))
        val registerVM = RegisterCampsiteController(SavedStateHandle(mapOf("ownerId" to ownerId)))
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController, startDestination = "OwnerHome") {
                composable("OwnerHome") { OwnerHome(navController, homeVM) }
                composable("RegisterCampsite/{id}") { OwnerCreateCamp(navController, registerVM) }
                composable("CampsiteDetails/{id}") { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val detailVM = remember(id) {
                        OwnerCampDetailsController(SavedStateHandle(mapOf("campsiteId" to id)))
                    }
                    OwnerCampDetails(navController, detailVM)
                }

                composable<CreateLotteryRoute> { entry ->
                    val vm: OwnerCreateLotteryController = viewModel()
                    lotteryVM = vm
                    OwnerCreateLottery(navController, vm)
                }
                composable("editLottery/{id}") { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val editVM = remember(id) {
                        OwnerEditLotteryController(SavedStateHandle(mapOf("campsiteId" to id)))
                    }
                    editLotteryVM = editVM
                    OwnerEditLottery(navController, editLotteryVM)
                }
            }
        }
    }

    @Test
    fun failToFillRequiredFieldsThenFillThem(){
        launch()

        waitFor("No campsites registered")

        composeRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeRule
            .onNodeWithText("Register Campsite")
            .assertExists()

        composeRule
            .onNodeWithTag("nameField")
            .performTextInput("Test Camp")

        composeRule
            .onNodeWithTag("locationField")
            .performTextInput("Quackada")

        composeRule
            .onNodeWithTag("priceField")
            .performTextInput("25")

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("8")

        composeRule
            .onNodeWithTag("map")
            .performScrollTo()
            .performClick()

        waitForTag("confirmMap")

        composeRule
            .onNodeWithTag("confirmMap")
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithText("Submit")
            .performScrollTo()
            .performClick()

        waitFor("Campsites")
        waitFor("Test Camp")

        composeRule
            .onNodeWithTag("Test Camp row")
            .performTouchInput { longClick(Offset(40f, 40f)) }

        waitForTag("createLotteryButton")
        composeRule
            .onNodeWithTag("selectedCount")
            .assertTextEquals("1 selected")

        composeRule
            .onNodeWithTag("createLotteryButton")
            .performClick()

        waitFor("Test Camp Lottery")
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()


        composeRule.runOnIdle {
            lotteryVM.registrationStart = millis(2026, 12, 10)
            lotteryVM.registrationEnd = millis(2026, 12, 14)
        }

        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.bookingStart = millis(2026, 12, 15)
            lotteryVM.bookingEnd = millis(2026, 12, 16)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.availabilityStart = millis(2026, 12, 20)
            lotteryVM.availabilityEnd = millis(2026, 12, 24)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule
            .onNodeWithTag("numWinnersField")
            .performTextInput("Ten")

        composeRule
            .onNodeWithTag("numWinnersField")
            .assert(
                SemanticsMatcher("EditableText is empty") { node ->
                    node.config.getOrNull(SemanticsProperties.EditableText)?.text.isNullOrEmpty()
                }
            )

        composeRule
            .onNodeWithTag("numWinnersField")
            .performTextInput("5")

        composeRule
            .onNodeWithTag("waitlistCapField")
            .performTextInput("Ten")

        composeRule
            .onNodeWithTag("waitlistCapField")
            .assert(
                SemanticsMatcher("EditableText is empty") { node ->
                    node.config.getOrNull(SemanticsProperties.EditableText)?.text.isNullOrEmpty()
                }
            )

        composeRule
            .onNodeWithTag("waitlistCapField")
            .performTextInput("50")

        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        waitForTag("detailsTest Camp")
        composeRule
            .onNodeWithTag("detailsTest Camp")
            .performClick()

        waitForTag("lotteryInfo")
        composeRule
            .onNodeWithTag("lotteryInfo")
            .performScrollTo()

        waitFor("Registration: Dec 10, 2026 to Dec 14, 2026")

        composeRule
            .onNodeWithText("Registration: Dec 10, 2026 to Dec 14, 2026")
            .performScrollTo()
            .assertExists()

        waitFor("Booking Period: Dec 15, 2026 to Dec 16, 2026")
        composeRule
            .onNodeWithText("Booking Period: Dec 15, 2026 to Dec 16, 2026")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithText("Winners to Draw: 5")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithTag("editLotteryButton")
            .performClick()


        waitFor("Editing Lottery")
        composeRule.runOnIdle {
            editLotteryVM.bookingStart = millis(2026, 12, 16)
            editLotteryVM.bookingEnd = millis(2026, 12, 18)
        }
        waitForTag("saveButton")
        composeRule
            .onNodeWithTag("saveButton")
            .performClick()
        waitFor("Test Camp")
        waitForTag("lotteryInfo")

        composeRule
            .onNodeWithTag("lotteryInfo")
            .performScrollTo()


        waitFor("Registration: Dec 10, 2026 to Dec 14, 2026")

        composeRule
            .onNodeWithText("Registration: Dec 10, 2026 to Dec 14, 2026")
            .performScrollTo()
            .assertExists()

        waitFor("Booking Period: Dec 16, 2026 to Dec 18, 2026")
        composeRule
            .onNodeWithText("Booking Period: Dec 16, 2026 to Dec 18, 2026")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithTag("backButton")
            .performClick()

        composeRule
            .onNodeWithTag("deleteTest Camp")
            .performClick()

        composeRule
            .onNodeWithTag("confirmDelete")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp")
            .assertDoesNotExist()
    }

    @Test
    fun fillOverlappingFieldsOutOfOrder(){
        launch()

        waitFor("No campsites registered")

        composeRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeRule
            .onNodeWithText("Register Campsite")
            .assertExists()

        composeRule
            .onNodeWithTag("nameField")
            .performTextInput("Test Camp")

        composeRule
            .onNodeWithTag("locationField")
            .performTextInput("Quackada")

        composeRule
            .onNodeWithTag("priceField")
            .performTextInput("25")

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("8")

        composeRule
            .onNodeWithTag("map")
            .performScrollTo()
            .performClick()

        waitForTag("confirmMap")

        composeRule
            .onNodeWithTag("confirmMap")
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithText("Submit")
            .performScrollTo()
            .performClick()

        waitFor("Campsites")
        waitFor("Test Camp")
        composeRule
            .onNodeWithTag("Test Camp row")
            .performTouchInput { longClick(Offset(40f, 40f)) }

        waitForTag("createLotteryButton")
        composeRule
            .onNodeWithTag("selectedCount")
            .assertTextEquals("1 selected")

        composeRule
            .onNodeWithTag("createLotteryButton")
            .performClick()
        waitFor("Test Camp Lottery")
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.bookingStart = millis(2026, 12, 10)
            lotteryVM.bookingEnd = millis(2026, 12, 14)
            lotteryVM.registrationStart = millis(2026, 12, 15)
            lotteryVM.registrationEnd = millis(2026, 12, 16)
            lotteryVM.availabilityStart = millis(2026, 12, 20)
            lotteryVM.availabilityEnd = millis(2026, 12, 24)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.availabilityStart = millis(2026, 12, 1)
            lotteryVM.availabilityEnd = millis(2026, 12, 9)
            lotteryVM.bookingStart = millis(2026, 12, 10)
            lotteryVM.bookingEnd = millis(2026, 12, 14)
            lotteryVM.registrationStart = millis(2026, 12, 15)
            lotteryVM.registrationEnd = millis(2026, 12, 16)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.availabilityStart = millis(2026, 12, 1)
            lotteryVM.availabilityEnd = millis(2026, 12, 3)
            lotteryVM.registrationStart = millis(2026, 12, 4)
            lotteryVM.registrationEnd = millis(2026, 12, 6)
            lotteryVM.bookingStart = millis(2026, 12, 14)
            lotteryVM.bookingEnd = millis(2026, 12, 15)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.registrationStart = millis(2026, 12, 4)
            lotteryVM.registrationEnd = millis(2026, 12, 6)
            lotteryVM.availabilityStart = millis(2026, 12, 7)
            lotteryVM.availabilityEnd = millis(2026, 12, 9)
            lotteryVM.bookingStart = millis(2026, 12, 14)
            lotteryVM.bookingEnd = millis(2026, 12, 15)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule.runOnIdle {
            lotteryVM.registrationStart = millis(2026, 12, 4)
            lotteryVM.registrationEnd = millis(2026, 12, 6)
            lotteryVM.bookingStart = millis(2026, 12, 5)
            lotteryVM.bookingEnd = millis(2026, 12, 13)
            lotteryVM.availabilityStart = millis(2026, 12, 15)
            lotteryVM.availabilityEnd = millis(2026, 12, 30)
        }
        composeRule
            .onNodeWithTag("submitButton")
            .performClick()
        composeRule
            .onNodeWithText("Test Camp Lottery")
            .assertExists()

        composeRule
            .onNodeWithTag("backButton")
            .performClick()


        waitForTag("deleteTest Camp")
        composeRule
            .onNodeWithTag("deleteTest Camp")
            .performClick()

        composeRule
            .onNodeWithTag("confirmDelete")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp")
            .assertDoesNotExist()
    }

}