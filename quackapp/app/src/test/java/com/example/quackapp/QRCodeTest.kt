package com.example.quackapp
import com.example.quackapp.Model.Campsite
import com.example.quackapp.Model.CampsiteOwner
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Unit tests to test QR Code functionality works between entrant and campsite owner
 */

class QRCodeTest {
    @Test
    fun testQRCodeIDFormat() {
        val owner = CampsiteOwner(deviceId = "owner_device")
        val campsite = Campsite(campId = "123", name = "bob")
        val qrCodeID = owner.generateQRCodeID(campsite)
        assertEquals(qrCodeID, "campsite/123")
    }

    @Test
    fun testQRCode() {}

}