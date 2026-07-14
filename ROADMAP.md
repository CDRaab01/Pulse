# ROADMAP.md — Pulse (created 2026-07-13 for the suite's 1.0 polish round)

Pulse never had a roadmap — it grew by extraction. The suite pivot changes that: **the host-level
1.0 polish program (C:\Code ROADMAP3, 2026-07-13) starts here**, because a change in this library
propagates to all four Compose consumers automatically. Tier P of that program, restated as this
repo's backlog:

## Tier P — harden the lever

1. **Promote the delight primitives into `pulse-ui`.** `ConfettiHost`/`CelebrationPulse` (living
   app-side in Spotter today) + `HeatBar`, under the existing law — **Pulse knows hues and
   structure, never meaning**: apps decide *what* to celebrate, Pulse owns *how it moves* (wire
   into `PulseMotion`). Add an onboarding scaffold (pager + page primitives) so Plate, Cookbook,
   Magpie, and the hub don't each reinvent Spotter's `ui/onboarding/`. New params additive with
   backward-compatible defaults, per the house rule.
   *Done when:* Spotter consumes the library versions with zero visual diff (its Roborazzi
   baselines), and one non-Spotter app ships a celebration moment through them.
2. **Component-level screenshot tests in this repo.** Today `:pulse-ui:assemble` "proves nearly
   nothing" (ARCHITECTURE.md) and correctness rides on consumers' under-recorded baselines. Add
   Roborazzi at the component level here — per component, both schemes, a couple of accents.
   *Done when:* a deliberate 1-px padding change on `StatTile` fails Pulse CI before any
   consumer sees it.
3. **Drive the consumer baseline debt to zero** (coordinated with each app's polish round):
   Cookbook non-Home screens; the hub app (record or delete its job — no permanently-skipped
   checks under the 1.0 bar).
4. ~~Toolchain doc drift~~ — **✓ fixed 2026-07-13**: CLAUDE.md here (and Dragonfly's, and the
   host map) said AGP 8.5.0 / Kotlin 2.0.0 / BOM 2024.06.00; `gradle/libs.versions.toml` is the
   source of truth (AGP 9.1.1 / Kotlin 2.2.10 / BOM 2026.06.01). Lesson: docs must point at the
   file, not restate its values — restated numbers rot.

## Explicitly not worth it

- A real versioning/publishing story (Maven, version pins) — consumers deliberately float on
  `main` via composite build; per-repo blast radius is handled by the consumer-build CI gate.
  Revisit only if a polish rollout actually needs a consumer pinned to older Pulse.
- Meaning-bearing components (a "WorkoutCard", a "BudgetRow") — semantics stay app-side, always.
- Hawksnest convergence — the CSS port stays a hand-synced port; a shared token pipeline is not
  worth the tooling for one web consumer.
