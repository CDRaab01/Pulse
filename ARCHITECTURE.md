# ARCHITECTURE.md — Pulse (software-level)

How this repo is organized and the responsibilities that come with it. Suite-level context:
`C:\Code\ARCHITECTURE.md`. Editing rules + consumer table: [CLAUDE.md](CLAUDE.md). Philosophy +
contents: [README.md](README.md).

Pulse is the smallest repo in the suite and the one with the widest blast radius: one Android
library module (`pulse-ui/` → `design.pulse:pulse-ui`), no app, **no tests of its own** — and
four apps (Spotter, Plate, Cookbook, Dragonfly) consume it live via Gradle composite builds.
It is never deployed; it ships inside its consumers' releases.

## Consumption model (why every change here is a four-app change)

Each consumer's `settings.gradle.kts` does `includeBuild("../../Pulse")` against the **sibling
working tree** — there is no published artifact and no version gate in practice (the `0.1.0` on
the dependency line is cosmetic). Consumer CI/release workflows check out this repo as a sibling.
Consequences:

- A change on Pulse `main` is picked up by the next CI/release run of every consumer,
  automatically.
- **Validation lives in the consumers, not here.** `pulse-ui` has no unit tests; correctness is
  pinned by the consumers' compiles and their Roborazzi screenshot baselines. Before pushing
  anything, build at least one consumer app (`assembleDebug` in Cookbook is the convention) —
  `:pulse-ui:assemble` alone proves nearly nothing (it skips `checkDebugAarMetadata`; a
  dependency bump that passed here once broke all four consumers' `main` at once — see host
  ROADMAP.md T2 #2).
- **Toolchain lockstep**: `gradle/libs.versions.toml` (currently AGP 9.1.1 / Kotlin 2.2.10 /
  Compose BOM 2026.06.01) is the suite's reference; consumers must match it exactly or the
  composite build breaks binary compatibility. AGP/Kotlin/Compose/KSP bumps are a coordinated
  Pulse-first, all-consumers change — Dependabot deliberately name-pins them (and Pulse's gradle
  updates are patch-only).
- Hawksnest consumes a **CSS port** of the tokens (`Hawksnest/src/theme/tokens.css`), not this
  library — token *value* changes here don't propagate there automatically; sync by hand.

## Module inventory (`pulse-ui/src/main/java/design/pulse/ui/`)

### `theme/` — the tokens

| File | Owns |
|---|---|
| `PulseTheme.kt` | The entry point: `PulseTheme(darkTheme, accent: PulseAccent)` — each app picks its lead accent (Spotter Blue, Plate Green, Cookbook Amber, Dragonfly Violet) and layers its own domain-channel CompositionLocal on top |
| `Palette.kt`, `Schemes.kt` | The shared hue families + M3 color schemes (dark-first OLED; contrast-safe light variants) |
| `Type.kt`, `DataType.kt` | UI type scale (Space Grotesk / Inter) + the mono data scale (JetBrains Mono for every numeral). **Fonts are static per-weight instances — never variable fonts** (real-device rendering bug) |
| `Motion.kt`, `Shape.kt`, `Dimens.kt`, `Structure.kt` | Motion tokens (Fast/Standard/Emphasized/Data + easings), 8/12/16dp shapes, spacing, panel/hairline structural tokens (depth = stroke + tone, not shadows) |

### `components/` — the kit

`PanelCard`, `PulseButton`, `StatTile` (incl. `dense` metric mode), `SectionHeader` (trailing
slot), `DataText`/`TickerNumber`, `ProgressRing`, `Sparkline` (line + filled modes), `Caption`,
`ChannelDot`, `EmptyState`, `Modifier.pressScale`. These are the **Spotter-superset** versions
(reconciled 2026-07-03): richer parameters with defaults chosen so leaner callers render
pixel-identically.

The dividing line: **generic tokens/components live here; channel *semantics* (which hue means
what) and app-specific components (Spotter's `HeatBar`/`ConfettiHost`, Plate's hero gradient,
etc.) stay app-side** in each `ui/theme/<App>Theme.kt`. If a component exists in two apps,
promote it here as a superset; never let in-tree copies reappear (that drift is what this repo
was created to kill).

## How to change Pulse safely

1. Make the change; keep parameter additions defaulted so existing call sites compile and render
   identically.
2. Build a consumer against it: `cd ../Cookbook/android && ./gradlew assembleDebug` (composite
   build picks up your working tree automatically).
3. If visuals moved intentionally, re-record the affected consumers' Roborazzi baselines in the
   same coordinated change window; if they moved unintentionally, that's your regression signal.
4. Push Pulse first, then any consumer changes — consumer CI checks out Pulse `main`.
