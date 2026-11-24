package com.example.quackapp

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.Lottery
import com.example.quackapp.View.Entrant.EntrantHomeList
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EntrantHomeListTest {
    @get:Rule
    val composeRule = createComposeRule()

    //Setup Test Data
    private val dec25 = 1798156800000L


    private fun camp(id: String, name: String, location: String, capacity: Int) =
        Campsite(campId = id, name = name, location = location, capacity = capacity)
            .apply { availability = listOf(dec25) }

    private val camps = listOf(
        camp("1", "Cliff Ridge", "Edmonton", 3),
        camp("2", "Lake Ridge", "Jasper", 5),
        camp("3", "Ridge Cliff", "Canmore", 7)
    )

    @Before
    fun setUp() {
        composeRule
            .setContent {
            EntrantHomeList(
                campsites = camps,
                lotteries = listOf(Lottery(lotteryID = "1")),
                campsiteCardClicked = {}
            )
        }
    }

    //UI component tester functions used by tests
    private fun openFilters() {
        composeRule
            .onNodeWithContentDescription("Filter")
            .performClick()
    }

    private fun scrollTo(text: String) {
        if (composeRule
            .onAllNodes(hasText(text))
            .fetchSemanticsNodes().isEmpty())
        {
            composeRule
                .onNode(hasScrollAction())
                .performScrollToNode(hasText(text))
        }
    }

    private fun assertShowing(count: Int) {
        composeRule
            .onNodeWithText("Showing $count of 3 campsites.")
            .assertIsDisplayed()
    }

    private fun typeInSearch(text: String) {
        composeRule
            .onNode(hasSetTextAction())
            .performTextInput(text)
    }


    //Search Bar Tests
    @Test
    fun checkIfItShowsAllCampsites() {
        assertShowing(3)
    }

    @Test
    fun checkIfSearchByNameWorks() {
        typeInSearch("lake")
        assertShowing(1)

        composeRule
            .onNodeWithText("Lake Ridge")
            .assertIsDisplayed()
    }

    @Test
    fun checkIfSearchWithNoResultsWorks() {
        typeInSearch("xxxxxxxx")
        assertShowing(0)
    }


    //Filter Pop-Up Tests
    @Test
    fun checkIfFilterTextShows() {
        openFilters()

        composeRule
            .onNodeWithText("Search By Location")
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Available Dates")
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Search campsites...")
            .assertIsDisplayed()

        scrollTo("Capacity: 3 - 7 people")

        composeRule
            .onNodeWithText("Capacity: 3 - 7 people")
            .assertIsDisplayed()
    }

    @Test
    fun checkIfFilterToggleWorks() {
        openFilters()

        composeRule
            .onNode(isToggleable())
            .performClick()
        composeRule
            .onNodeWithText("Search locations...")
            .assertIsDisplayed()

        typeInSearch("jasper")
        assertShowing(1)

        composeRule
            .onNodeWithText("Lake Ridge")
            .assertIsDisplayed()
    }


    //Test Campsite Capacity Slider
    @Test
    fun checkIfCapacitySliderFiltersOutSmallCampsites() {
        openFilters()
        scrollTo("Capacity: 3 - 7 people")
        val thumbs = composeRule
            .onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)
        )
        thumbs[0].performSemanticsAction(SemanticsActions.SetProgress) {
            it(7f)
        }
        assertShowing(1)
    }

    @Test
    fun checkIfCapacitySliderFiltersOutLargeCampsites() {
        openFilters()
        scrollTo("Capacity: 3 - 7 people")
        val thumbs = composeRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)
        )
        thumbs[1].performSemanticsAction(SemanticsActions.SetProgress) {
            it(3f)
        }

        assertShowing(1)
    }

    @Test
    fun checkIfCapacitySliderFiltersOutExtremesCampsites() {
        openFilters()
        scrollTo("Capacity: 3 - 7 people")

        val thumbs = composeRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)
        )
        thumbs[0].performSemanticsAction(SemanticsActions.SetProgress) {
            it(4f)
        }
        thumbs[1].performSemanticsAction(SemanticsActions.SetProgress) {
            it(6f)
        }
        assertShowing(1)
    }

    //Test Reset Button
    @Test
    fun checkIfResetButtonResetsSearchAndCheckbox() {
        openFilters()

        composeRule
            .onNode(isToggleable())
            .performClick()

        typeInSearch("Edmonton")
        assertShowing(1)

        scrollTo("Reset")

        composeRule
            .onNodeWithText("Reset")
            .performClick()

        assertShowing(3)

        composeRule
            .onNodeWithText("Search campsites...")
            .assertIsDisplayed()
    }

    @Test
    fun checkIfResetButtonResetsCapacityRange() {
        openFilters()
        scrollTo("Capacity: 3 - 7 people")
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))[0]
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        assertShowing(2)

        scrollTo("Reset")
        composeRule
            .onNodeWithText("Reset")
            .performClick()
        assertShowing(3)
    }
}