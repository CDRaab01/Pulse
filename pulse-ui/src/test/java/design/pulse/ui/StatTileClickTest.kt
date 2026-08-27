package design.pulse.ui

import android.app.Application
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import design.pulse.ui.components.StatTile
import design.pulse.ui.theme.PulseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `StatTile` honours `onClick` in BOTH layouts.
 *
 * It used to honour it only when `dense = true`. The standard layout accepted the parameter and
 * dropped it on the floor — a component that lies about its own API, and the worst shape of lie:
 * the tile renders, the node is findable, the tap lands, and nothing happens. Nothing in this
 * library or any consumer could have caught it, because the two callers that existed both passed
 * `dense = true`.
 *
 * Found from Tote, whose Find tab made the first non-dense tile tappable so a count of loose items
 * could open the screen listing them.
 *
 * Both cases are asserted, not just the fixed one: the dense branch already worked, and a later
 * refactor that unified the two layouts could plausibly break it in the other direction.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    application = Application::class,
    sdk = [34],
    qualifiers = RobolectricDeviceQualifiers.Pixel5,
)
class StatTileClickTest {

    @get:Rule val compose = createComposeRule()

    private fun render(dense: Boolean, onClick: () -> Unit) {
        compose.setContent {
            PulseTheme {
                Surface {
                    StatTile(label = "Not in a bin", value = "20", dense = dense, onClick = onClick)
                }
            }
        }
    }

    @Test
    fun `a standard tile reports its tap`() {
        var tapped = false
        render(dense = false, onClick = { tapped = true })

        compose.onNodeWithText("20").performClick()

        assertTrue("a standard StatTile given an onClick must actually call it", tapped)
    }

    @Test
    fun `a dense tile still reports its tap`() {
        var tapped = false
        render(dense = true, onClick = { tapped = true })

        compose.onNodeWithText("20").performClick()

        assertTrue(tapped)
    }
}
