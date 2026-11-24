package com.example.quackapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.quackapp.Model.AppNotification
import com.example.quackapp.Model.CampsiteSummary
import com.example.quackapp.Model.LotteryOffer
import com.example.quackapp.Model.NotificationType
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationStatus
import com.example.quackapp.View.Entrant.ActiveReservationsList
import com.example.quackapp.View.Entrant.LotteryOfferCard
import com.example.quackapp.View.Entrant.NotificationsDialog
import com.example.quackapp.View.Entrant.PastReservationsList
import com.example.quackapp.View.Entrant.ReservationCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Intent (UI) tests for the entrant Active Campsites screen components.
 *
 * Uses realistic fake reservations and lottery offers instead of Firestore.
 * Covers US 01.03.01, 01.03.02, 01.04.01, 01.04.02, 01.04.03, 01.05.02, 01.06.01, 01.06.02.
 */
class EntrantReservationsIntentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val jasper = CampsiteSummary("camp-jasper-1", "Whistlers Site 12", "Jasper, AB", 48.0, 6, "Near the tramway")
    private val banff = CampsiteSummary("camp-banff-1", "Tunnel Mountain Site 4", "Banff, AB", 65.0, 8, "Outdoor cookhouse")

    private val singleOffer = LotteryOffer(
        id = "offer-1",
        lotteryTitle = "Jasper May Weekends",
        entrantDeviceId = "entrant-1",
        campsites = listOf(jasper),
        availableDateRanges = listOf("May 7 – May 9, 2027", "May 14 – May 16, 2027")
    )
    private val multiOffer = singleOffer.copy(id = "offer-2", lotteryTitle = "Rockies Multi-Site", campsites = listOf(jasper, banff))

    private val activeReservation = Reservation(
        id = "res-12345678", campsiteId = "camp-banff-1", campsiteName = "Tunnel Mountain Site 4",
        campsiteLocation = "Banff, AB", entrantDeviceId = "entrant-1", entrantName = "Sam Climber",
        dateRange = "May 21 – May 23, 2027", numGuests = 4, totalPrice = 130.0,
        status = ReservationStatus.CONFIRMED
    )

    /** US 01.04.01: a lottery winner sees the dates they can reserve. */
    @Test
    fun winnerSeesAvailableDates() {
        composeRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LotteryOfferCard(offer = singleOffer, onDecline = {}, onConfirm = { _, _, _ -> })
            }
        }
        composeRule.onNodeWithText("LOTTERY WINNER").assertIsDisplayed()
        composeRule.onNodeWithText("May 7 – May 9, 2027").assertIsDisplayed()
        composeRule.onNodeWithText("May 14 – May 16, 2027").assertIsDisplayed()
    }

    /** US 01.04.02: winner of a multi-campsite lottery picks the campsite and dates. */
    @Test
    fun winnerChoosesCampsiteAndDates() {
        var chosenCamp: CampsiteSummary? = null
        var chosenDates = ""
        composeRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LotteryOfferCard(
                    offer = multiOffer,
                    onDecline = {},
                    onConfirm = { camp, dates, _ -> chosenCamp = camp; chosenDates = dates }
                )
            }
        }
        composeRule.onNodeWithText("Select Campsite Option:").assertIsDisplayed()
        composeRule.onNodeWithText("Tunnel Mountain Site 4").performClick()
        composeRule.onNodeWithText("May 14 – May 16, 2027").performScrollTo().performClick()
        composeRule.onNodeWithText("Accept & Reserve").performScrollTo().performClick()

        assertEquals("camp-banff-1", chosenCamp?.campsiteId)
        assertEquals("May 14 – May 16, 2027", chosenDates)
    }

    /** US 01.04.03: winner declines the reservation. */
    @Test
    fun winnerDeclinesOffer() {
        var declined = false
        composeRule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LotteryOfferCard(offer = singleOffer, onDecline = { declined = true }, onConfirm = { _, _, _ -> })
            }
        }
        composeRule.onNodeWithText("Decline").performScrollTo().performClick()
        assertTrue(declined)
    }

    /** US 01.06.01 + US 01.05.02: active reservation shows details and a CSV receipt button. */
    @Test
    fun activeReservationShowsDetailsAndCsvReceipt() {
        var csvFor: Reservation? = null
        composeRule.setContent {
            ActiveReservationsList(
                reservations = listOf(activeReservation),
                onCancelClick = {},
                onDownloadCsv = { csvFor = it },
                onExportAllCsv = {},
                onGoToLotteryWins = {}
            )
        }
        composeRule.onNodeWithText("Confirmed Bookings (1)").assertIsDisplayed()
        composeRule.onNodeWithText("Tunnel Mountain Site 4").assertIsDisplayed()
        composeRule.onNodeWithText("Dates: May 21 – May 23, 2027").assertIsDisplayed()
        composeRule.onNodeWithText("Total: $130.00 CAD").assertIsDisplayed()

        composeRule.onNodeWithText("CSV Receipt").performClick()
        assertEquals("res-12345678", csvFor?.id)
    }

    @Test
    fun reservationCardCancelButton() {
        var cancelled = false
        composeRule.setContent {
            ReservationCard(reservation = activeReservation, onCancel = { cancelled = true }, onDownloadCsv = {})
        }
        composeRule.onNodeWithText("Cancel").performClick()
        assertTrue(cancelled)
    }

    @Test
    fun noActiveReservationsShowsEmptyStateAndLotteryLink() {
        var wentToWins = false
        composeRule.setContent {
            ActiveReservationsList(
                reservations = emptyList(),
                onCancelClick = {},
                onDownloadCsv = {},
                onExportAllCsv = {},
                onGoToLotteryWins = { wentToWins = true }
            )
        }
        composeRule.onNodeWithText("No Active Reservations").assertIsDisplayed()
        composeRule.onNodeWithText("Check Lottery Wins").performClick()
        assertTrue(wentToWins)
    }

    /** US 01.06.02: past reservations are listed. */
    @Test
    fun pastReservationsListed() {
        composeRule.setContent {
            PastReservationsList(
                reservations = listOf(activeReservation.copy(id = "res-old-1", status = ReservationStatus.COMPLETED))
            )
        }
        composeRule.onNodeWithText("Tunnel Mountain Site 4").assertIsDisplayed()
    }

    /** US 01.03.01 / US 01.03.02: win and loss notifications are shown. */
    @Test
    fun winAndLossNotificationsShown() {
        composeRule.setContent {
            NotificationsDialog(
                notifications = listOf(
                    AppNotification(id = "n1", recipientDeviceId = "entrant-1", title = "You won the lottery!",
                        message = "You were selected for Jasper May Weekends.", type = NotificationType.LOTTERY_WIN),
                    AppNotification(id = "n2", recipientDeviceId = "entrant-1", title = "Lottery results",
                        message = "You were not selected for Banff Long Weekend.", type = NotificationType.LOTTERY_LOSS)
                ),
                onDismiss = {},
                onMarkAllAsRead = {},
                onClearAll = {},
                onNotificationClick = {},
                onExportNotifCsv = {}
            )
        }
        composeRule.onNodeWithText("You won the lottery!").assertIsDisplayed()
        composeRule.onNodeWithText("Lottery results").assertIsDisplayed()
    }
}
