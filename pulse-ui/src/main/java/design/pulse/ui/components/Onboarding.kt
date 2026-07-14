package design.pulse.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import design.pulse.ui.theme.Pulse
import design.pulse.ui.theme.PulseMotion
import kotlinx.coroutines.launch

/**
 * First-run scaffolding, app-agnostic. Pulse owns the *shape* of an intro flow — a swipeable pager, a
 * page-dot indicator, Skip/Next/Done affordances, and a consistent page layout — so every app's
 * onboarding reads as the same product. The *content and copy* of each page, and what "finish" does,
 * stay app-side (that's meaning, not structure).
 *
 * See [OnboardingScaffold] for the pager shell, [OnboardingPage] for a standard page, and
 * [PulseSelectableCard] for the pick-one/pick-many card used by questionnaire-style onboarding.
 */

/**
 * The page-dot indicator: a row of dots with the active one stretched into a pill (accent-tinted).
 * The active pill animates its width on page change, so it's cheap-but-alive.
 */
@Composable
fun PulsePageIndicator(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Pulse.accent.base,
    inactiveColor: Color = Pulse.structure.hairlineStrong,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val active = i == current
            val width by animateDpAsState(
                targetValue = if (active) 20.dp else 6.dp,
                animationSpec = PulseMotion.standard(),
                label = "pageDotWidth",
            )
            Box(
                Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (active) activeColor else inactiveColor),
            )
        }
    }
}

/**
 * A standard onboarding page: an optional [illustration] slot (glyph, image, [CelebrationPulse]…),
 * a bold [title], and a [body] line — centered, generously padded. Keeping the layout here is what
 * makes five apps' intro pages feel cut from one cloth.
 */
@Composable
fun OnboardingPage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Pulse.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (illustration != null) {
            illustration()
            Spacer(Modifier.height(Pulse.spacing.xl))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Pulse.spacing.md))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * The intro-pager shell: [pageCount] swipeable pages with a [PulsePageIndicator], a "Skip" text
 * action, and a primary [PulseButton] that advances ("Next") until the last page, where it reads
 * "Done" and calls [onFinish]. Swiping and the button stay in sync via a shared [pagerState].
 *
 * The app supplies each page via [page] (typically an [OnboardingPage]) and decides what finishing
 * means (persist the "seen" flag, navigate home…) in [onFinish]. [onSkip] defaults to [onFinish].
 */
@Composable
fun OnboardingScaffold(
    pageCount: Int,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    onSkip: () -> Unit = onFinish,
    nextLabel: String = "Next",
    finishLabel: String = "Done",
    skipLabel: String = "Skip",
    pagerState: PagerState = rememberPagerState(pageCount = { pageCount }),
    page: @Composable (index: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val onLastPage = pagerState.currentPage >= pageCount - 1
    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { index ->
            page(index)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Pulse.spacing.xl, vertical = Pulse.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Skip stays present but hides itself on the last page (nothing left to skip).
            if (!onLastPage) {
                TextButton(onClick = onSkip) { Text(skipLabel) }
            }
            PulsePageIndicator(
                count = pageCount,
                current = pagerState.currentPage,
                modifier = Modifier.weight(1f),
            )
            PulseButton(
                text = if (onLastPage) finishLabel else nextLabel,
                onClick = {
                    if (onLastPage) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
            )
        }
    }
}

/**
 * A selectable card — the pick-one / pick-many primitive for questionnaire-style onboarding, and any
 * settings/picker surface. Selected state fills with the channel's [PulseChannel.dim] tone and draws
 * a channel-tinted border + trailing check; unselected is a plain hairline panel. Channel defaults to
 * the app accent; pass a data-domain channel to bind it to a meaning.
 *
 * [trailing] overrides the default check affordance (e.g. a radio glyph) when set.
 */
@Composable
fun PulseSelectableCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    channelDim: Color = Pulse.accent.dim,
    trailing: (@Composable (selected: Boolean) -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().pressScale(interaction),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) channelDim else Pulse.structure.panel,
        border = BorderStroke(1.dp, if (selected) channel.copy(alpha = 0.45f) else Pulse.structure.hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        interactionSource = interaction,
    ) {
        Row(
            modifier = Modifier.padding(Pulse.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (trailing != null) {
                trailing(selected)
            } else if (selected) {
                Spacer(Modifier.width(Pulse.spacing.sm))
                Box(
                    Modifier.height(8.dp).width(8.dp).clip(CircleShape).background(channel),
                )
            }
        }
    }
}
