package com.example.quackapp

import com.example.quackapp.Controller.imageGalleryItems
import com.example.quackapp.Model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the admin Image Gallery filter.
 *
 * US 03.06.01: As an admin, I want to browse uploaded images so I can remove them if necessary.
 */
class ImageGalleryFilterTest {

    private val banff = Campsite(campId = "c1", name = "Tunnel Mountain", location = "Banff", optionalPromoImage = "https://img/c1.jpg")
    private val jasper = Campsite(campId = "c2", name = "Whistlers", location = "Jasper", optionalPromoImage = "https://img/c2.jpg")
    private val noImage = Campsite(campId = "c3", name = "Hinton Hideaway", location = "Hinton", optionalPromoImage = "")

    private val all = listOf(banff, jasper, noImage)

    @Test
    fun testOnlyCampsitesWithImagesAreShown() {
        assertEquals(listOf(banff, jasper), imageGalleryItems(all, ""))
    }

    @Test
    fun testSearchMatchesNameIgnoringCase() {
        assertEquals(listOf(jasper), imageGalleryItems(all, "whistLERS"))
    }

    @Test
    fun testSearchMatchesLocation() {
        assertEquals(listOf(banff), imageGalleryItems(all, " banff "))
    }

    @Test
    fun testCampsiteWithoutImageNeverShownEvenIfSearched() {
        assertEquals(emptyList<Campsite>(), imageGalleryItems(all, "Hinton"))
    }
}
