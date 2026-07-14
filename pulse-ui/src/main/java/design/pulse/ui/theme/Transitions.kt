package design.pulse.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * The suite's screen-transition vocabulary — a theme-level token set like [PulseMotion], so navigation
 * *moves* with one hand instead of every app using the platform default (or none). Wire these into a
 * NavHost's enter/exit/popEnter/popExit lambdas (or any AnimatedContent).
 *
 *  - **Shared-axis X** ([forwardEnter]/[forwardExit] and their pop counterparts): the primary
 *    forward/back within a stack — the incoming screen slides from the trailing edge, the outgoing one
 *    yields inward, both cross-fading. Directional, so depth reads correctly.
 *  - **Fade-through** ([fadeThroughEnter]/[fadeThroughExit]): peers with no spatial relationship —
 *    switching bottom-nav tabs — a fade with a slight scale, no slide.
 *
 * All timed on [PulseMotion] so transitions share the app's motion identity.
 */
object PulseTransitions {

    private const val SLIDE_FRACTION = 0.18f

    /** Forward navigation (push): new screen enters from the trailing edge. */
    fun forwardEnter(): EnterTransition =
        slideInHorizontally(PulseMotion.emphasized()) { full -> (full * SLIDE_FRACTION).toInt() } +
            fadeIn(PulseMotion.standard())

    /** Forward navigation (push): current screen yields inward. */
    fun forwardExit(): ExitTransition =
        slideOutHorizontally(PulseMotion.emphasized()) { full -> -(full * SLIDE_FRACTION).toInt() } +
            fadeOut(PulseMotion.standard())

    /** Back navigation (pop): previous screen returns from the leading edge. */
    fun backEnter(): EnterTransition =
        slideInHorizontally(PulseMotion.emphasized()) { full -> -(full * SLIDE_FRACTION).toInt() } +
            fadeIn(PulseMotion.standard())

    /** Back navigation (pop): current screen leaves toward the trailing edge. */
    fun backExit(): ExitTransition =
        slideOutHorizontally(PulseMotion.emphasized()) { full -> (full * SLIDE_FRACTION).toInt() } +
            fadeOut(PulseMotion.standard())

    /** Fade-through for peers (tab switches): fade + a slight scale up, no slide. */
    fun fadeThroughEnter(): EnterTransition =
        fadeIn(PulseMotion.standard()) + scaleIn(PulseMotion.standard(), initialScale = 0.96f)

    /** Fade-through for peers (tab switches): fade + a slight scale down. */
    fun fadeThroughExit(): ExitTransition =
        fadeOut(PulseMotion.fast()) + scaleOut(PulseMotion.standard(), targetScale = 0.96f)
}
