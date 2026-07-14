# ROADMAP.md — Pulse (created 2026-07-13 for the suite's 1.0 polish round)

Pulse never had a roadmap — it grew by extraction. The suite pivot changes that: **the host-level
1.0 polish program (C:\Code ROADMAP3, 2026-07-13) starts here**, because a change in this library
propagates to all five Compose consumers automatically. Tier P of that program, restated as this
repo's backlog:

## ✓ v1.0.0 shipped (2026-07-14) — the library content of Tier P + P2

`pulse-ui` is at **1.0.0**. What landed in the library (each item's line below is annotated):
- **Delight:** `ConfettiHost`, `CelebrationPulse` (+ konfetti dep).
- **First-run:** `OnboardingScaffold`, `OnboardingPage`, `PulsePageIndicator`, `PulseSelectableCard`.
- **Felt/appearance/motion:** `PulseHaptics`, `ThemePref` + `PulseSegmentedControl`, `PulseTransitions`.
- **Data-viz:** `HeatCalendar`, `PulseBarChart`, `PulseLineChart`.
- **Surfaces:** `HeroPanel`, `PulseRefreshBox`.
- **Safety net:** component Roborazzi screenshot tests + gating CI job (Tier P #2).
- **Splash:** shipped as a documented recipe (app-side; ARCHITECTURE.md § Splash-screen recipe).

**What "v1.0.0" does NOT include, deliberately** (these become v1.x, built as apps consume them):
- **Glance widget theming (#1 tail)** — deferred to a future **`pulse-glance` module** so the
  `glance` dep doesn't burden non-widget consumers; build with the Tier W widget family.
- **App icon family (#11)** and **empty-state glyph set (#12)** — design-art tasks, done
  deliberately rather than rushed; `EmptyState` takes any `ImageVector` meanwhile.
- **Consumer baseline debt (#3)** — belongs to each app's own polish round, not this repo.

**Adoption is the next phase**, per "the rollout recipe" below: the components are *available* on
`main`; each app now swaps its local copies for the library versions (Spotter first for the
extracted ones) in its own polish round. Version stays 1.0.0; consumers float on it.

## Tier P — harden the lever

1. **✓ Promote the delight primitives into `pulse-ui`.** (SHIPPED — Glance tail deferred, see header.) `ConfettiHost`/`CelebrationPulse` (living
   app-side in Spotter today) + `HeatBar`, under the existing law — **Pulse knows hues and
   structure, never meaning**: apps decide *what* to celebrate, Pulse owns *how it moves* (wire
   into `PulseMotion`). Add an onboarding scaffold (pager + page primitives) so Plate, Cookbook,
   Magpie, and the hub don't each reinvent Spotter's `ui/onboarding/`. New params additive with
   backward-compatible defaults, per the house rule. *Added 2026-07-14:* **Glance widget theming
   primitives** too (colors/type for RemoteViews-land, where Compose theming doesn't reach) — the
   host Tier W4 widget family only reads as one system if the tokens come from here.
   *Done when:* Spotter consumes the library versions with zero visual diff (its Roborazzi
   baselines), and one non-Spotter app ships a celebration moment through them.
2. **✓ Component-level screenshot tests in this repo.** (SHIPPED — `PulseScreenshotTest` + gating CI.) Today `:pulse-ui:assemble` "proves nearly
   nothing" (ARCHITECTURE.md) and correctness rides on consumers' under-recorded baselines. Add
   Roborazzi at the component level here — per component, both schemes, a couple of accents.
   *Done when:* a deliberate 1-px padding change on `StatTile` fails Pulse CI before any
   consumer sees it.
3. **[deferred to each app's polish round] Drive the consumer baseline debt to zero** (coordinated with each app's polish round):
   Cookbook non-Home screens; the hub app (record or delete its job — no permanently-skipped
   checks under the 1.0 bar).
4. ~~Toolchain doc drift~~ — **✓ fixed 2026-07-13**: CLAUDE.md here (and Dragonfly's, and the
   host map) said AGP 8.5.0 / Kotlin 2.0.0 / BOM 2024.06.00; `gradle/libs.versions.toml` is the
   source of truth (AGP 9.1.1 / Kotlin 2.2.10 / BOM 2026.06.01). Lesson: docs must point at the
   file, not restate its values — restated numbers rot.

## Tier P2 — the premium layer (added 2026-07-14; the "looks expensive" round)

Tier P hardens what exists; this tier adds the layers *around* components where commercial
polish actually lives. Grounded in an audit of the consumers: apps follow system dark/light but
offer **no in-app theme choice**, **no themed splash screens**, haptics used in exactly 2 files
across Spotter+Plate, and every app hand-rolls its larger charts. Ranked by wow-per-effort:

5. **✓ Semantic haptics vocabulary (`PulseHaptics`).** (SHIPPED.) Tokens — `confirm`, `tick` (rest-timer /
   rep counts), `celebrate`, `warning` — paired to `PulseMotion` so animation and vibration fire
   together (Hawksnest's semantic-haptics work is the precedent; the Compose apps barely
   vibrate). Premium feel is multi-sensory, and this is the cheapest lever left.
   *Done when:* two apps ship interactions using the same named haptic + motion pair.
6. **✓ Themed splash screens.** (SHIPPED as a recipe — ARCHITECTURE.md § Splash-screen recipe.) One Pulse recipe for the Android 12+ SplashScreen API
   (accent-tinted icon reveal on the dark field); apply per app (~an hour each — none use it
   today). First impression of every single launch. *Done when:* all five Compose apps launch
   through it.
7. **✓ Appearance scaffold (Dark/Light/System).** (SHIPPED — `ThemePref` + `PulseSegmentedControl`.) The light schemes exist in `Schemes.kt` but only
   the OS can choose. A Pulse-side preference + segmented-control pattern (Hawksnest Settings is
   the model) so all five apps gain the toggle identically — and the suite bar's "dark/light
   parity" becomes demonstrable, not latent. *Done when:* every app has Settings → Appearance
   and honors it.
8. **✓ `HeatCalendar`** (SHIPPED) (GitHub-contribution style) — the most "serious tool" chart there is:
   Spotter training consistency, Plate logging streaks, Magpie spending-by-day, one component.
   Entrance on the DATA motion spec. *Done when:* it ships in two apps from the same component.
9. **✓ Unified chart kit.** (SHIPPED — `PulseBarChart` + `PulseLineChart`, DATA-motion draw-in.) `Sparkline` + `ProgressRing` are here; the bigger
   charts (Spotter progress, Plate trends, Magpie trends) are three hand-rolled dialects. One
   bar/line-with-area kit sharing a single draw-in entrance (DATA 600 ms), the mono data face
   for axes/labels, one tooltip treatment. Charts are where "one developer" vs "a team" is most
   visible. *Done when:* the three apps' headline charts render through it with baselines.
10. **✓ Screen-transition vocabulary.** (SHIPPED — `PulseTransitions`.) `Motion.kt` owns durations/easings but nothing owns
    *navigation*; each app uses defaults. A nav-transition set (shared-axis forward/back,
    fade-through for tab switches) consumed suite-wide. *Done when:* two apps navigate with it
    and it's in the consumer contract docs.
11. **[deferred — design-art task] App icon family** — six icons, one geometry language, per-app accent on the shared dark
    field; the home-screen row is the suite's storefront (Magpie's icon is on record as
    "acceptable, not perfect"; the set was never designed together). Design task more than code;
    the launcher-icon assets live in each app repo, the geometry spec lives here.
    *Done when:* all six icons visibly share one language on a real launcher.
12. **[deferred — design-art task] Empty-state glyph set** — 8–10 geometric line-art glyphs in one stroke style (barbell,
    plate, pot, nest, magpie…) to feed `EmptyState`; kills the last bare-feeling screens.
    Glyphs are structure, not meaning — apps pick which glyph means what.
13. **✓ `HeroPanel` promotion** (SHIPPED) — Plate's gradient hero and Magpie's Tier-4 hero are near-identical
    app-side inventions; standardize the one component (gradient rules from the accent, not
    hand-picked colors).
14. **✓ Pull-to-refresh indicator skin** (SHIPPED — `PulseRefreshBox`) — the stock Material spinner is the one visibly non-Pulse
    element in every scroll view; skin it once.

Suggested order: 5–7 first (quick, touch every interaction), then 9 and 11 (the two loudest
"team built this" signals), 8/10/12–14 opportunistically as app rounds consume them.

## How consumers adopt a new Pulse capability (the rollout recipe)

There is **no upgrade step** — all five Compose apps composite-build against the sibling
checkout, so anything merged to `main` here is *available* to their next build automatically
(CI checks out Pulse `main` as a sibling; local dev uses whatever `C:\Code\Pulse` has — pull it).
But available ≠ used; every rollout is **one Pulse PR + five small app PRs**:

1. **Pulse PR:** additive-with-defaults API; regenerate `pulse-index.json` + add the
   `pulse-meta.json` entry (`index-drift` gates); the `consumer-build` job proves Cookbook
   compiles; build the other consumers locally before pushing anything breaking.
2. **App PRs (one per app, normal feature PRs):** swap the app-local implementation for the
   library one, wire any manifest/dependency bits (splash needs `core-splashscreen` + theme
   entries; the appearance toggle needs a Settings row + stored pref per app), and
   **re-record + eyeball that app's Roborazzi baselines** — adopting visuals changes pixels,
   and a baseline accepted unseen is how the wrapped-money bug shipped (Magpie Tier 4 lesson).
3. **Spotter goes first for anything extracted *from* Spotter** (celebration, onboarding):
   zero-visual-diff against its existing baselines proves the extraction before four other
   apps depend on it.
4. **Releases ride adoption:** a Pulse push cuts no consumer release; each app's adopting PR
   touches `android/**` and cuts that app's release naturally. Watch the corollary — any
   unrelated app release silently picks up whatever Pulse has merged since, so `main` here
   must always be shippable.
5. **Hawksnest separately:** token-level changes get hand-mirrored into its
   `src/theme/tokens.css`; component-level items would need a React port — keep that scope
   deliberately small (see non-goals).

## Explicitly not worth it

- A real versioning/publishing story (Maven, version pins) — consumers deliberately float on
  `main` via composite build; per-repo blast radius is handled by the consumer-build CI gate.
  Revisit only if a polish rollout actually needs a consumer pinned to older Pulse.
- Meaning-bearing components (a "WorkoutCard", a "BudgetRow") — semantics stay app-side, always.
- **Material You / dynamic color** — Pulse IS the brand; letting the wallpaper recolor it
  dissolves the identity the suite is built on (added 2026-07-14).
- **Skeleton loaders** — the suite deliberately standardized on the centered spinner (Magpie
  Tier 4 record). Revisit only as a deliberate suite-wide swap, never per-app (added 2026-07-14).
- Hawksnest convergence — the CSS port stays a hand-synced port; a shared token pipeline is not
  worth the tooling for one web consumer.
