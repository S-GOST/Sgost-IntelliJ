package org.sgost.project.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KtmOrange = Color(0xFFFF6600)
val RacingBlack = Color(0xFF0F0F10)
val CarbonGray = Color(0xFF202124)
val TrackWhite = Color(0xFFF7F7F2)
val WarningAmber = Color(0xFFFFB000)

private val DarkColors = darkColorScheme(
    primary = KtmOrange,
    onPrimary = Color.Black,
    secondary = WarningAmber,
    background = RacingBlack,
    onBackground = TrackWhite,
    surface = CarbonGray,
    onSurface = TrackWhite,
    error = Color(0xFFFF5A5F),
)

private val LightColors = lightColorScheme(
    primary = KtmOrange,
    onPrimary = Color.Black,
    secondary = WarningAmber,
    background = TrackWhite,
    onBackground = RacingBlack,
    surface = Color.White,
    onSurface = RacingBlack,
    error = Color(0xFFB3261E),
)

@Composable
fun SgostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
