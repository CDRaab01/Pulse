package design.pulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * The suite's standard appearance preference. Every app stores one of these (its own persistence —
 * DataStore/prefs) and resolves it to the boolean [PulseTheme] wants, so all five apps offer an
 * identical Dark / Light / System choice instead of each app only following the OS.
 *
 * ```
 * @Composable
 * fun PlateTheme(pref: ThemePref = ThemePref.System, content: @Composable () -> Unit) {
 *     PulseTheme(darkTheme = pref.resolveDarkTheme(), accent = PulseAccent.Green) { ... }
 * }
 * ```
 *
 * The light schemes already exist in [pulseLightColorScheme]; this is the missing user-facing switch.
 */
enum class ThemePref {
    System, Light, Dark;

    /** Resolve to the dark/light boolean [PulseTheme] takes; [System] follows the OS setting. */
    @Composable
    fun resolveDarkTheme(): Boolean = when (this) {
        System -> isSystemInDarkTheme()
        Light -> false
        Dark -> true
    }

    companion object {
        /** The order to render as segments in a Dark/Light/System control (System first — the default). */
        val segments: List<ThemePref> = listOf(System, Light, Dark)
    }
}
