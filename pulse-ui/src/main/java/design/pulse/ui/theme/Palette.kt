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
val PulseRed = Color(0xFFFF5C5C)

// Deep variants — meet >= 4.5:1 contrast on white for the light theme.
val PulseBlueDeep = Color(0xFF2A5BFF)
val PulseIndigoDeep = Color(0xFF5B2BE0)
val PulseVioletDeep = Color(0xFF5B2BE0)
val PulseOrangeDeep = Color(0xFFC2410C)
val PulseGreenDeep = Color(0xFF047857)
val PulseGreenDeeper = Color(0xFF064E3B) // forest green — deep end of the emerald hero ramp (Plate)
val PulseTealDeep = Color(0xFF0F766E)
val PulseRedDeep = Color(0xFFDC2626)
