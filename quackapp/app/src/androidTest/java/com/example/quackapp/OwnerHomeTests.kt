package com.example.quackapp

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.example.quackapp.Controller.OwnerHomeController
import com.example.quackapp.Model.Campsite
import com.example.quackapp.View.OwnerHome
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerHomeTest {
    @get:Rule
    val composeRule = createComposeRule()

    // Create a mock navController
    private lateinit var navController: TestNavHostController
    // Create a mock controller and campsite database without firestore
    private val campsFlow = MutableStateFlow<List<Campsite>>(emptyList())
    private val viewModel = mockk<OwnerHomeController>(relaxed = true) {
        every { camps } returns campsFlow
    }

    private fun launch(camps: List<Campsite>) {
        campsFlow.value = camps
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController, startDestination = "OwnerHome") {
                composable("OwnerHome") { OwnerHome(navController, viewModel) }
                // Stub destinations: we only care that navigation happened
                composable("RegisterCampsite") {}
                composable("EditCampsite/{id}") {}
                composable("CampsiteDetails/{id}") {}
            }
        }
    }

    private fun currentRoute() = navController.currentBackStackEntry?.destination?.route

    @After
    fun tearDown() = io.mockk.unmockkAll()

    @Test
    fun noCampsShowsEmptyMessage() {
        launch(emptyList())
        composeRule.onNodeWithText("No campsites registered").assertIsDisplayed()
    }

    @Test
    fun campsDisplayedAndHidesEmptyMessage() {
        launch(listOf(Campsite("1", "Lakeview"), Campsite("2", "Pine Ridge")))

        composeRule.onNodeWithText("Lakeview").assertIsDisplayed()
        composeRule.onNodeWithText("Pine Ridge").assertIsDisplayed()
        composeRule.onNodeWithText("No campsites registered").assertDoesNotExist()
    }

    @Test
    fun campWithoutImageHaveNoImagePlaceholder() {
        launch(listOf(Campsite("1", "Lakeview")))
        composeRule.onNodeWithText("No Image").assertIsDisplayed()
    }

    @Test
    fun listUpdatesWithNewData() {
        launch(emptyList())
        composeRule.onNodeWithText("No campsites registered").assertIsDisplayed()

        campsFlow.value = listOf(Campsite("1", "Lakeview"))

        composeRule.onNodeWithText("Lakeview").assertIsDisplayed()
    }

    @Test
    fun addButtonNavigatesToRegisterCampsite() {
        launch(emptyList())
        composeRule.onNodeWithTag("addCampsiteButton").performClick()
        assertEquals("RegisterCampsite", currentRoute())
    }

    @Test
    fun editButtonNavigatesToEditCampsite() {
        launch(listOf(Campsite("abc", "Lakeview")))
        composeRule.onNodeWithText("Edit").performClick()

        assertEquals("EditCampsite/{id}", currentRoute())
        assertEquals("abc", navController.currentBackStackEntry?.arguments?.getString("id"))
    }

    @Test
    fun detailsButtonNavigatesToCampsiteDetails() {
        launch(listOf(Campsite("abc", "Lakeview")))
        composeRule.onNodeWithText("Details").performClick()

        assertEquals("CampsiteDetails/{id}", currentRoute())
        assertEquals("abc", navController.currentBackStackEntry?.arguments?.getString("id"))
    }

    @Test
    fun deleteButtonShowsConfirmationPopup() {
        launch(listOf(Campsite("1", "Lakeview")))
        composeRule.onNodeWithText("Delete").performClick()

        composeRule.onNodeWithText("Delete campsite?").assertIsDisplayed()
    }

    @Test
    fun confirmDeleteCallsControllerAndClosesPopup() {
        val target = Campsite("1", "Lakeview")
        launch(listOf(target))

        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithTag("confirmDelete").performClick()

        verify(exactly = 1) { viewModel.deleteCampsite(target) }
        composeRule.onNodeWithText("Delete campsite?").assertDoesNotExist()
    }

    @Test
    fun cancelDeleteDoesNotCallController() {
        launch(listOf(Campsite("1", "Lakeview")))

        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        verify(exactly = 0) { viewModel.deleteCampsite(any()) }
        composeRule.onNodeWithText("Delete campsite?").assertDoesNotExist()
    }

    @Test
    fun qrClick_showsQrDialogAndCloseDismissesIt() {
        launch(listOf(Campsite("1", "Lakeview")))

        composeRule.onNodeWithText("QR Code").performClick()
        composeRule.onNodeWithText("QR Code for Lakeview").assertIsDisplayed()

        composeRule.onNodeWithText("Close").performClick()
        composeRule.onNodeWithText("QR Code for Lakeview").assertDoesNotExist()
    }
}