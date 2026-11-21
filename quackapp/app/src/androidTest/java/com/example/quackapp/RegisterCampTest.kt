package com.example.quackapp

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.example.quackapp.Controller.DetailedCampsiteController
import com.example.quackapp.Controller.EditCampsiteController
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Controller.RegisterCampsiteController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.View.DetailedCampsiteScreen
import com.example.quackapp.View.EditCampsiteScreen
import com.example.quackapp.View.OwnerHome
import com.example.quackapp.View.RegisterCampsiteScreen
import okhttp3.internal.wait
import org.junit.Rule
import org.junit.Test

class RegisterCampTest {
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

    private fun launch() {
        val homeVM = OwnerHomeController(SavedStateHandle(mapOf("ownerId" to ownerId)))
        val registerVM = RegisterCampsiteController(SavedStateHandle(mapOf("ownerId" to ownerId)))
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController, startDestination = "OwnerHome") {
                composable("OwnerHome") { OwnerHome(navController, homeVM) }
                composable("RegisterCampsite/{id}") { RegisterCampsiteScreen(navController, registerVM) }
                composable("CampsiteDetails/{id}") { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val detailVM = remember(id) {
                        DetailedCampsiteController(SavedStateHandle(mapOf("campsiteId" to id)))
                    }
                    DetailedCampsiteScreen(navController, detailVM)
                }
                composable("EditCampsite/{id}") { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val editVM = remember(id) {
                        EditCampsiteController(SavedStateHandle(mapOf("campsiteId" to id)))
                    }
                    EditCampsiteScreen(navController, editVM)
                }
            }
        }
    }

    @Test
    fun submitEmptyCamp(){
        launch()
        waitFor("No campsites registered")

        composeRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeRule
            .onNodeWithText("Register Campsite")
            .assertExists()

        composeRule
            .onNodeWithTag("submitButton")
            .performClick()

        composeRule
            .onNodeWithTag("backButton")
            .performClick()

        composeRule
            .onNodeWithText("No campsites registered")
            .assertExists()
    }

    @Test
    fun tryNonNumericPriceAndCapacityFieldsAndDelete(){
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
            .performTextInput("Kool Kids Camp")

        composeRule
            .onNodeWithTag("locationField")
            .performTextInput("Quackada")

        composeRule
            .onNodeWithTag("priceField")
            .performTextInput("Ten")

        composeRule
            .onNodeWithTag("priceField")
            .assert(
                SemanticsMatcher("EditableText is empty") { node ->
                    node.config.getOrNull(SemanticsProperties.EditableText)?.text.isNullOrEmpty()
                }
            )

        composeRule
            .onNodeWithTag("priceField")
            .performTextInput("10")

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("Ten")

        composeRule
            .onNodeWithTag("capacityField")
            .assert(
                SemanticsMatcher("EditableText is empty") { node ->
                    node.config.getOrNull(SemanticsProperties.EditableText)?.text.isNullOrEmpty()
                }
            )

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("10")


        composeRule
            .onNodeWithText("Submit")
            .performScrollTo()
            .performClick()

        waitFor("Campsites")

        composeRule
            .onNodeWithText("Kool Kids Camp")
            .assertExists()

        composeRule
            .onNodeWithTag("deleteKool Kids Camp")
            .performClick()

        composeRule
            .onNodeWithTag("confirmDelete")
            .performClick()

        composeRule
            .onNodeWithText("Kool Kids Camp")
            .assertDoesNotExist()
    }

    @Test
    fun addFullCampAndViewDetails(){
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
            .performTextInput("50")

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("10")

        composeRule
            .onNodeWithTag("cancelPolicyField")
            .performTextInput("24 hour notice")

        composeRule
            .onNodeWithTag("amenity1")
            .performTextInput("Water")

        composeRule
            .onNodeWithTag("addAmenity")
            .performClick()

        composeRule
            .onNodeWithTag("amenity2")
            .performTextInput("Food")

        composeRule
            .onNodeWithTag("deleteAmenity1")
            .performClick()

        composeRule
            .onNodeWithTag("amenity2")
            .assertDoesNotExist()

        composeRule
            .onNodeWithTag("addAmenity")
            .performClick()

        composeRule
            .onNodeWithTag("amenity2")
            .performTextInput("Water")

        composeRule
            .onNodeWithTag("rule1")
            .performTextInput("No running")

        composeRule
            .onNodeWithTag("addRule")
            .performClick()

        composeRule
            .onNodeWithTag("rule2")
            .performTextInput("No dancing")

        composeRule
            .onNodeWithTag("deleteRule1")
            .performClick()

        composeRule
            .onNodeWithTag("rule2")
            .assertDoesNotExist()

        composeRule
            .onNodeWithTag("addRule")
            .performClick()

        composeRule
            .onNodeWithTag("rule2")
            .performTextInput("Have fun")

        composeRule
            .onNodeWithText("Smoke Free")
            .performScrollTo()
            .performClick()

        composeRule
            .onNodeWithTag("newTagField")
            .performTextInput("Cool")

        composeRule
            .onNodeWithTag("addTagButton")
            .performScrollTo()
            .performClick()

        composeRule
            .onNodeWithText("Submit")
            .performScrollTo()
            .performClick()

        waitFor("Campsites")

        composeRule
            .onNodeWithText("Test Camp")
            .assertExists()

        composeRule
            .onNodeWithTag("detailsTest Camp")
            .performClick()

        waitFor("Test Camp")

        composeRule
            .onNodeWithText("Quackada")
            .assertExists()

        composeRule
            .onNodeWithText("$50 per night")
            .assertExists()

        composeRule
            .onNodeWithText("10 people")
            .assertExists()

        composeRule
            .onNodeWithText("24 hour notice")
            .assertExists()

        composeRule
            .onNodeWithTag("amenity2")
            .assertExists()

        composeRule
            .onNodeWithTag("rule2")
            .assertExists()

        composeRule
            .onNodeWithText("Smoke Free")
            .assertExists()

        composeRule
            .onNodeWithText("Cool")
            .assertExists()

        composeRule
            .onNodeWithTag("backButton")
            .performClick()

        waitFor("Campsites")

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
    fun editName(){
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
            .performTextInput("10")

        composeRule
            .onNodeWithTag("capacityField")
            .performTextInput("5")

        composeRule
            .onNodeWithTag("submitButton")
            .performScrollTo()
            .performClick()

        waitFor("Test Camp")

        composeRule.waitUntil(10_000) {
            composeRule
                .onAllNodesWithTag("editTest Camp")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule
            .onNodeWithTag("editTest Camp")
            .performClick()

        waitFor("Test Camp")

        composeRule
            .onNodeWithTag("nameField")
            .performTextClearance()

        composeRule
            .onNodeWithTag("nameField")
            .performTextInput("Test Again")

        composeRule
            .onNodeWithTag("saveButton")
            .performScrollTo()
            .performClick()

        waitFor("Test Again")
        composeRule.waitUntil(10_000) {
            composeRule
                .onAllNodesWithTag("deleteTest Again")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule
            .onNodeWithTag("deleteTest Again")
            .performClick()

        composeRule
            .onNodeWithTag("confirmDelete")
            .performClick()

    }
}