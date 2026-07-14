package design.pulse.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import design.pulse.ui.theme.Pulse

/**
 * Pull-to-refresh, skinned to Pulse — the stock Material spinner is the one visibly non-Pulse element
 * in every scroll view, so wrap it once here: the indicator's arrow/arc takes the [channel] color and
 * the puck takes the panel tone, matching the flat, channel-colored language everything else uses.
 *
 * A thin wrapper over Material3's [PullToRefreshBox]; content runs in a [BoxScope]. Channel defaults to
 * the app accent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    channel: Color = Pulse.accent.base,
    state: PullToRefreshState = rememberPullToRefreshState(),
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Pulse.structure.panelHigh,
                color = channel,
            )
        },
        content = content,
    )
}
