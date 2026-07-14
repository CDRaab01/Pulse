package design.pulse.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import design.pulse.ui.theme.Pulse
import design.pulse.ui.theme.PulseBlue
import design.pulse.ui.theme.PulseGreen
import design.pulse.ui.theme.PulseMotion
import design.pulse.ui.theme.PulseOrange
import design.pulse.ui.theme.PulseViolet
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * The family's default confetti palette — the four brand hues. Kept deliberately festive-but-restrained
 * (see [ConfettiHost]); pass your own list to [ConfettiHost] to theme a burst to a single data domain.
 */
val PulseConfettiColors: List<Color> = listOf(PulseBlue, PulseViolet, PulseOrange, PulseGreen)

/**
 * A one-shot celebration burst — deliberately sparse and slow so it reads as a *signal*, not a party
 * store. Drop it on top of the content; it renders nothing until [play] flips true.
 *
 * Extracted verbatim from Spotter (the suite's celebration reference) so its adoption there is a
 * zero-visual-diff swap. [colors] defaults to the four brand hues ([PulseConfettiColors]); apps that
 * want a domain-tinted burst (e.g. a single-channel PR pop) pass their own.
 *
 * Pulse owns *how it moves*; the app decides *what* is worth celebrating — never call this on a routine
 * action.
 */
@Composable
fun ConfettiHost(
    play: Boolean,
    modifier: Modifier = Modifier,
    colors: List<Color> = PulseConfettiColors,
) {
    if (!play) return
    val argb = colors.map { it.toArgb() }
    KonfettiView(
        modifier = modifier.fillMaxSize(),
        parties = listOf(
            Party(
                speed = 0f,
                maxSpeed = 18f,
                damping = 0.9f,
                spread = 360,
                colors = argb,
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(40),
            ),
        ),
    )
}

/**
 * A soft expanding ring-glow behind [content] — the quieter celebration for badges and summary
 * checkmarks, where full [ConfettiHost] confetti would be too loud.
 *
 * [channel] defaults to the app's lead accent ([Pulse.accent]); pass a data-domain channel color to
 * tint the glow to a specific meaning (the hue→meaning mapping lives app-side, never here).
 */
@Composable
fun CelebrationPulse(
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "celebration")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = PulseMotion.EaseDecel)),
        label = "celebrationRing",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val radius = (size.minDimension / 2f) * (0.6f + 0.4f * progress)
            drawCircle(channel.copy(alpha = (1f - progress) * 0.35f), radius = radius)
        }
        content()
    }
}
