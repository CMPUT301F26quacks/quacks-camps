package com.example.quackapp

import com.example.quackapp.Model.Entrant
import com.example.quackapp.Model.NotificationRepository
import com.example.quackapp.Model.NotificationType
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests validating notification management and entrant alert workflows:
 * - US 01.03.01: Receive notification when winning campsite lottery.
 * - US 01.03.02: Receive notification when losing campsite lottery.
 * - US 01.05.02: Receive reservation confirmation notification.
 * - US 01.10.01: Respect user notification opt-out preferences.
 */
class NotificationRepositoryTest {

    private lateinit var notificationRepo: NotificationRepository

    @Before
    fun setUp() {
        notificationRepo = NotificationRepository.getInstance()
        notificationRepo.resetToMockData()
    }

    @Test
    fun testInitialMockNotificationsLoaded() {
        val notifs = notificationRepo.getNotificationsForUser("default_device")
        assertTrue("Should have initial mock notifications", notifs.isNotEmpty())
        assertTrue("Should have an initial lottery win notification", notifs.any { it.type == NotificationType.LOTTERY_WIN })
        assertTrue("Should have an initial lottery loss notification", notifs.any { it.type == NotificationType.LOTTERY_LOSS })
        assertTrue("Should have an initial reservation confirmation", notifs.any { it.type == NotificationType.RESERVATION_CONFIRMATION })
    }

    /**
     * US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
     */
    @Test
    fun testNotifyLotteryWinDispatchesAlert() {
        val notif = notificationRepo.notifyLotteryWin(
            recipientDeviceId = "entrant_device_77",
            lotteryTitle = "Jasper Alpine Meadow Draw",
            offerId = "offer-jasper-77",
            userPreferenceEnabled = true
        )

        assertNotNull("Notification should be returned upon successful dispatch", notif)
        assertEquals(NotificationType.LOTTERY_WIN, notif!!.type)
        assertEquals("offer-jasper-77", notif.relatedOfferId)
        assertTrue(notif.title.contains("Jasper Alpine Meadow Draw"))
        assertFalse("New notification should be unread", notif.isRead)

        val userNotifs = notificationRepo.getNotificationsForUser("entrant_device_77")
        assertTrue("User should find the dispatched win notification", userNotifs.any { it.id == notif.id })
    }

    /**
     * US 01.03.01 / US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
     */
    @Test
    fun testNotifyLotteryLossDispatchesAlert() {
        val notif = notificationRepo.notifyLotteryLoss(
            recipientDeviceId = "entrant_device_77",
            lotteryTitle = "Banff Sunshine Draw",
            userPreferenceEnabled = true
        )

        assertNotNull("Loss notification should be returned", notif)
        assertEquals(NotificationType.LOTTERY_LOSS, notif!!.type)
        assertTrue(notif.title.contains("Banff Sunshine Draw"))
        assertTrue(notif.message.contains("not selected"))

        val userNotifs = notificationRepo.getNotificationsForUser("entrant_device_77")
        assertTrue(userNotifs.any { it.id == notif.id })
    }

    /**
     * US 01.05.02: As an entrant, I want to receive reservation by notification.
     */
    @Test
    fun testNotifyReservationConfirmedDispatchesAlert() {
        val reservation = Reservation(
            id = "res-test-999",
            campsiteId = "site-test-1",
            campsiteName = "Tunnel Mountain Village",
            campsiteLocation = "Banff, AB",
            entrantDeviceId = "entrant_device_77",
            dateRange = "July 1 - July 4, 2027",
            numGuests = 2,
            totalPrice = 180.0,
            status = ReservationStatus.CONFIRMED
        )

        val notif = notificationRepo.notifyReservationConfirmed(
            recipientDeviceId = "entrant_device_77",
            reservation = reservation,
            userPreferenceEnabled = true
        )

        assertNotNull("Confirmation notification should be created", notif)
        assertEquals(NotificationType.RESERVATION_CONFIRMATION, notif!!.type)
        assertEquals("res-test-999", notif.relatedReservationId)
        assertTrue(notif.message.contains("Tunnel Mountain Village"))
    }

    /**
     * US 01.10.01 / CRC card requirement: Respect user notification opt-out preferences.
     */
    @Test
    fun testNotificationSuppressedWhenUserOptsOut() {
        val winNotif = notificationRepo.notifyLotteryWin(
            recipientDeviceId = "entrant_opted_out",
            lotteryTitle = "Banff Draw",
            offerId = "offer-x",
            userPreferenceEnabled = false
        )
        assertNull("Notification should be null when suppressed by user opt-out", winNotif)

        val lossNotif = notificationRepo.notifyLotteryLoss(
            recipientDeviceId = "entrant_opted_out",
            lotteryTitle = "Banff Draw",
            userPreferenceEnabled = false
        )
        assertNull("Loss notification should be suppressed when opted out", lossNotif)

        val notifs = notificationRepo.getNotificationsForUser("entrant_opted_out")
        assertTrue("No notifications should be stored for opted out recipient", notifs.isEmpty())
    }

    @Test
    fun testMarkAsReadAndUnreadCount() {
        notificationRepo.clearAll()
        val notif1 = notificationRepo.notifyLotteryWin("device_a", "Draw A", "offer-1")!!
        val notif2 = notificationRepo.notifyLotteryLoss("device_a", "Draw B")!!

        assertEquals(2, notificationRepo.getUnreadCount("device_a"))

        notificationRepo.markAsRead(notif1.id)
        assertEquals(1, notificationRepo.getUnreadCount("device_a"))

        notificationRepo.markAllAsRead("device_a")
        assertEquals(0, notificationRepo.getUnreadCount("device_a"))
    }

    @Test
    fun testEntrantModelHelperMethods() {
        val entrant = Entrant(
            deviceId = "test_entrant_device",
            name = "Jordan Entrant",
            notificationsEnabled = true
        )

        val initialNotifs = entrant.getNotifications(notificationRepo)
        assertTrue(initialNotifs.isEmpty())

        notificationRepo.notifyLotteryWin(
            recipientDeviceId = entrant.deviceId,
            lotteryTitle = "Kananaskis Draw",
            offerId = "offer-k"
        )

        val updatedNotifs = entrant.getNotifications(notificationRepo)
        assertEquals(1, updatedNotifs.size)
        assertEquals(NotificationType.LOTTERY_WIN, updatedNotifs.first().type)
    }
}
