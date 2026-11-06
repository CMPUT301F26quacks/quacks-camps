package com.example.quackapp.Model

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting and downloading campsite reservations as CSV files.
 * Implements requirements for:
 * - US 01.05.02: As an entrant, I want to receive reservation by notification or downloadable CSV.
 */
object ReservationCsvExporter {

    private const val CSV_HEADER = "Reservation ID,Campsite ID,Campsite Name,Location,Dates,Guests,Total Price (CAD),Status,Created At"

    /**
     * Formats a single [Reservation] into standard RFC 4180 CSV text.
     *
     * @param reservation The reservation to format.
     * @return Complete CSV formatted string with headers and row data.
     */
    fun generateCsv(reservation: Reservation): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(reservation.createdAt))

        val row = listOf(
            escapeCsv(reservation.id),
            escapeCsv(reservation.campsiteId),
            escapeCsv(reservation.campsiteName),
            escapeCsv(reservation.campsiteLocation),
            escapeCsv(reservation.dateRange),
            reservation.numGuests.toString(),
            String.format(Locale.US, "%.2f", reservation.totalPrice),
            reservation.status.name,
            escapeCsv(formattedDate)
        ).joinToString(",")

        return "$CSV_HEADER\n$row\n"
    }

    /**
     * Formats a list of [Reservation] records into a multi-row CSV text.
     *
     * @param reservations List of reservations to export.
     * @return Complete CSV formatted string.
     */
    fun generateReservationsCsv(reservations: List<Reservation>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\n")

        reservations.forEach { res ->
            val formattedDate = dateFormat.format(Date(res.createdAt))
            val row = listOf(
                escapeCsv(res.id),
                escapeCsv(res.campsiteId),
                escapeCsv(res.campsiteName),
                escapeCsv(res.campsiteLocation),
                escapeCsv(res.dateRange),
                res.numGuests.toString(),
                String.format(Locale.US, "%.2f", res.totalPrice),
                res.status.name,
                escapeCsv(formattedDate)
            ).joinToString(",")
            sb.append(row).append("\n")
        }

        return sb.toString()
    }

    /**
     * Escapes fields containing commas, double quotes, or newlines according to CSV standards.
     */
    private fun escapeCsv(value: String): String {
        var escaped = value.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }

    /**
     * Saves a reservation CSV to the app's cache directory and returns the [File].
     *
     * @param context Android context.
     * @param reservation The reservation to write.
     * @return Created CSV file.
     */
    fun saveCsvToFile(context: Context, reservation: Reservation): File {
        val exportDir = File(context.cacheDir, "reservation_exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val safeId = reservation.id.take(8)
        val file = File(exportDir, "campsite_reservation_$safeId.csv")
        FileWriter(file).use { writer ->
            writer.write(generateCsv(reservation))
        }
        return file
    }

    /**
     * Launches the Android Share/Download sheet allowing the entrant to save, open,
     * or export their reservation receipt as a CSV file.
     *
     * @param context Android context.
     * @param reservation Target reservation.
     * @return True if share intent launched successfully; false on failure.
     */
    fun exportAndShareCsv(context: Context, reservation: Reservation): Boolean {
        return try {
            val file = saveCsvToFile(context, reservation)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "QuackCamps Reservation Receipt - ${reservation.campsiteName}")
                putExtra(Intent.EXTRA_TEXT, "Attached is your confirmed campsite reservation receipt for ${reservation.campsiteName} (${reservation.dateRange}).")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Export Reservation CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
