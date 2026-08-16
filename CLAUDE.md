# CLAUDE.md — Pulse

The PULSE design system as an Android library (`pulse-ui/` → `design.pulse:pulse-ui`). One
module, no app. The [README.md](README.md) explains the philosophy and contents; this file is
about the responsibilities that come with editing a library six apps depend on.

## Who depends on this repo, and how

| Consumer | How | Lead accent |
|---|---|---|
| Cookbook | composite build `includeBuild("../../Pulse")` | Amber |
| Dragonfly (hub) | composite build | Violet |
| Spotter | composite build `includeBuild("../../Pulse")` (migrated 2026-07-03) | Blue |
| Plate | composite build `includeBuild("../../Pulse")` (migrated 2026-07-03) | **Green** |
| Magpie | composite build `includeBuild("../../Pulse")` (live since the 2026-07-05 build) | **Teal** |
| Crate | composite build `includeBuild("../../Pulse")` (scaffolded 2026-07-25) | **Copper** |
| Tote | composite build `includeBuild("../../Pulse")` (scaffolded 2026-08-15) | **Slate** |
| Hawksnest | CSS port only (`src/theme/tokens.css`) — web can't consume a Compose lib; dark + light (`:root.light`) since V1 Gate 4 | (CSS port) |

Consequences:

- **A change here ships with every consumer's next release automatically** — all six Compose
  apps' (Cookbook/Dragonfly/Spotter/Plate/Magpie/Crate) `release.yml` jobs check out this repo
  as a sibling for the composite build. There is no Pulse version gate in practice (the dependency
  line says `0.1.0` but the composite build always uses the sibling working tree/branch that CI
  checks out). Breaking API changes here break those apps' CI immediately; build all six
  consumers before pushing. Corollary: a Pulse push alone triggers **no** consumer release —
  it rides silently until each app's next `android/**` push, which is why additive-with-defaults
  is law. (Hawksnest is a separate CSS port; not a Compose consumer.)
- **Components are the superset from Spotter** (the original richer PULSE), reconciled 2026-07-03:
  `StatTile` has a `dense` metric layout (icon/animatedValue/sparkline) alongside the standard tile;
  `PanelCard` takes onClick/channel/raised/contentPadding; `SectionHeader` takes a `trailing` slot;
  `Sparkline` has a filled-line mode via a non-null `strokeWidth`; `TickerNumber` is here now. New
  params are additive with backward-compatible defaults, so the leaner callers stay pixel-identical —
  keep it that way (verify all six apps' Roborazzi when touching a shared component).
- **Version alignment is load-bearing:** consumers' AGP/Kotlin/Compose-BOM must match
  `gradle/libs.versions.toml` here (currently AGP 9.1.1 / Kotlin 2.2.10 / BOM 2026.06.01 —
  that file is the source of truth if this line ever drifts again).
  Composite builds are only binary-compatible on matching versions. Bumping any of these is a
  suite-wide, all-repos-in-one-sitting change.

## Rules of the library

- **Update `ARCHITECTURE.md` in the same PR** when a change alters architecture — a component's
  public API, a token's meaning, the module layout, or the consumer contract. Silently-drifting
  docs are how a consumer app's API docs said `/plans` for a round (ROADMAP2 T2 #5c).
- **`pulse-index.json` is a generated contract — regenerate it in the same PR** when you add,
  remove, or re-signature a public component, or add a new one. Structure is parsed from source by
  `tools/PulseIndex.java`; semantics (role/perfTier/since/agent-guidance) are hand-curated in
  `pulse-meta.json`. A new component **must** get a `pulse-meta.json` entry or CI's `index-drift`
  job fails. This is the retrieval layer consumer agents read instead of guessing at the API — see
  [AGENTS.md](AGENTS.md). Run `java tools/PulseIndex.java generate` and commit the result.
- **Pulse knows hues and structure, never meaning.** Channel *semantics* (what blue/green/amber
  signify) belong in each app's own CompositionLocal layered over `PulseTheme`. Do not add
  app-domain names (protein, streak, heat…) to this repo.
- **Static per-weight font instances only** — never variable fonts (some devices render the
  lightest master for everything). The fonts are Space Grotesk / Inter / JetBrains Mono
  (slashed-zero monospace numerals are part of the identity).
- Accent leads are claimed: blue = Spotter, green = Plate, amber = Cookbook, violet = Dragonfly,
  teal = Magpie (added 2026-07-04, ahead of Magpie's Android scaffold — the accent is reserved
  even though no consumer exists yet), rose = Remnant (reserved 2026-07-19, ahead of Remnant's
  scaffold — `PulseAccent.Rose` lands with its Phase 0), copper = Crate (added 2026-07-25 with
  Crate's Phase 0 scaffold — deliberately darker/browner than Cookbook's amber; its hero sweep
  uses deep hues with white text where Amber's is bright with ink text, so the two warm accents
  never read as each other), slate = Tote (added 2026-08-15 with Tote's Phase 0). A new app picks
  an unclaimed accent and registers it here.
- **Slate is the one accent that is a PAIR of hues, not one** — charcoal body + safety-yellow
  marking, after the black-and-yellow site tote. It is the exception to "a deep variant plus a
  bright variant of the same hue", so three things about it are deliberate and should not be
  tidied up:
  1. **The dark and light channels lead with different halves.** Dark mode's surface is already
     the charcoal (`PulseInk`/`PulsePanel` are near-black), so `base` is the yellow; on white,
     yellow cannot bear text at all, so `base` becomes `PulseSlateDeep` and the yellow drops to
     the container fill. Both halves are present in both themes — only the text-bearing one swaps.
  2. **`lightSlateChannel` is the only channel whose `base` and `dim` are different hues** (slate
     stroke/text on a pale-yellow fill). That is the object: a yellow label lettered in charcoal.
  3. **The hero gradient contains no yellow, on measured grounds.** A slate→yellow sweep passes
     through the olive `#8A8023`, where white falls to 4.04:1 *and* ink only reaches 4.81:1 — the
     one blend in this family where neither text colour is safe. The hero is therefore a
     charcoal-only sweep (white text: 14.63 → 10.67 → 7.58 across it) and the yellow is applied
     on top as a mark. Do not "complete" the gradient with a yellow stop.
  Hue alone does **not** separate Tote's yellow from Cookbook's amber (52.3° vs 37.4° — only
  14.9° apart). What separates them is structural: Cookbook's light primary is a warm
  `PulseOrangeDeep` with a bright orange→amber ink-text hero, while Tote's light primary is a cool
  `PulseSlateDeep` with a charcoal white-text hero. Keep that structural contrast if either accent
  is ever retuned — narrowing it is what would make the two apps read as each other.
- Publishing to a real Maven repo can replace the composite build later without consumers
  changing their dependency coordinates — that's the intended evolution if the sibling-checkout
  requirement becomes painful.

## Verify a change

```powershell
java tools/PulseIndex.java verify .           # component index in lockstep with source (fast, no SDK)
./gradlew :pulse-ui:assembleRelease           # here
cd ../Cookbook/android; ./gradlew :app:assembleDebug   # cheapest consumer check
cd ../../Dragonfly/android; ./gradlew :app:assembleDebug
```

If the index check fails after an intentional API change, run `java tools/PulseIndex.java generate`
and commit `pulse-index.json`.
