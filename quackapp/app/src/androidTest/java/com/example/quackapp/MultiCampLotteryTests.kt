package com.example.quackapp

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.SavedStateHandle
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
import androidx.compose.ui.test.longClick
import androidx.lifecycle.viewmodel.compose.viewModel
import org.junit.Rule
import org.junit.Test

class MultiCampLotteryTests {
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

    private lateinit var lotteryVM: OwnerCreateLotteryController
    private lateinit var editLotteryVM: OwnerEditLotteryController
    @Test
    fun createAndEditMultiCampLottery(){
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
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeRule
            .onNodeWithText("Register Campsite")
            .assertExists()

        composeRule
            .onNodeWithTag("nameField")
            .performTextInput("Test Camp 2")

        composeRule
            .onNodeWithTag("locationField")
            .performTextInput("Quackada")

        composeRule
            .onNodeWithTag("priceField")
            .performTextInput("50")

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("2")

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
        waitFor("Test Camp 2")

        composeRule
            .onNodeWithTag("Test Camp row")
            .performTouchInput { longClick(Offset(40f, 40f)) }


        waitForTag("createLotteryButton")
        composeRule
            .onNodeWithTag("selectedCount")
            .assertTextEquals("1 selected")

        composeRule
            .onNodeWithTag("Test Camp 2 row")
            .performTouchInput { longClick(Offset(40f, 40f)) }

        waitForTag("selectedCount")
        composeRule
            .onNodeWithTag("selectedCount")
            .assertTextEquals("2 selected")

        composeRule
            .onNodeWithTag("createLotteryButton")
            .performClick()

        waitFor("Multi-Camp Lottery")

        composeRule.runOnIdle {
            lotteryVM.registrationStart = millis(2026, 12, 4)
            lotteryVM.registrationEnd = millis(2026, 12, 6)
            lotteryVM.bookingStart = millis(2026, 12, 7)
            lotteryVM.bookingEnd = millis(2026, 12, 13)
            lotteryVM.availabilityStart = millis(2026, 12, 15)
            lotteryVM.availabilityEnd = millis(2026, 12, 30)
        }
        composeRule
            .onNodeWithTag("numWinnersField")
            .performTextInput("2")

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

        waitFor("Registration: Dec 4, 2026 to Dec 6, 2026")

        composeRule
            .onNodeWithText("Registration: Dec 4, 2026 to Dec 6, 2026")
            .performScrollTo()
            .assertExists()

        waitFor("Booking Period: Dec 7, 2026 to Dec 13, 2026")
        composeRule
            .onNodeWithText("Booking Period: Dec 7, 2026 to Dec 13, 2026")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithText("Winners to Draw: 2")
            .performScrollTo()
            .assertExists()
        composeRule
            .onNodeWithTag("backButton")
            .performClick()

        waitForTag("detailsTest Camp 2")

        composeRule
            .onNodeWithTag("detailsTest Camp 2")
            .performClick()

        waitForTag("lotteryInfo")

        composeRule
            .onNodeWithTag("lotteryInfo")
            .performScrollTo()

        waitFor("Registration: Dec 4, 2026 to Dec 6, 2026")

        composeRule
            .onNodeWithText("Registration: Dec 4, 2026 to Dec 6, 2026")
            .performScrollTo()
            .assertExists()

        waitFor("Booking Period: Dec 7, 2026 to Dec 13, 2026")
        composeRule
            .onNodeWithText("Booking Period: Dec 7, 2026 to Dec 13, 2026")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithText("Winners to Draw: 2")
            .performScrollTo()
            .assertExists()

        waitForTag("editLotteryButton")
        composeRule
            .onNodeWithTag("editLotteryButton")
            .performClick()

        waitFor("Editing Lottery")

        composeRule.runOnIdle {
            editLotteryVM.bookingStart = millis(2026, 12, 7)
            editLotteryVM.bookingEnd = millis(2026, 12, 14)
        }
        waitForTag("saveButton")
        composeRule
            .onNodeWithTag("saveButton")
            .performClick()

        waitForTag("lotteryInfo")
        composeRule
            .onNodeWithTag("lotteryInfo")
            .performScrollTo()

        waitFor("Registration: Dec 4, 2026 to Dec 6, 2026")

        composeRule
            .onNodeWithText("Registration: Dec 4, 2026 to Dec 6, 2026")
            .performScrollTo()
            .assertExists()

        waitFor("Booking Period: Dec 7, 2026 to Dec 14, 2026")
        composeRule
            .onNodeWithText("Booking Period: Dec 7, 2026 to Dec 14, 2026")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithText("Winners to Draw: 2")
            .performScrollTo()
            .assertExists()


        waitForTag("backButton")
        composeRule
            .onNodeWithTag("backButton")
            .performClick()

        waitForTag("detailsTest Camp")
        composeRule
            .onNodeWithTag("detailsTest Camp")
            .performClick()

        waitForTag("lotteryInfo")
        composeRule
            .onNodeWithTag("lotteryInfo")
            .performScrollTo()

        waitFor("Registration: Dec 4, 2026 to Dec 6, 2026")

        composeRule
            .onNodeWithText("Registration: Dec 4, 2026 to Dec 6, 2026")
            .performScrollTo()
            .assertExists()

        waitFor("Booking Period: Dec 7, 2026 to Dec 14, 2026")
        composeRule
            .onNodeWithText("Booking Period: Dec 7, 2026 to Dec 14, 2026")
            .performScrollTo()
            .assertExists()

        composeRule
            .onNodeWithText("Winners to Draw: 2")
            .performScrollTo()
            .assertExists()

        waitForTag("backButton")
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
            .onNodeWithTag("Test Camp row")
            .assertDoesNotExist()

        waitForTag("deleteTest Camp 2")
        composeRule
            .onNodeWithTag("deleteTest Camp 2")
            .performClick()

        composeRule
            .onNodeWithTag("confirmDelete")
            .performClick()

        composeRule
            .onNodeWithText("Test Camp 2")
            .assertDoesNotExist()
    }
}