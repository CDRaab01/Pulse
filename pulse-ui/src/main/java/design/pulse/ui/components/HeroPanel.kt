package design.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import design.pulse.ui.theme.Pulse

/**
 * The hero panel — a gradient-filled greeting/CTA surface, the app's lead voice at the top of a
 * screen. Standardized from the near-identical hero panels Plate and Magpie each invented app-side.
 *
 * Defaults to the app's [PulseStructure.heroGradient] (derived from the accent); pass
 * [Pulse.structure.energyGradient] for the celebration voice, or any [gradient] for a one-off. Content
 * runs in a [ColumnScope] — hero headline text is typically light/on-gradient, so set your text colors
 * for the gradient rather than relying on the surface scheme.
 */
@Composable
fun HeroPanel(
    modifier: Modifier = Modifier,
    gradient: Brush = Pulse.structure.heroGradient,
    contentPadding: Dp = Pulse.spacing.xl,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(gradient)
            .padding(contentPadding),
        content = content,
    )
}
