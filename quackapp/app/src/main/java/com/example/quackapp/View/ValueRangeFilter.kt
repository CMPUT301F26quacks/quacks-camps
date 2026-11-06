package com.example.quackapp.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

//TODO Add Tests

/**
 * Display a range slider from a min to a max value
 *
 * @param label Text above the range slider
 * @param range the currently selected range from min to max
 * @param onRangeChange Called while slider is being adjusted by entrant
 * @param valueRange the full value range from min to max
 * @param steps the number of increments on the slider
 * @param modifier adds modifiers if there are any
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValueRangeFilter(
    label: String,
    range: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$label: ${range.start.toInt()} - ${range.endInclusive.toInt()} people",
            style = MaterialTheme.typography.titleMedium
        )
        RangeSlider(
            value = range,
            onValueChange = onRangeChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFF2BA00),
                activeTrackColor = Color(0xFFF16B00)
            )
        )
    }
}