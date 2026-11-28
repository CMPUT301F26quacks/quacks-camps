package com.example.quackapp.ui.theme

import com.example.quackapp.R
//import androidx.compose.material3.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
/*
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
*/

val Quicksand = FontFamily(
    Font(R.font.quicksand_medium, FontWeight.Medium)
)

private val default = Typography()

private fun TextStyle.quick() = copy(
    fontFamily = Quicksand,
    fontWeight = FontWeight.Medium
)

val Typography = Typography(
    displayLarge = default.displayLarge.quick(),
    displayMedium = default.displayMedium.quick(),
    displaySmall = default.displaySmall.quick(),
    headlineLarge = default.headlineLarge.quick(),
    headlineMedium = default.headlineMedium.quick(),
    headlineSmall = default.headlineSmall.quick(),
    titleLarge = default.titleLarge.quick(),
    titleMedium = default.titleMedium.quick(),
    titleSmall = default.titleSmall.quick(),
    bodyLarge = default.bodyLarge.quick(),
    bodyMedium = default.bodyMedium.quick(),
    bodySmall = default.bodySmall.quick(),
    labelLarge = default.labelLarge.quick(),
    labelMedium = default.labelMedium.quick(),
    labelSmall = default.labelSmall.quick()
)