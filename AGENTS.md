# AGENTS.md — using Pulse from a consumer app

You are (probably) a coding agent working in **Cookbook, Dragonfly, Plate, or Spotter** — an app that
consumes the Pulse design system via a Gradle composite build. This file tells you how to reuse Pulse's
components correctly instead of regenerating UI or guessing at the API.

The machine-readable source of truth is **[`pulse-index.json`](pulse-index.json)** (generated from the
Kotlin source — it cannot lie about the API). Read it, don't re-derive it from the `.kt` files.

## The one rule

**Prefer an existing Pulse component over generating new UI from scratch.** If the index has something
that fits, use it. If it *almost* fits, extend it in Pulse with a new **defaulted** parameter (so every
existing caller stays pixel-identical) — do not fork or copy it into the app. In-tree copies are the
exact drift Pulse exists to kill.

## How to retrieve

1. **Load `pulse-index.json` once.** It's small. Top-level: `pulseVersion`, `accents`
   (`Blue/Amber/Green/Violet/Teal`), and `components[]`.
2. **Filter by facets** in code — don't paste the whole file into a model. Useful facets per component:
   - `role` — `surface`, `data-readout`, `metric-display`, `chart`, `header`, `label`, `action`,
     `progress`, `indicator`, `empty-state`, `interaction-modifier`.
   - `perfTier` — `cheap` (static), `moderate` (self-animating), `conditional` (animates only when
     certain params are set). **Prefer `cheap` unless the task explicitly needs motion.**
   - `params[]` — each has `name`, `type`, `hasDefault`, `required`, and sometimes `since`. Required
     params are the minimal call; everything else is optional and defaulted.
3. **Read `agentMeta`** on the chosen component before adapting it: `extend` (how to add to it safely),
   `neverDo` (hard constraints), `meaningVia` (how to bind it to a data domain).
4. **Only then** open the component's `file` if you need the implementation detail.

## Honor these when you call or extend a component

- **Colors come from the accent channel**, not literals. Each app has a lead accent (Cookbook Amber,
  Dragonfly Violet, Plate Green, Spotter Blue). Pass a `channel: Color` to bind a surface to a specific
  data domain; the hue→meaning mapping lives in **your app's** CompositionLocal over `PulseTheme`, never
  in Pulse.
- **Depth is stroke + tone, never shadow.** Don't add elevation.
- **Pulse knows hues and structure, never meaning.** Never push app-domain names (protein, streak, heat,
  calories…) into Pulse.
- **`perfTier: conditional`** (e.g. `StatTile`) means static until you opt into animation — pass
  `animatedValue`/`sparkline` only when a live metric genuinely warrants it.
- A few components are commonly misread — the index encodes the truth in `agentMeta.neverDo`, e.g.
  `Sparkline` is a **pure Canvas draw that does not self-animate**; motion comes from you changing its
  `values`.

## If you change Pulse itself

Regenerate the index in the same change: `java tools/PulseIndex.java generate` and commit
`pulse-index.json`. A new public component also needs a `pulse-meta.json` entry (role/perfTier/since) or
CI's `index-drift` job fails. Details: [ARCHITECTURE.md](ARCHITECTURE.md) → *Machine-readable API surface*.
