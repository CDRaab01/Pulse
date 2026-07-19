package design.pulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Material 3 token sets derived from the reference palette. The neutral/secondary/tertiary/error
 * families are shared verbatim across the app family; the primary family follows the app's
 * [PulseAccent] so M3-styled controls (text fields, switches, plain buttons) speak the app's lead
 * hue without every call site opting in.
 */

private data class PrimaryFamily(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color,
)

private fun darkPrimary(accent: PulseAccent) = when (accent) {
    PulseAccent.Blue -> PrimaryFamily(PulseBlue, Color(0xFFFFFFFF), Color(0xFF1B2440), Color(0xFFD6E0FF))
    PulseAccent.Amber -> PrimaryFamily(PulseOrange, Color(0xFF2B1100), Color(0xFF3B2418), Color(0xFFFFDCC9))
    PulseAccent.Green -> PrimaryFamily(PulseGreen, Color(0xFF00301F), Color(0xFF11332A), Color(0xFFB9F2DC))
    PulseAccent.Violet -> PrimaryFamily(PulseViolet, Color(0xFF120A38), Color(0xFF231F3F), Color(0xFFDAD4FF))
    PulseAccent.Teal -> PrimaryFamily(PulseTeal, Color(0xFF00312D), Color(0xFF0F2E2B), Color(0xFFB0F1E8))
    PulseAccent.Rose -> PrimaryFamily(PulseRose, Color(0xFF3D0716), Color(0xFF331721), Color(0xFFFFD9E0))
}

private fun lightPrimary(accent: PulseAccent) = when (accent) {
    PulseAccent.Blue -> PrimaryFamily(PulseBlueDeep, Color(0xFFFFFFFF), Color(0xFFDEE7FF), Color(0xFF0A2078))
    PulseAccent.Amber -> PrimaryFamily(PulseOrangeDeep, Color(0xFFFFFFFF), Color(0xFFFBE3D4), Color(0xFF5C2000))
    PulseAccent.Green -> PrimaryFamily(PulseGreenDeep, Color(0xFFFFFFFF), Color(0xFFD8F3E8), Color(0xFF02382A))
    PulseAccent.Violet -> PrimaryFamily(PulseVioletDeep, Color(0xFFFFFFFF), Color(0xFFE6E2FB), Color(0xFF241C66))
    PulseAccent.Teal -> PrimaryFamily(PulseTealDeep, Color(0xFFFFFFFF), Color(0xFFD5F3EF), Color(0xFF00332E))
    PulseAccent.Rose -> PrimaryFamily(PulseRoseDeep, Color(0xFFFFFFFF), Color(0xFFFCE1E8), Color(0xFF4C0518))
}

fun pulseLightColorScheme(accent: PulseAccent = PulseAccent.Blue): ColorScheme {
    val p = lightPrimary(accent)
    return lightColorScheme(
        primary = p.primary,
        onPrimary = p.onPrimary,
        primaryContainer = p.container,
        onPrimaryContainer = p.onContainer,
        secondary = PulseGreenDeep,
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD8F3E8),
        onSecondaryContainer = Color(0xFF02382A),
        tertiary = PulseVioletDeep,
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE6E2FB),
        onTertiaryContainer = Color(0xFF241C66),
        error = PulseRedDeep,
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFBE0E0),
        onErrorContainer = Color(0xFF5C0E0E),
        background = Color(0xFFF4F6F8),
        onBackground = Color(0xFF14181D),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF14181D),
        surfaceVariant = Color(0xFFECEEF2),
        onSurfaceVariant = Color(0xFF525A66),
        outline = Color(0xFFC9CDD4),
        outlineVariant = Color(0x1A000000),
    )
}

fun pulseDarkColorScheme(accent: PulseAccent = PulseAccent.Blue): ColorScheme {
    val p = darkPrimary(accent)
    return darkColorScheme(
        primary = p.primary,
        onPrimary = p.onPrimary,
        primaryContainer = p.container,
        onPrimaryContainer = p.onContainer,
        secondary = PulseGreen,
        onSecondary = Color(0xFF00301F),
        secondaryContainer = Color(0xFF11332A),
        onSecondaryContainer = Color(0xFFB9F2DC),
        tertiary = PulseViolet,
        onTertiary = Color(0xFF120A38),
        tertiaryContainer = Color(0xFF231F3F),
        onTertiaryContainer = Color(0xFFDAD4FF),
        error = PulseRed,
        onError = Color(0xFF3D0202),
        errorContainer = Color(0xFF4A1414),
        onErrorContainer = Color(0xFFFFD3D3),
        background = PulseInk,
        onBackground = Color(0xFFE7EAF0),
        surface = PulsePanel,
        onSurface = Color(0xFFE7EAF0),
        surfaceVariant = PulsePanelHigh,
        onSurfaceVariant = Color(0xFF9AA3B2),
        outline = Color(0xFF2A2F38),
        outlineVariant = Color(0x14FFFFFF),
    )
}
