plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

// Coordinates consumers depend on ("design.pulse:pulse-ui:<version>"). Composite builds
// (includeBuild from a sibling checkout) substitute these automatically; a published Maven
// artifact can replace that later without consumers changing their dependency line.
group = "design.pulse"
version = "0.1.0"

android {
    namespace = "design.pulse.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

// Roborazzi record/verify/compare are driven by system properties (no Gradle plugin needed — the
// suite pattern): `./gradlew :pulse-ui:testDebugUnitTest -Proborazzi.test.record=true` to record,
// `-Proborazzi.test.verify=true` to gate. Baselines land in pulse-ui/screenshots/.
tasks.withType<Test>().configureEach {
    listOf("roborazzi.test.record", "roborazzi.test.verify", "roborazzi.test.compare").forEach { key ->
        (project.findProperty(key) as String?)?.let { systemProperty(key, it) }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // The celebration burst (ConfettiHost). Proven in Spotter; brought into Pulse so delight-motion
    // is standardized, not re-invented per app. `implementation`, not `api`: consumers call
    // ConfettiHost, never konfetti types directly, so konfetti stays off their compile classpath.
    implementation(libs.konfetti.compose)

    // Screenshot tests — Robolectric native graphics + Roborazzi (no device). See ScreenshotTest.
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    // Provides the ComponentActivity that createComposeRule() launches — a library module has no
    // launcher activity of its own, so Robolectric can't resolve one without this.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
