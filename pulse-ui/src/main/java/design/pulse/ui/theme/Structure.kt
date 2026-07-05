package design.pulse.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The PULSE structural layer, app-agnostic.
 *
 * Apps own their channel *semantics* (Plate maps hues to macros, Spotter to workout domains,
 * Cookbook to kitchen moments) — the library only knows about:
 *  - [PulseChannel]: one hue as a (base / dim / on) triple. `base` for strokes, text and rings;
 *    `dim` a pre-composited solid container fill (not an alpha, so hairlines drawn on top stay
 *    predictable); `on` for content atop a base-filled surface.
 *  - [PulseStructure]: the instrument-panel bones — hairline strokes, panel tones, glow — plus the
 *    app's two gradient voices: [PulseStructure.heroGradient] (primary CTAs, greeting panels,
 *    derived from the app's accent) and [PulseStructure.energyGradient] (celebrations,
 *    orange→amber, shared family-wide).
 *  - [PulseAccent]: which hue leads the app. Spotter/Plate lead blue; Cookbook leads amber.
 *
 * Components default their channel to the app accent via [LocalPulseAccentChannel]; screens pass
 * their domain's channel explicitly when a surface belongs to a specific data domain.
 */
@Immutable
data class PulseChannel(
    val base: Color,
    val dim: Color,
    val on: Color,
)

@Immutable
data class PulseStructure(
    val hairline: Color,        // 1px inner strokes on panels
    val hairlineStrong: Color,  // emphasized strokes (selected states)
    val panel: Color,
    val panelHigh: Color,
    val glow: Color,            // ring/dot glow base; draw at low alpha
    val heroGradient: Brush,    // the app's lead voice: primary CTAs, greeting panels
    val energyGradient: Brush,  // celebration voice (orange → amber), shared family-wide
)

/** Which reference hue leads the app — drives the M3 primary family, hero gradient and accent channel. */
enum class PulseAccent { Blue, Amber, Green, Violet, Teal }

// Channel triples, shared verbatim across the family (values from the original PULSE build).

fun darkBlueChannel() = PulseChannel(PulseBlue, Color(0xFF151C33), Color(0xFFFFFFFF))
fun darkGreenChannel() = PulseChannel(PulseGreen, Color(0xFF11332A), Color(0xFF00301F))
fun darkAmberChannel() = PulseChannel(PulseOrange, Color(0xFF3B2418), Color(0xFF2B1100))
fun darkVioletChannel() = PulseChannel(PulseViolet, Color(0xFF231F3F), Color(0xFF120A38))
fun darkTealChannel() = PulseChannel(PulseTeal, Color(0xFF0F2E2B), Color(0xFF00312D))

fun lightBlueChannel() = PulseChannel(PulseBlueDeep, Color(0xFFECF1FF), Color(0xFFFFFFFF))
fun lightGreenChannel() = PulseChannel(PulseGreenDeep, Color(0xFFD8F3E8), Color(0xFFFFFFFF))
fun lightAmberChannel() = PulseChannel(PulseOrangeDeep, Color(0xFFFBE3D4), Color(0xFFFFFFFF))
fun lightVioletChannel() = PulseChannel(PulseVioletDeep, Color(0xFFE6E2FB), Color(0xFFFFFFFF))
fun lightTealChannel() = PulseChannel(PulseTealDeep, Color(0xFFD5F3EF), Color(0xFFFFFFFF))

fun darkChannel(accent: PulseAccent): PulseChannel = when (accent) {
    PulseAccent.Blue -> darkBlueChannel()
    PulseAccent.Amber -> darkAmberChannel()
    PulseAccent.Green -> darkGreenChannel()
    PulseAccent.Violet -> darkVioletChannel()
    PulseAccent.Teal -> darkTealChannel()
}

fun lightChannel(accent: PulseAccent): PulseChannel = when (accent) {
    PulseAccent.Blue -> lightBlueChannel()
    PulseAccent.Amber -> lightAmberChannel()
    PulseAccent.Green -> lightGreenChannel()
    PulseAccent.Violet -> lightVioletChannel()
    PulseAccent.Teal -> lightTealChannel()
}

// Hero gradients per accent. The dark blue hero uses the deeper hues so white headline text
// clears WCAG AA 4.5:1 (white on PulseBlue is 3.72; on PulseBlueDeep 5.20 — measured in Plate).
// The amber hero pairs with dark ink content (the amber channel's `on`), not white.
private fun heroGradient(accent: PulseAccent, dark: Boolean): Brush = when (accent) {
    PulseAccent.Blue ->
        if (dark) Brush.linearGradient(listOf(PulseBlueDeep, PulseIndigo))
        else Brush.linearGradient(listOf(PulseBlueDeep, PulseIndigoDeep))
    PulseAccent.Amber ->
        if (dark) Brush.linearGradient(listOf(PulseOrange, PulseAmber))
        else Brush.linearGradient(listOf(Color(0xFFFF6B35), PulseAmber))
    PulseAccent.Green ->
        Brush.linearGradient(listOf(PulseGreenDeep, PulseBlueDeep))
    PulseAccent.Violet ->
        if (dark) Brush.linearGradient(listOf(PulseIndigo, PulseViolet))
        else Brush.linearGradient(listOf(PulseIndigoDeep, PulseVioletDeep))
    // Teal hero is a 3-stop indigo→teal→green sweep (the "magpie" gradient) — a nod to the
    // structural iridescence on a real magpie's wing, which shifts blue→teal→green under
    // raking light rather than sitting on one hue. Deep hues in both modes (the Green
    // precedent): PulseIndigoDeep and PulseGreenDeep already clear 4.5:1 for white headline
    // text elsewhere (Dragonfly's light-mode hero, and the Green hero above); PulseTealDeep
    // sits tonally between them, so the white-text guarantee holds across the whole sweep.
    PulseAccent.Teal ->
        Brush.linearGradient(listOf(PulseIndigoDeep, PulseTealDeep, PulseGreenDeep))
}

private fun energyGradient(dark: Boolean): Brush =
    if (dark) Brush.linearGradient(listOf(PulseOrange, PulseAmber))
    else Brush.linearGradient(listOf(Color(0xFFFF6B35), PulseAmber))

fun darkPulseStructure(accent: PulseAccent = PulseAccent.Blue) = PulseStructure(
    hairline = Color(0x14FFFFFF),
    hairlineStrong = Color(0x29FFFFFF),
    panel = PulsePanel,
    panelHigh = PulsePanelHigh,
    glow = darkChannel(accent).base,
    heroGradient = heroGradient(accent, dark = true),
    energyGradient = energyGradient(dark = true),
)

fun lightPulseStructure(accent: PulseAccent = PulseAccent.Blue) = PulseStructure(
    hairline = Color(0x1A000000),
    hairlineStrong = Color(0x33000000),
    panel = Color(0xFFFFFFFF),
    panelHigh = Color(0xFFF1F3F6),
    glow = lightChannel(accent).base,
    heroGradient = heroGradient(accent, dark = false),
    energyGradient = energyGradient(dark = false),
)

val LocalPulseStructure = staticCompositionLocalOf { darkPulseStructure() }
val LocalPulseAccentChannel = staticCompositionLocalOf { darkChannel(PulseAccent.Blue) }
