package design.pulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * One-line "this data is stale" banner for screens serving a cached/offline read: a channel-tinted
 * cloud-off glyph plus "Offline — as of h:mm a". Render it only when the data actually came from a
 * cache (asOfMs non-null at the call site) so fresh screens stay untouched.
 *
 * The channel is caller-supplied — apps bind it to their own warning/attention hue (Pulse knows
 * hues and structure, never meaning). [formatAsOf] is an overridable seam for apps that need a
 * different timestamp voice; the default reads "as of h:mm a" today and "as of MMM d, h:mm a"
 * for older captures, so a days-old cache reads honestly.
 */
@Composable
fun StaleBanner(
    asOfMs: Long,
    channel: Color,
    modifier: Modifier = Modifier,
    prefix: String = "Offline",
    formatAsOf: (Long) -> String = ::defaultFormatAsOf,
    icon: ImageVector = CloudOffGlyph,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = channel,
            modifier = Modifier.size(16.dp),
        )
        Text(
            "$prefix — ${formatAsOf(asOfMs)}",
            style = MaterialTheme.typography.labelMedium,
            color = channel,
        )
    }
}

/**
 * Default capture-time voice for [StaleBanner]: "as of h:mm a" when the timestamp is today,
 * "as of MMM d, h:mm a" otherwise.
 */
fun defaultFormatAsOf(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val dt = Instant.ofEpochMilli(epochMs).atZone(zone)
    val pattern = if (dt.toLocalDate() == LocalDate.now(zone)) "h:mm a" else "MMM d, h:mm a"
    return "as of " + dt.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
}

// Material "cloud off" outline, embedded as raw path data — pulse-ui deliberately carries no
// material-icons dependency (icon-taking components accept an ImageVector instead), so the one
// glyph this banner owns is built here. Fill color is irrelevant; Icon() tints via the channel.
private val CloudOffGlyph: ImageVector by lazy {
    ImageVector.Builder(
        name = "PulseCloudOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(
            "M19.35 10.04C18.67 6.59 15.64 4 12 4c-1.48 0-2.85.43-4.01 1.17l1.46 1.46C10.21 6.23 " +
                "11.08 6 12 6c3.04 0 5.5 2.46 5.5 5.5v.5H19c1.66 0 3 1.34 3 3 0 1.13-.64 2.11-1.56 " +
                "2.62l1.45 1.45C23.16 18.16 24 16.68 24 15c0-2.64-2.05-4.78-4.65-4.96zM3 5.27l2.75 " +
                "2.74C2.56 8.15 0 10.77 0 14c0 3.31 2.69 6 6 6h11.73l2 2 1.27-1.27L4.27 4 3 5.27zM7.73 " +
                "10l8 8H6c-2.21 0-4-1.79-4-4s1.79-4 4-4h1.73z"
        ).toNodes(),
        fill = SolidColor(Color.White),
    ).build()
}
