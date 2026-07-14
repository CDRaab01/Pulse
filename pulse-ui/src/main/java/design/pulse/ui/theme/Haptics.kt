package design.pulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The suite's semantic haptics vocabulary — a theme-level token set, like [PulseMotion], not a visual
 * component. Every actuation gets a tick, every commit a confirm, every failure a warning, so the
 * apps feel like they have switches, not glass. Pair a haptic with its matching motion so animation
 * and vibration fire together (that pairing is what reads as "expensive").
 *
 * Consolidated from Hawksnest's proven control-haptics wrapper so all five apps buzz alike. Call these
 * names, not [HapticFeedback] directly, in components.
 */
class PulseHaptics(private val hf: HapticFeedback) {
    /** A light micro-tick: rep counts, scrubbing a value, crossing a list section. */
    fun tick() = hf.performHapticFeedback(HapticFeedbackType.SegmentTick)

    /** A choice made — a toggle on, a segment selected, an option card picked. */
    fun select() = hf.performHapticFeedback(HapticFeedbackType.ToggleOn)

    /** A choice cleared — a toggle off. */
    fun deselect() = hf.performHapticFeedback(HapticFeedbackType.ToggleOff)

    /** An action committed and confirmed (a save landed, a slide reached its end). */
    fun confirm() = hf.performHapticFeedback(HapticFeedbackType.Confirm)

    /**
     * A genuine milestone — a PR, a goal hit, onboarding complete. Deliberately the same positive
     * actuator as [confirm]; the *visual* ([ConfettiHost]/[CelebrationPulse]) carries the party.
     * Reserve it for real moments, never routine saves.
     */
    fun celebrate() = hf.performHapticFeedback(HapticFeedbackType.Confirm)

    /** An action failed, was rejected, or needs attention. */
    fun warning() = hf.performHapticFeedback(HapticFeedbackType.Reject)

    /** Crossing an actionable threshold mid-gesture (a slide arming its commit point). */
    fun threshold() = hf.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
}

/** The [PulseHaptics] vocabulary bound to the current composition's [LocalHapticFeedback]. */
@Composable
fun rememberPulseHaptics(): PulseHaptics {
    val hf = LocalHapticFeedback.current
    return remember(hf) { PulseHaptics(hf) }
}
