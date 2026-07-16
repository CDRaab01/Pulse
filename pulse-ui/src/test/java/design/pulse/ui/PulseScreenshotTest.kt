package design.pulse.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import design.pulse.ui.components.Caption
import design.pulse.ui.components.EmptyState
import design.pulse.ui.components.ErrorState
import design.pulse.ui.components.HeatCalendar
import design.pulse.ui.components.HeroPanel
import design.pulse.ui.components.PanelCard
import design.pulse.ui.components.PulseBarChart
import design.pulse.ui.components.PulseButton
import design.pulse.ui.components.PulseLineChart
import design.pulse.ui.components.PulsePageIndicator
import design.pulse.ui.components.ProfileHeader
import design.pulse.ui.components.PulseSegmentedControl
import design.pulse.ui.components.SettingsSection
import design.pulse.ui.components.PulseSelectableCard
import design.pulse.ui.components.SectionHeader
import design.pulse.ui.components.StatTile
import design.pulse.ui.theme.Pulse
import design.pulse.ui.theme.PulseAccent
import design.pulse.ui.theme.PulseTheme
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Component-level screenshot tests (Robolectric native graphics + Roborazzi) — render the Pulse kit to
 * PNGs with no device, so a shared-component change is caught here in Pulse CI instead of surfacing as
 * a diff in four consumers' baselines later. This is the safety net that "harden the lever" (ROADMAP
 * Tier P #2) is about.
 *
 * Run: `./gradlew :pulse-ui:testDebugUnitTest`. Record: add `-Proborazzi.test.record=true`; verify
 * (CI): `-Proborazzi.test.verify=true`. Baselines land in `pulse-ui/screenshots/`. Finite draw-in
 * animations settle to their final frame before capture (the test clock advances to idle); the
 * infinite-transition celebration components (ConfettiHost/CelebrationPulse) are intentionally not
 * captured — they never idle.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel5)
class PulseScreenshotTest {

    @get:Rule val compose = createComposeRule()

    private val options = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.03f),
    )

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String, dark: Boolean, accent: PulseAccent, content: @Composable () -> Unit) {
        compose.setContent {
            PulseTheme(darkTheme = dark, accent = accent) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) { content() }
                }
            }
        }
        compose.onRoot().captureRoboImage("screenshots/$name.png", roborazziOptions = options)
    }

    // ---- scenes ------------------------------------------------------------------------

    @Composable
    private fun SurfacesScene() {
        HeroPanel {
            Text("Good evening", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Three sessions this week", color = Color.White.copy(alpha = 0.85f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(label = "Volume", value = "12,340", unit = "kg", modifier = Modifier.weight(1f))
            StatTile(label = "Streak", value = "6", unit = "days", modifier = Modifier.weight(1f))
        }
        PanelCard {
            SectionHeader("This week")
            Text("A flat, hairline-stroked surface — depth is tone and stroke, never shadow.")
        }
        PulseButton(text = "Start workout", onClick = {}, modifier = Modifier.fillMaxWidth())
    }

    @Composable
    private fun DataVizScene() {
        Caption("Consistency")
        HeatCalendar(intensities = heatSample())
        Caption("This week")
        PulseBarChart(
            values = listOf(3f, 5f, 2f, 6f, 4f, 7f, 5f),
            labels = listOf("M", "T", "W", "T", "F", "S", "S"),
        )
        Caption("Bodyweight trend")
        PulseLineChart(values = listOf(82.1f, 81.8f, 81.9f, 81.4f, 81.2f, 80.9f, 80.7f, 80.8f, 80.4f))
    }

    @Composable
    private fun ControlsScene() {
        PulseSegmentedControl(
            options = listOf("System", "Light", "Dark"),
            selectedIndex = 2,
            onSelect = {},
        )
        PulseSelectableCard(label = "Build muscle", selected = true, onClick = {})
        PulseSelectableCard(label = "Lose fat", selected = false, onClick = {})
        PulsePageIndicator(count = 4, current = 1, modifier = Modifier.padding(8.dp))
        EmptyState(icon = plusGlyph, title = "Nothing here yet", subtitle = "Log your first entry to get started.")
    }

    @Composable
    private fun StatesScene() {
        // Each full-surface state fills its own bounded box so both render (they're fillMaxSize inside).
        Box(Modifier.fillMaxWidth().height(240.dp)) {
            EmptyState(icon = plusGlyph, title = "Nothing here yet", subtitle = "Log your first entry to get started.")
        }
        Box(Modifier.fillMaxWidth().height(280.dp)) {
            ErrorState(
                icon = plusGlyph,
                title = "Couldn't load",
                detail = "Check your connection and try again.",
                onRetry = {},
            )
        }
    }

    @Composable
    private fun SettingsScene() {
        ProfileHeader(name = "Chris Raab", email = "chris@dragonflymedia.org")
        SettingsSection("Appearance") {
            PulseSegmentedControl(
                options = listOf("System", "Light", "Dark"),
                selectedIndex = 0,
                onSelect = {},
            )
        }
        SettingsSection("Account") {
            Text("chris@dragonflymedia.org", style = MaterialTheme.typography.bodyMedium)
        }
    }

    private fun heatSample(): List<Float> = List(35) { i -> ((i * 7 + 3) % 5).toFloat() }

    // A dependency-free sample glyph (a plus) so the test needn't pull in material-icons; EmptyState
    // applies its own tint, so only the shape matters.
    private val plusGlyph: ImageVector = ImageVector.Builder(
        name = "sample_plus", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(10f, 4f); lineTo(14f, 4f); lineTo(14f, 10f); lineTo(20f, 10f); lineTo(20f, 14f)
            lineTo(14f, 14f); lineTo(14f, 20f); lineTo(10f, 20f); lineTo(10f, 14f); lineTo(4f, 14f)
            lineTo(4f, 10f); lineTo(10f, 10f); close()
        }
    }.build()

    // ---- captures ----------------------------------------------------------------------

    @Test fun surfaces_dark() = capture("surfaces_dark", true, PulseAccent.Blue) { SurfacesScene() }
    @Test fun surfaces_light() = capture("surfaces_light", false, PulseAccent.Blue) { SurfacesScene() }

    @Test fun dataviz_dark() = capture("dataviz_dark", true, PulseAccent.Blue) { DataVizScene() }
    @Test fun dataviz_light() = capture("dataviz_light", false, PulseAccent.Blue) { DataVizScene() }
    @Test fun dataviz_green() = capture("dataviz_green", true, PulseAccent.Green) { DataVizScene() }

    @Test fun controls_dark() = capture("controls_dark", true, PulseAccent.Blue) { ControlsScene() }
    @Test fun controls_light() = capture("controls_light", false, PulseAccent.Blue) { ControlsScene() }

    @Test fun settings_dark() = capture("settings_dark", true, PulseAccent.Green) { SettingsScene() }
    @Test fun settings_light() = capture("settings_light", false, PulseAccent.Green) { SettingsScene() }

    @Test fun states_dark() = capture("states_dark", true, PulseAccent.Blue) { StatesScene() }
    @Test fun states_light() = capture("states_light", false, PulseAccent.Blue) { StatesScene() }
}
