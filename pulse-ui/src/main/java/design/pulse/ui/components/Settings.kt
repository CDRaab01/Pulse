package design.pulse.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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

/**
 * A titled settings group: a [PanelCard] with a channel-ticked [SectionHeader] over its content.
 * [channel] tints both the card stroke and the header tick when the group belongs to a data domain.
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    channel: Color? = null,
    content: @Composable () -> Unit,
) {
    PanelCard(modifier = modifier.fillMaxWidth(), channel = channel) {
        SectionHeader(title, channel = channel)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/**
 * A settings toggle: [title] (plus optional [subtitle]) with a trailing [Switch]. The row keeps a
 * 48dp minimum height and a fixed gutter before the switch, so long explanatory copy can never
 * crowd or collide with the control — the failure mode every hand-rolled version of this row has
 * hit. A null [subtitle] renders the single-line form.
 *
 * [enabled] gates the switch only; the copy stays full-strength, because a row whose switch is
 * unavailable is exactly when the user most needs to be able to read why.
 */
@Composable
fun PulseSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = MinRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(Pulse.spacing.md))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

/**
 * The settings list row — one shape for every "label on the left, something on the right" line:
 * a navigation row (pass [onClick] and it gains a chevron), a value readout (pass [value] with no
 * [onClick], e.g. an app version), or a row mid-action (pass a spinner as [trailing] with
 * `chevron = false`). [leading] takes a glyph, [trailing] renders before the chevron.
 *
 * Rows keep a 48dp minimum height so they stay comfortably tappable.
 */
@Composable
fun PulseSettingRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    chevron: Boolean? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val showChevron = chevron ?: (onClick != null)
    val alpha = if (enabled) 1f else DisabledAlpha
    val clickable = if (onClick != null && enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinRowHeight)
            .then(clickable)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Pulse.spacing.md),
    ) {
        if (leading != null) {
            Box(Modifier.alpha(alpha)) { leading() }
        }
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                textAlign = TextAlign.End,
            )
        }
        trailing?.invoke()
        if (showChevron) {
            Icon(
                ChevronGlyph,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * A tap-to-set time row: [label] on the left, the formatted clock value on the right, opening a
 * Material 3 time picker.
 *
 * [onTimeChange] reports hour **and** minute because the picker always shows minutes — a caller
 * that dropped them would be showing the user a time it isn't going to honour. Pass [is24Hour]
 * explicitly in screenshot tests; it otherwise follows the device's clock setting, which would
 * make baselines locale-dependent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseTimeRow(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
    dialogTitle: String = label,
) {
    var picking by remember { mutableStateOf(false) }
    PulseSettingRow(
        label = label,
        modifier = modifier,
        value = formatTimeOfDay(hour, minute, is24Hour),
        subtitle = subtitle,
        chevron = false,
        onClick = { picking = true },
        enabled = enabled,
    )
    if (picking) {
        val state = rememberTimePickerState(
            initialHour = hour.coerceIn(0, 23),
            initialMinute = minute.coerceIn(0, 59),
            is24Hour = is24Hour,
        )
        TimePickerDialog(
            onDismissRequest = { picking = false },
            title = { Text(dialogTitle) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(state.hour, state.minute)
                    picking = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { picking = false }) { Text("Cancel") }
            },
        ) {
            TimePicker(state = state)
        }
    }
}

/**
 * A −/+ stepper row for a small integer setting — a count, a cadence, a quantity. Use
 * [PulseTimeRow] for a clock time instead.
 *
 * [wrap] makes the ends roll over rather than clamp. The label takes the flexible width and the
 * buttons are fixed 48dp targets, so the row can never out-measure its card (a stepper pair built
 * from min-width buttons and a fixed-width value silently clips its trailing control).
 */
@Composable
fun PulseStepperRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 23,
    wrap: Boolean = false,
    valueLabel: (Int) -> String = { it.toString() },
) {
    val span = max - min + 1
    fun step(delta: Int): Int = if (wrap) {
        min + (((value - min + delta) % span) + span) % span
    } else {
        (value + delta).coerceIn(min, max)
    }
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = MinRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(
            onClick = { onValueChange(step(-1)) },
            enabled = wrap || value > min,
            modifier = Modifier.size(MinRowHeight),
        ) { Icon(MinusGlyph, contentDescription = null, modifier = Modifier.size(20.dp)) }
        Text(
            valueLabel(value),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 56.dp),
        )
        IconButton(
            onClick = { onValueChange(step(1)) },
            enabled = wrap || value < max,
            modifier = Modifier.size(MinRowHeight),
        ) { Icon(PlusGlyph, contentDescription = null, modifier = Modifier.size(20.dp)) }
    }
}

/**
 * Settings rows are tappable controls, so they hold the platform's 48dp minimum target — the
 * convention for every row in this file.
 */
private val MinRowHeight = 48.dp

/** Disabled rows dim rather than disappear; the copy must stay readable. */
private const val DisabledAlpha = 0.4f

/** 12h/24h clock label ("9:05 PM" / "21:05"), used by [PulseTimeRow]'s value. */
internal fun formatTimeOfDay(hour: Int, minute: Int, is24Hour: Boolean): String {
    val h = hour.coerceIn(0, 23)
    val m = minute.coerceIn(0, 59)
    if (is24Hour) return "%02d:%02d".format(h, m)
    val suffix = if (h < 12) "AM" else "PM"
    val display = when {
        h % 12 == 0 -> 12
        else -> h % 12
    }
    return "%d:%02d %s".format(display, m, suffix)
}

// Glyphs are embedded as path data — pulse-ui deliberately carries no material-icons dependency
// (see StaleBanner). Fill color is irrelevant; Icon() tints them.
private val ChevronGlyph: ImageVector by lazy {
    glyph("PulseChevronRight", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z")
}

private val MinusGlyph: ImageVector by lazy { glyph("PulseMinus", "M19 13H5v-2h14v2z") }

private val PlusGlyph: ImageVector by lazy {
    glyph("PulsePlus", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
}

private fun glyph(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.White),
    ).build()
