# CLAUDE.md — Pulse

The PULSE design system as an Android library (`pulse-ui/` → `design.pulse:pulse-ui`). One
module, no app. The [README.md](README.md) explains the philosophy and contents; this file is
about the responsibilities that come with editing a library four apps depend on.

## Who depends on this repo, and how

| Consumer | How | Lead accent |
|---|---|---|
| Cookbook | composite build `includeBuild("../../Pulse")` | Amber |
| Dragonfly (hub) | composite build | Violet |
| Spotter | **in-tree copy** of PULSE (`android/app/.../ui/theme/`) — migration to this lib is a planned, per-app task | Blue |
| Plate | composite build `includeBuild("../../Pulse")` (migrated 2026-07-03) | **Green** |
| Hawksnest | CSS port only (`src/theme/tokens.css`) — web can't consume a Compose lib | (dark-only web) |

Consequences:

- **A change here ships with the next Cookbook/Dragonfly/Plate release automatically** — their
  `release.yml` jobs check out this repo as a sibling for the composite build. There is no Pulse
  version gate in practice (the dependency line says `0.1.0` but the composite build always uses
  the sibling working tree/branch that CI checks out). Breaking API changes here break those
  apps' CI immediately; build all three consumers before pushing.
- **Spotter does NOT get changes** until its in-tree copy is migrated. If you fix a token or
  component here, the fix is invisible in Spotter — either port it manually or do the migration.
  Watch for drift in the meantime. (Plate was migrated 2026-07-03; only Spotter remains in-tree.)
- **Version alignment is load-bearing:** consumers' AGP/Kotlin/Compose-BOM must match
  `gradle/libs.versions.toml` here (currently AGP 8.5.0 / Kotlin 2.0.0 / BOM 2024.06.00).
  Composite builds are only binary-compatible on matching versions. Bumping any of these is a
  suite-wide, all-repos-in-one-sitting change.

## Rules of the library

- **Pulse knows hues and structure, never meaning.** Channel *semantics* (what blue/green/amber
  signify) belong in each app's own CompositionLocal layered over `PulseTheme`. Do not add
  app-domain names (protein, streak, heat…) to this repo.
- **Static per-weight font instances only** — never variable fonts (some devices render the
  lightest master for everything). The fonts are Space Grotesk / Inter / JetBrains Mono
  (slashed-zero monospace numerals are part of the identity).
- Accent leads are claimed: blue = Spotter, green = Plate, amber = Cookbook, violet = Dragonfly.
  A new app picks an unclaimed accent and registers it here.
- Publishing to a real Maven repo can replace the composite build later without consumers
  changing their dependency coordinates — that's the intended evolution if the sibling-checkout
  requirement becomes painful.

## Verify a change

```powershell
./gradlew :pulse-ui:assembleRelease          # here
cd ../Cookbook/android; ./gradlew :app:assembleDebug   # cheapest consumer check
cd ../../Dragonfly/android; ./gradlew :app:assembleDebug
```
