package design.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ripple
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import design.pulse.ui.theme.Pulse

/**
 * The primary action: the app's hero gradient with press-scale and ripple. Use one per screen;
 * secondary actions take [tonal] (channel dim fill, channel text) or plain M3 buttons.
 *
 * Pass [gradient] to switch the voice (e.g. `Pulse.structure.energyGradient` for a celebration
 * CTA), or [channel]/[onChannel]/[dimChannel] + `tonal` when an action belongs to a specific data
 * domain. Defaults follow the app's accent channel.
 */
@Composable
fun PulseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tonal: Boolean = false,
    compact: Boolean = false,
    gradient: Brush? = null,
    channel: Color = Pulse.accent.base,
    onChannel: Color = Pulse.accent.on,
    dimChannel: Color = Pulse.accent.dim,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }
    val brush = if (tonal) {
        SolidColor(dimChannel)
    } else {
        gradient ?: Pulse.structure.heroGradient
    }
    val content = if (tonal) channel else onChannel
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .pressScale(interaction)
            .clip(shape)
            .background(brush)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = content),
                enabled = enabled,
                onClick = onClick,
            )
            .heightIn(min = if (compact) 40.dp else 52.dp)
            .padding(horizontal = if (compact) Pulse.spacing.lg else Pulse.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Pulse.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.invoke()
                    Text(text, color = content)
                }
            }
        }
    }
}
