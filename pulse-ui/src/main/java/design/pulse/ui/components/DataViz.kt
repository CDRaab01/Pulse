package design.pulse.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import design.pulse.ui.theme.Pulse
import design.pulse.ui.theme.PulseMotion

/**
 * The Pulse data-viz layer — the charts that make an app read as a serious tool rather than a logger.
 * Everything here draws in one visual language: channel-colored, flat, the mono data type for labels,
 * and a single draw-in entrance on the DATA motion spec (~600ms) so a value feels *measured*, not
 * printed. Pulse owns the *how*; the app owns the *what* (which series means what — pass a channel).
 *
 * For a tiny inline series use [Sparkline]; these are the full, labelled versions.
 */

/**
 * A contribution-graph heat calendar: [intensities] is one value per day in chronological order,
 * laid out in columns of 7 (a week per column). Cell alpha scales with intensity over [max] (series
 * max when null); zero/empty days render as a faint hairline cell. The single most "serious tool"
 * chart in the kit — training consistency, logging streaks, spend-by-day.
 */
@Composable
fun HeatCalendar(
    intensities: List<Float>,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    cellSize: Dp = 12.dp,
    cellGap: Dp = 3.dp,
    max: Float? = null,
) {
    val ceiling = (max ?: intensities.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val emptyCell = Pulse.structure.hairlineStrong
    val columns = if (intensities.isEmpty()) 0 else (intensities.size + 6) / 7
    Canvas(
        modifier = modifier
            .width(cellSize * columns + cellGap * (columns - 1).coerceAtLeast(0))
            .height(cellSize * 7 + cellGap * 6),
    ) {
        val cell = cellSize.toPx()
        val gap = cellGap.toPx()
        val radius = CornerRadius(cell * 0.25f, cell * 0.25f)
        intensities.forEachIndexed { i, v ->
            val col = i / 7
            val row = i % 7
            val x = col * (cell + gap)
            val y = row * (cell + gap)
            val t = (v / ceiling).coerceIn(0f, 1f)
            val color = if (v <= 0f) emptyCell else lerp(channel.copy(alpha = 0.18f), channel, t)
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(cell, cell),
                cornerRadius = radius,
            )
        }
    }
}

/**
 * A vertical bar chart with x-axis labels — magnitudes over a small set of periods (a training week, a
 * spend-by-category row). Bars grow in from the baseline on the DATA motion spec the first time it
 * composes. Channel-colored; the last bar is emphasized so "now" reads at a glance.
 */
@Composable
fun PulseBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    barHeight: Dp = 120.dp,
    emphasizeLast: Boolean = true,
) {
    val grow = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { grow.animateTo(1f, animationSpec = PulseMotion.data()) }
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val faint = channel.copy(alpha = 0.5f)
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
        ) {
            if (values.isEmpty()) return@Canvas
            val slot = size.width / values.size
            val barW = slot * 0.55f
            values.forEachIndexed { i, v ->
                val full = (v / max) * size.height
                val barH = full * grow.value
                val left = i * slot + (slot - barW) / 2f
                val color = if (emphasizeLast && i == values.lastIndex) channel else faint
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, size.height - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(barW / 3f, barW / 3f),
                )
            }
        }
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Pulse.spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * A line chart with an area fill and a draw-in reveal (the line wipes in left→right on the DATA spec).
 * Min–max normalized by default so a tightly-clustered series (bodyweight, a balance trend) fills the
 * height; pass [normalizeMinMax] = false for a zero baseline. The latest point is dotted.
 */
@Composable
fun PulseLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    lineHeight: Dp = 140.dp,
    normalizeMinMax: Boolean = true,
    fill: Boolean = true,
) {
    val reveal = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { reveal.animateTo(1f, animationSpec = PulseMotion.data()) }
    val seriesMin = values.minOrNull() ?: 0f
    val seriesMax = values.maxOrNull() ?: 0f
    val range = (seriesMax - seriesMin).takeIf { it > 0f }
    val max = seriesMax.coerceAtLeast(1f)
    Canvas(modifier = modifier.fillMaxWidth().height(lineHeight)) {
        if (values.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val pad = h * 0.1f
        fun yOf(v: Float): Float = if (normalizeMinMax && range != null) {
            h - pad - ((v - seriesMin) / range) * (h - 2 * pad)
        } else {
            h - (v / max) * (h - pad) - pad * 0.5f
        }
        val stepX = w / (values.size - 1)
        // How many whole segments to draw for the current reveal, plus a partial segment.
        val progress = reveal.value * (values.size - 1)
        val whole = progress.toInt().coerceIn(0, values.size - 1)
        val frac = progress - whole
        val linePath = Path().apply {
            moveTo(0f, yOf(values[0]))
            for (i in 1..whole) lineTo(i * stepX, yOf(values[i]))
            if (whole < values.size - 1 && frac > 0f) {
                val x0 = whole * stepX; val y0 = yOf(values[whole])
                val x1 = (whole + 1) * stepX; val y1 = yOf(values[whole + 1])
                lineTo(x0 + (x1 - x0) * frac, y0 + (y1 - y0) * frac)
            }
        }
        if (fill) {
            val lastX = (whole * stepX + (if (whole < values.size - 1) stepX * frac else 0f))
            val area = Path().apply {
                addPath(linePath)
                lineTo(lastX, h)
                lineTo(0f, h)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(channel.copy(alpha = 0.16f), Color.Transparent)))
        }
        drawPath(
            linePath,
            color = channel,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        if (reveal.value >= 1f) {
            val lx = (values.size - 1) * stepX
            drawCircle(channel.copy(alpha = 0.25f), radius = 10f, center = Offset(lx, yOf(values.last())))
            drawCircle(channel, radius = 5f, center = Offset(lx, yOf(values.last())))
        }
    }
}
