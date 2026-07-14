package design.pulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import design.pulse.ui.theme.Pulse
import design.pulse.ui.theme.PulseMotion

/**
 * A segmented control — a hairline-bordered pill split into 2–4 choices, the selected one filled with
 * the channel's dim tone and channel-colored text. The classic settings/appearance toggle (and any
 * small exclusive choice); it's what the suite's Dark / Light / System switch is built from.
 *
 * String labels only (no generics) so it stays simple and the component index stays clean — apps map
 * their enum to labels + index. Channel defaults to the app accent; pass a data-domain channel to bind
 * the selection to a meaning. The selected fill crossfades on change.
 */
@Composable
fun PulseSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    channelDim: Color = Pulse.accent.dim,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(1.dp, Pulse.structure.hairline), shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { i, label ->
            Segment(
                label = label,
                selected = i == selectedIndex,
                onClick = { onSelect(i) },
                channel = channel,
                channelDim = channelDim,
            )
        }
    }
}

@Composable
private fun RowScope.Segment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    channel: Color,
    channelDim: Color,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) channelDim else Color.Transparent,
        animationSpec = PulseMotion.standard(),
        label = "segmentBg",
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .weight(1f)
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .background(bg)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) channel else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
