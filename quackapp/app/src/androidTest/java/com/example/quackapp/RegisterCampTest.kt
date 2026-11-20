package com.example.quackapp

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Controller.RegisterCampsiteController
import com.example.quackapp.Model.Campsite
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
            }
        }
    }

    @Test
    fun submitEmptyCamp(){
        launch()
        composeRule
            .onNodeWithText("No campsites registered")
            .assertExists()

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
        composeRule
            .onNodeWithText("No campsites registered")
            .assertExists()

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

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithText("Campsites")
                .fetchSemanticsNodes()
                .isNotEmpty()

        }
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
}