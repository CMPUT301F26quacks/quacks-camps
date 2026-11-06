package com.example.quackapp.View


import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Function which turns a a date stored as millis (type Long)
 * into readable text like “Jan 1, 2026”
 * @return a date as a String
 */
fun Long.toDisplayDate(): String {
    val format = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return format.format(java.util.Date(this))
}

/**
 * Today's date as UTC midnight, so "today" is selectable in the pickers.
 * @return today's date as a Long
 */
fun todayUtcMillis(): Long {
    val local = java.util.Calendar.getInstance()   // device's local date
    val utc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            local.get(java.util.Calendar.YEAR),
            local.get(java.util.Calendar.MONTH),
            local.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
    return utc.timeInMillis
}


/**
 * Composable function for creating a popup calendar to select dates from
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerField(
    label: String,
    startMillis: Long?,
    endMillis: Long?,
    onRangeSelected: (Long?, Long?) -> Unit,
    modifier: Modifier = Modifier,
    minDateMillis: Long? = null
) {
    var showPicker by remember { mutableStateOf(false) }

    val text = if (startMillis != null && endMillis != null)
        "${startMillis.toDisplayDate()} – ${endMillis.toDisplayDate()}" else ""

    OutlinedTextField(
        value = text,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick dates") },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (up != null) showPicker = true
                }
            }
    )

    if (showPicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startMillis,
            initialSelectedEndDateMillis = endMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    minDateMillis == null || utcTimeMillis >= minDateMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                    onClick = {
                        onRangeSelected(state.selectedStartDateMillis, state.selectedEndDateMillis)
                        showPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = state, modifier = Modifier.height(500.dp))
        }
    }
}