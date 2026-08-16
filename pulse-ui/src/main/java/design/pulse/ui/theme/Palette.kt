package design.pulse.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * PULSE reference palette — the single source of truth shared by every app in the family
 * (Spotter, Plate, Cookbook). Color always carries meaning: each hue is owned by a data domain
 * (the app decides which — see Structure.kt), and gradients are reserved for hero moments.
 */

// Neutral inks/panels + the channel hues and their WCAG-deep variants.
val PulseInk = Color(0xFF0B0D10)
val PulsePanel = Color(0xFF13161B)
val PulsePanelHigh = Color(0xFF1A1E25)
val PulseBlue = Color(0xFF4D7CFF)
val PulseIndigo = Color(0xFF7A45F0)
val PulseViolet = Color(0xFF8B7CFF)
val PulseOrange = Color(0xFFFF8A5C)
val PulseAmber = Color(0xFFF5A623)
val PulseGreen = Color(0xFF34D399)
val PulseTeal = Color(0xFF2DD4BF)
val PulseRose = Color(0xFFFB7185) // pinker than PulseRed on purpose — Rose is an accent, red stays the error voice
val PulseCopper = Color(0xFFD98A5B) // burnished metal — browner than PulseOrange, less golden than PulseAmber
val PulseSlate = Color(0xFF94A3B8) // cool grey-blue — the charcoal body of a site tote, dark-theme stroke weight
val PulseYellow = Color(0xFFF6D80B) // safety yellow — greener/purer than PulseAmber (52.3° vs 37.4° hue)
val PulseRed = Color(0xFFFF5C5C)

// Deep variants — meet >= 4.5:1 contrast on white for the light theme.
val PulseBlueDeep = Color(0xFF2A5BFF)
val PulseIndigoDeep = Color(0xFF5B2BE0)
val PulseVioletDeep = Color(0xFF5B2BE0)
val PulseOrangeDeep = Color(0xFFC2410C)
val PulseGreenDeep = Color(0xFF047857)
val PulseGreenDeeper = Color(0xFF064E3B) // forest green — deep end of the emerald hero ramp (Plate)
val PulseTealDeep = Color(0xFF0F766E)
val PulseRoseDeep = Color(0xFFBE123C)
val PulseCopperDeep = Color(0xFF9A4D1B)
val PulseSlateDeep = Color(0xFF334155) // 10.35:1 on white — the text-bearing half of the Slate accent
// Yellow's deep variant is an olive-gold, because that is what "yellow at 4.5:1 on white" actually
// is (4.92:1). Kept for the rare light-theme case that needs yellow to BEAR text; the Slate accent
// deliberately hands that job to PulseSlateDeep instead and uses yellow only as a field with ink
// on it. White on PulseYellow is 1.42:1 — it can never carry white text, in either theme.
val PulseYellowDeep = Color(0xFF8A6D00)
val PulseRedDeep = Color(0xFFDC2626)
