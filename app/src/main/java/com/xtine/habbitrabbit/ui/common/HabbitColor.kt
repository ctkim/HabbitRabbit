package com.xtine.habbitrabbit.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object HabbitColor {
    val palette: List<Color> = listOf(
        Color(0xFFE57373), // red
        Color(0xFFF06292), // pink
        Color(0xFFBA68C8), // purple
        Color(0xFF9575CD), // deep purple
        Color(0xFF64B5F6), // blue
        Color(0xFF4DB6AC), // teal
        Color(0xFF81C784), // green
        Color(0xFFDCE775), // lime
        Color(0xFFFFB74D), // orange
        Color(0xFF8D6E63)  // brown
    )

    val defaultArgb: Int = palette.first().toArgb()
}
