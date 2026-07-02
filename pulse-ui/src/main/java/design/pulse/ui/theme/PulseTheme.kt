package design.pulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The PULSE theme root. Apps wrap this in their own theme composable (PlateTheme, CookbookTheme…)
 * that picks the app's [PulseAccent] and layers the app's domain-channel CompositionLocal on top:
 *
 * ```
 * @Composable
 * fun CookbookTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
 *     PulseTheme(darkTheme, PulseAccent.Amber) {
 *         CompositionLocalProvider(LocalCookbookColors provides cookbookColors(darkTheme)) {
 *             content()
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun PulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: PulseAccent = PulseAccent.Blue,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) pulseDarkColorScheme(accent) else pulseLightColorScheme(accent)
    val structure = if (darkTheme) darkPulseStructure(accent) else lightPulseStructure(accent)
    val accentChannel = if (darkTheme) darkChannel(accent) else lightChannel(accent)
    CompositionLocalProvider(
        LocalPulseStructure provides structure,
        LocalPulseAccentChannel provides accentChannel,
        LocalDataTypography provides pulseDataTypography(),
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PulseTypography,
            shapes = PulseShapes,
            content = content,
        )
    }
}

/** Convenience accessors mirroring `MaterialTheme.*`. */
object Pulse {
    val structure: PulseStructure
        @Composable @ReadOnlyComposable get() = LocalPulseStructure.current
    val accent: PulseChannel
        @Composable @ReadOnlyComposable get() = LocalPulseAccentChannel.current
    val dataType: PulseDataTypography
        @Composable @ReadOnlyComposable get() = LocalDataTypography.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}
