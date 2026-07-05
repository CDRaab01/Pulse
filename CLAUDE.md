# CLAUDE.md — Pulse

The PULSE design system as an Android library (`pulse-ui/` → `design.pulse:pulse-ui`). One
module, no app. The [README.md](README.md) explains the philosophy and contents; this file is
about the responsibilities that come with editing a library four apps depend on.

## Who depends on this repo, and how

| Consumer | How | Lead accent |
|---|---|---|
| Cookbook | composite build `includeBuild("../../Pulse")` | Amber |
| Dragonfly (hub) | composite build | Violet |
| Spotter | composite build `includeBuild("../../Pulse")` (migrated 2026-07-03) | Blue |
| Plate | composite build `includeBuild("../../Pulse")` (migrated 2026-07-03) | **Green** |
| Magpie | composite build (planned — repo founded 2026-07-04, Android not yet scaffolded) | **Teal** |
| Hawksnest | CSS port only (`src/theme/tokens.css`) — web can't consume a Compose lib | (dark-only web) |

Consequences:

- **A change here ships with the next Cookbook/Dragonfly/Plate release automatically** — their
  `release.yml` jobs check out this repo as a sibling for the composite build. There is no Pulse
  version gate in practice (the dependency line says `0.1.0` but the composite build always uses
  the sibling working tree/branch that CI checks out). Breaking API changes here break those
  apps' CI immediately; build all three consumers before pushing.
- **All four consumers (Cookbook/Dragonfly/Plate/Spotter) now take changes automatically** — every
  app is on the composite build. Build all four before pushing a breaking change here. (Hawksnest is
  a separate CSS port; not a Compose consumer.)
- **Components are the superset from Spotter** (the original richer PULSE), reconciled 2026-07-03:
  `StatTile` has a `dense` metric layout (icon/animatedValue/sparkline) alongside the standard tile;
  `PanelCard` takes onClick/channel/raised/contentPadding; `SectionHeader` takes a `trailing` slot;
  `Sparkline` has a filled-line mode via a non-null `strokeWidth`; `TickerNumber` is here now. New
  params are additive with backward-compatible defaults, so the leaner callers stay pixel-identical —
  keep it that way (verify all four apps' Roborazzi when touching a shared component).
- **Version alignment is load-bearing:** consumers' AGP/Kotlin/Compose-BOM must match
  `gradle/libs.versions.toml` here (currently AGP 8.5.0 / Kotlin 2.0.0 / BOM 2024.06.00).
  Composite builds are only binary-compatible on matching versions. Bumping any of these is a
  suite-wide, all-repos-in-one-sitting change.

## Rules of the library

- **Update `ARCHITECTURE.md` in the same PR** when a change alters architecture — a component's
  public API, a token's meaning, the module layout, or the consumer contract. Silently-drifting
  docs are how a consumer app's API docs said `/plans` for a round (ROADMAP2 T2 #5c).
- **Pulse knows hues and structure, never meaning.** Channel *semantics* (what blue/green/amber
  signify) belong in each app's own CompositionLocal layered over `PulseTheme`. Do not add
  app-domain names (protein, streak, heat…) to this repo.
- **Static per-weight font instances only** — never variable fonts (some devices render the
  lightest master for everything). The fonts are Space Grotesk / Inter / JetBrains Mono
  (slashed-zero monospace numerals are part of the identity).
- Accent leads are claimed: blue = Spotter, green = Plate, amber = Cookbook, violet = Dragonfly,
  teal = Magpie (added 2026-07-04, ahead of Magpie's Android scaffold — the accent is reserved
  even though no consumer exists yet). A new app picks an unclaimed accent and registers it here.
- Publishing to a real Maven repo can replace the composite build later without consumers
  changing their dependency coordinates — that's the intended evolution if the sibling-checkout
  requirement becomes painful.

## Verify a change

```powershell
./gradlew :pulse-ui:assembleRelease          # here
cd ../Cookbook/android; ./gradlew :app:assembleDebug   # cheapest consumer check
cd ../../Dragonfly/android; ./gradlew :app:assembleDebug
```
