package com.example.quackapp

import com.example.quackapp.Model.Admin
import com.example.quackapp.View.startRouteFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the admin role and the start-screen routing decision.
 *
 * US 03.11.01: an admin can also be a campsite owner and/or entrant with the same profile.
 */
class AdminRoleTest {

    @Test
    fun testAdminDefaultsToAdminRole() {
        val admin = Admin(deviceId = "admin-device-1", name = "Ada Admin")
        assertEquals(listOf("ADMIN"), admin.roles)
        assertTrue(Admin.isAdmin(admin.roles))
    }

    @Test
    fun testEntrantIsNotAdmin() {
        assertFalse(Admin.isAdmin(listOf("ENTRANT")))
        assertFalse(Admin.isAdmin(emptyList()))
    }

    @Test
    fun testAdminStartsOnAdminHome() {
        assertEquals("AdminHome", startRouteFor(listOf("ADMIN"), "d1"))
    }

    /** US 03.11.01: admins who are also entrants/owners still start on the admin home. */
    @Test
    fun testAdminWithOtherRolesStartsOnAdminHome() {
        assertEquals("AdminHome", startRouteFor(listOf("ENTRANT", "ADMIN"), "d1"))
        assertEquals("AdminHome", startRouteFor(listOf("OWNER", "ENTRANT", "ADMIN"), "d1"))
    }

    @Test
    fun testEntrantAndOwnerRoutesUnchanged() {
        assertEquals("EntrantHome", startRouteFor(listOf("ENTRANT"), "d1"))
        assertEquals("CampsiteOwnerHome/d1", startRouteFor(listOf("OWNER"), "d1"))
    }

    @Test
    fun testNoRolesHasNoRoute() {
        assertNull(startRouteFor(emptyList(), "d1"))
    }
}
