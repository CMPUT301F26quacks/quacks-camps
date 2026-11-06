package com.example.quackapp

import com.example.quackapp.Model.User
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Unit tests for the User model.
 *
 * Tests that profile information, roles, and notification preferences
 * are stored correctly.
 */
class UserTest {

    @Test
    fun testUserStoresProfileInfo() {

        val user = User(
            deviceId = "test-device-123",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "780-555-1234",
            notificationsEnabled = true,
            roles = listOf("ENTRANT")
        )

        assertEquals("test-device-123", user.deviceId)
        assertEquals("Test User", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals("780-555-1234", user.phoneNumber)
        assertEquals(listOf("ENTRANT"), user.roles)
        assertTrue(user.notificationsEnabled)
    }

    /**
     * US 01.10.01
     * Users can opt out of receiving notifications.
     */
    @Test
    fun testNotificationPreferenceToggling() {

        val user = User(
            deviceId = "testid"
        )

        assertTrue(user.notificationsEnabled)

        user.notificationsEnabled = false

        assertFalse(user.notificationsEnabled)
    }
}

