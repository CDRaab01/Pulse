# Pulse

The PULSE design system for the app family (Spotter · Plate · Cookbook · Dragonfly · Hawksnest),
packaged as an Android library: **`design.pulse:pulse-ui`**. (Hawksnest's web app uses a CSS port
of the tokens; the others consume this library or an in-tree copy — see CLAUDE.md.)

PULSE is a data-forward, instrument-panel design language — flat hairline-stroked panels, channel
colors that carry meaning, monospace numerals (slashed zeros), uppercase captions, one motion
vocabulary. It was built in Spotter (Sprint 5), copy-pasted into Plate, and extracted here when
Cookbook became the third copy.

## What's in the library

- **`design.pulse.ui.theme`** — the reference palette, Material 3 schemes, `PulseTheme` root,
  structural tokens (`PulseStructure`), generic channel triples (`PulseChannel`), type system
  (Space Grotesk / Inter / JetBrains Mono as **static per-weight font instances** — never variable
  fonts, some devices render the lightest master), data-display type scale, motion tokens, shapes,
  spacing.
- **`design.pulse.ui.components`** — the 0.1 kit: `PanelCard`, `PulseButton`, `DataText`, `Caption`,
  `SectionHeader`, `StatTile`, `Sparkline`, `ChannelDot`, `EmptyState`, `ProgressRing`,
  `Modifier.pressScale`. **v1.0.0 added** (the suite 1.0 polish program): *delight* — `ConfettiHost`,
  `CelebrationPulse`; *first-run* — `OnboardingScaffold`, `OnboardingPage`, `PulsePageIndicator`,
  `PulseSelectableCard`; *data-viz* — `HeatCalendar`, `PulseBarChart`, `PulseLineChart`; *surfaces/
  controls* — `HeroPanel`, `PulseSegmentedControl`, `PulseRefreshBox`. Theme-level v1 additions:
  `PulseHaptics`, `ThemePref` (Dark/Light/System), `PulseTransitions`.

## What stays in each app

Channel *semantics*. The library only knows hues and structure; each app decides what the hues
mean and provides its own domain-named CompositionLocal on top of `PulseTheme`:

| App       | Lead accent | Channel meanings                                  |
|-----------|-------------|---------------------------------------------------|
| Spotter   | Blue        | effort=blue, strength=violet, streak=orange, recovery=green |
| Plate     | Blue        | carbs=blue, calories=violet, fat=orange, protein=green |
| Cookbook  | **Amber**   | heat=amber/orange (hero), fresh=green (done), info=blue, plum=violet |
| Dragonfly | **Violet**  | hub=violet (identity/primary), info=blue (versions), ok=green (up-to-date), warn=amber (update available) |

## Consuming

Composite build from a sibling checkout (all repos under one parent dir):

```kotlin
// settings.gradle.kts
includeBuild("../../Pulse")

// app/build.gradle.kts
implementation("design.pulse:pulse-ui:1.0.0")
```

Keep AGP/Kotlin/Compose versions aligned with `gradle/libs.versions.toml` here — composite builds
only stay binary-compatible when they match. Publishing to a Maven repo can replace the composite
build later without consumers changing their dependency line.

## Status

- Extracted from **Plate's** copy (the most recently exercised) on 2026-07-01.
- Spotter and Plate still carry their own in-tree copies; migrating them onto this library is a
  planned follow-up per app. Any drift found during extraction is noted in the git history.
