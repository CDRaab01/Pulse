package design.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import design.pulse.ui.theme.Pulse

/**
 * Settings building blocks, extracted from Spotter (the suite's settings reference) so every app's
 * Settings reads the same and themes correctly in dark mode — flat hairline [PanelCard]s, not the
 * default Material card that renders a muddy grey on OLED.
 */

/**
 * The account header: an accent-tinted avatar with the user's initial, their name, and email — on a
 * [PanelCard]. The avatar takes the app's lead accent ([Pulse.accent]) unless a [channel] is passed.
 */
@Composable
fun ProfileHeader(
    name: String,
    email: String,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    channelDim: Color = Pulse.accent.dim,
) {
    PanelCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).background(channelDim, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.trim().take(1).uppercase().ifBlank { "?" },
                    style = MaterialTheme.typography.titleLarge,
                    color = channel,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A titled settings group: a [PanelCard] with a channel-ticked [SectionHeader] over its content. */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PanelCard(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title)
        Spacer(Modifier.height(8.dp))
        content()
    }
}
