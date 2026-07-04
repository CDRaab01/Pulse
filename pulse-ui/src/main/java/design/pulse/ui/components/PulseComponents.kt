package design.pulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import design.pulse.ui.theme.Pulse
import design.pulse.ui.theme.PulseMotion

/**
 * The PULSE component kit. Flat panels (hairline stroke + tone, no shadows), monospace data
 * readouts, uppercase instrument captions, and a tiny sparkline — driven by the tokens in
 * [Pulse.structure] / [Pulse.dataType]. Channel defaults come from the app's accent
 * ([Pulse.accent]); pass a channel explicitly when a surface belongs to a specific data domain.
 */

/**
 * A flat, hairline-stroked surface — the base container for everything. Depth comes from tone
 * ([raised] uses the lighter panel) and stroke, never elevation. [channel] tints the *stroke* for a
 * panel that's "live" in a data domain; [tint]/[containerColor] override the *background* fill. When
 * [onClick] is set the whole panel is tappable with a press-scale. Content runs in a [ColumnScope].
 */
@Composable
fun PanelCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    channel: Color? = null,
    tint: Color? = null,
    raised: Boolean = false,
    containerColor: Color? = null,
    contentPadding: Dp = Pulse.spacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    val structure = Pulse.structure
    val shape = MaterialTheme.shapes.medium
    val color = containerColor ?: tint ?: if (raised) structure.panelHigh else structure.panel
    val border = BorderStroke(1.dp, channel?.copy(alpha = 0.35f) ?: structure.hairline)
    val inner: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
    if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier.pressScale(interaction),
            shape = shape,
            color = color,
            border = border,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            interactionSource = interaction,
            content = inner,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            border = border,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            content = inner,
        )
    }
}

/** A monospace numeric readout. Defaults to the small data scale; pass a [style] for a hero figure. */
@Composable
fun DataText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Pulse.dataType.dataSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(text = text, modifier = modifier, style = style, color = color)
}

/**
 * A [DataText] readout that rolls up to [target] when it first appears (and sweeps between values on
 * change), on the slower data easing so the number feels measured rather than printed.
 */
@Composable
fun TickerNumber(
    target: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = Pulse.dataType.dataSmall,
    color: Color = Color.Unspecified,
    prefix: String = "",
    suffix: String = "",
) {
    var goal by remember { mutableIntStateOf(0) }
    LaunchedEffect(target) { goal = target }
    val value by animateIntAsState(
        targetValue = goal,
        animationSpec = PulseMotion.data(),
        label = "ticker",
    )
    DataText(text = "$prefix$value$suffix", modifier = modifier, style = style, color = color)
}

/** An uppercase, wide-tracked caption — the instrument label voice. */
@Composable
fun Caption(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
        fontWeight = FontWeight.Medium,
        color = color,
    )
}

/** Section header: a channel-colored tick followed by an uppercase label. */
@Composable
fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
    channel: Color? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 3.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(channel ?: Pulse.accent.base),
        )
        Spacer(Modifier.width(8.dp))
        // No-trailing path is byte-identical to the original (existing consumers unchanged).
        if (trailing != null) {
            Caption(label, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            trailing()
        } else {
            Caption(label, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** A stat tile: caption + big mono value (+ optional unit), tinted by its channel. */
@Composable
fun StatTile(
    label: String,
    value: String? = null,
    modifier: Modifier = Modifier,
    unit: String? = null,
    channel: Color? = null,
    // Dense "metric" layout (Spotter): plain caption + small value, with an optional leading [icon],
    // a rolling [animatedValue] (+ [valueSuffix]), a trailing [sparkline], and tap via [onClick].
    // Left false, renders the standard tile (tick caption + prominent value + [unit]).
    dense: Boolean = false,
    animatedValue: Int? = null,
    valueSuffix: String = "",
    icon: (@Composable () -> Unit)? = null,
    sparkline: List<Float>? = null,
    onClick: (() -> Unit)? = null,
) {
    val ch = channel ?: Pulse.accent.base
    if (dense) {
        PanelCard(modifier = modifier, onClick = onClick, channel = channel, contentPadding = 14.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Pulse.spacing.xs),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Pulse.spacing.xs),
                ) {
                    icon?.invoke()
                    val valueColor = channel ?: MaterialTheme.colorScheme.onSurface
                    if (animatedValue != null) {
                        TickerNumber(
                            target = animatedValue,
                            suffix = valueSuffix,
                            style = Pulse.dataType.dataSmall,
                            color = valueColor,
                        )
                    } else {
                        DataText(text = value.orEmpty(), style = Pulse.dataType.dataSmall, color = valueColor)
                    }
                }
                if (sparkline != null && sparkline.size >= 2) {
                    Sparkline(
                        values = sparkline,
                        channel = ch,
                        asBars = false,
                        strokeWidth = 2.dp,
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                    )
                }
            }
        }
    } else {
        PanelCard(modifier = modifier) {
            Column {
                SectionHeader(label, channel = ch)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    DataText(value.orEmpty(), style = Pulse.dataType.dataMedium, color = ch)
                    if (unit != null) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            unit,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * A minimal sparkline / bar series.
 *
 * - [asBars] true: each value is a vertical bar scaled to the series **max** from a zero baseline —
 *   right for magnitudes like a Mon–Sun weekly readout.
 * - [asBars] false: the values are joined as a line. By default it's **min–max normalized** so the
 *   shape fills the height (right for tightly-clustered series like bodyweight, where a zero
 *   baseline would render flat); the latest point gets an emphasized dot. Pass [normalizeMinMax] =
 *   false to keep a zero baseline.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier.fillMaxWidth().height(48.dp),
    channel: Color = Pulse.accent.base,
    asBars: Boolean = true,
    normalizeMinMax: Boolean = true,
    // Filled line-chart mode (pass a non-null [strokeWidth]): an inset polyline with an optional
    // area fill and a double-dot emphasis on the last point — Spotter's stat-tile/record sparkline,
    // distinct from the plain library line below. Left null, callers get the plain line.
    strokeWidth: Dp? = null,
    fill: Boolean = true,
    emphasizeLast: Boolean = true,
) {
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val seriesMin = values.minOrNull() ?: 0f
    val seriesMax = values.maxOrNull() ?: 0f
    val range = (seriesMax - seriesMin).takeIf { it > 0f }
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        if (asBars) {
            val slot = w / values.size
            val barW = slot * 0.55f
            values.forEachIndexed { i, v ->
                val barH = (v / max) * h
                val left = i * slot + (slot - barW) / 2f
                drawRoundRect(
                    color = channel,
                    topLeft = Offset(left, h - barH),
                    size = androidx.compose.ui.geometry.Size(barW, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f, barW / 2f),
                )
            }
        } else if (strokeWidth != null) {
            // Filled line mode (Spotter): inset polyline + optional area fill + double-dot emphasis.
            if (values.size < 2) return@Canvas
            val strokePx = strokeWidth.toPx()
            val r = range ?: 1f
            val dotRadius = strokePx * 1.6f
            val stepX = (w - dotRadius * 2) / (values.size - 1)
            val usableH = h - dotRadius * 2
            fun pointAt(i: Int): Offset {
                val norm = (values[i] - seriesMin) / r
                return Offset(dotRadius + stepX * i, dotRadius + usableH * (1f - norm))
            }
            val linePath = Path().apply {
                moveTo(pointAt(0).x, pointAt(0).y)
                for (i in 1 until values.size) lineTo(pointAt(i).x, pointAt(i).y)
            }
            if (fill) {
                val area = Path().apply {
                    addPath(linePath)
                    lineTo(pointAt(values.size - 1).x, h)
                    lineTo(pointAt(0).x, h)
                    close()
                }
                drawPath(
                    area,
                    Brush.verticalGradient(listOf(channel.copy(alpha = 0.14f), Color.Transparent)),
                )
            }
            drawPath(
                linePath,
                color = channel,
                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            if (emphasizeLast) {
                drawCircle(channel.copy(alpha = 0.25f), radius = dotRadius * 2f, center = pointAt(values.size - 1))
                drawCircle(channel, radius = dotRadius, center = pointAt(values.size - 1))
            }
        } else {
            val pad = h * 0.12f // keep the line off the very top/bottom edges
            fun yOf(v: Float): Float = if (normalizeMinMax && range != null) {
                // Min–max: lowest value sits near the bottom, highest near the top.
                h - pad - ((v - seriesMin) / range) * (h - 2 * pad)
            } else {
                h - (v / max) * h
            }
            val step = if (values.size > 1) w / (values.size - 1) else w
            val path = Path()
            values.forEachIndexed { i, v ->
                val x = i * step
                val y = yOf(v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = channel, style = Stroke(width = 4f, cap = StrokeCap.Round))
            // Emphasize the latest point.
            val lastX = (values.size - 1) * step
            drawCircle(color = channel, radius = 5f, center = Offset(lastX, yOf(values.last())))
        }
    }
}

/** A small filled dot — used inline for channel legends. */
@Composable
fun ChannelDot(color: Color, size: androidx.compose.ui.unit.Dp = 8.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

/** Friendly empty state: icon in a tinted circle, bold title, optional subtitle. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
