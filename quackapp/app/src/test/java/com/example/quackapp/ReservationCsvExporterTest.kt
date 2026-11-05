package com.example.quackapp

import com.example.quackapp.Model.Entrant
import com.example.quackapp.Model.Reservation
import com.example.quackapp.Model.ReservationCsvExporter
import com.example.quackapp.Model.ReservationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ReservationCsvExporter] validating:
 * - US 01.05.02: As an entrant, I want to receive reservation by downloadable CSV.
 */
class ReservationCsvExporterTest {

    private val sampleReservation = Reservation(
        id = "res-csv-test-01",
        campsiteId = "site-lake-minnewanka",
        campsiteName = "Lake Minnewanka Shoreline, Lot 4",
        campsiteLocation = "Banff National Park, AB",
        entrantDeviceId = "device-1234",
        entrantName = "Taylor Entrant",
        dateRange = "August 1 - August 3, 2027",
        numGuests = 4,
        totalPrice = 145.50,
        status = ReservationStatus.CONFIRMED,
        createdAt = 1715000000000L
    )

    @Test
    fun testGenerateCsvContainsRequiredHeaders() {
        val csv = ReservationCsvExporter.generateCsv(sampleReservation)
        val lines = csv.trim().lines()

        assertTrue("CSV should have at least 2 lines (header + 1 row)", lines.size >= 2)
        val header = lines.first()
        assertTrue("Header should contain Reservation ID", header.contains("Reservation ID"))
        assertTrue("Header should contain Campsite Name", header.contains("Campsite Name"))
        assertTrue("Header should contain Location", header.contains("Location"))
        assertTrue("Header should contain Dates", header.contains("Dates"))
        assertTrue("Header should contain Guests", header.contains("Guests"))
        assertTrue("Header should contain Total Price", header.contains("Total Price"))
        assertTrue("Header should contain Status", header.contains("Status"))
    }

    @Test
    fun testGenerateCsvEscapesFieldsWithCommas() {
        val csv = ReservationCsvExporter.generateCsv(sampleReservation)
        val lines = csv.trim().lines()
        val dataRow = lines[1]

        // Campsite name contains a comma ("Lake Minnewanka Shoreline, Lot 4"), so it must be enclosed in quotes
        assertTrue("Fields with commas should be quoted", dataRow.contains("\"Lake Minnewanka Shoreline, Lot 4\""))
        assertTrue("Location with comma should be quoted", dataRow.contains("\"Banff National Park, AB\""))
        assertTrue("Data row must contain reservation ID", dataRow.contains("res-csv-test-01"))
        assertTrue("Data row must contain total price", dataRow.contains("145.50"))
        assertTrue("Data row must contain CONFIRMED status", dataRow.contains("CONFIRMED"))
    }

    @Test
    fun testGenerateReservationsCsvBatchExport() {
        val res2 = sampleReservation.copy(
            id = "res-csv-test-02",
            campsiteName = "Athabasca Falls Camp",
            totalPrice = 90.0
        )
        val batchCsv = ReservationCsvExporter.generateReservationsCsv(listOf(sampleReservation, res2))
        val lines = batchCsv.trim().lines()

        assertEquals("Batch CSV should have 3 lines (1 header + 2 rows)", 3, lines.size)
        assertTrue(lines[1].contains("res-csv-test-01"))
        assertTrue(lines[2].contains("res-csv-test-02"))
    }

    @Test
    fun testEntrantExportReservationCsvHelper() {
        val entrant = Entrant(deviceId = "device-1234", name = "Taylor Entrant")
        val csv = entrant.exportReservationCsv(sampleReservation)

        assertTrue(csv.contains("Reservation ID"))
        assertTrue(csv.contains("res-csv-test-01"))
    }
}
