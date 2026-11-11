# Design notes

The rule the app follows: **keep the core UI calm.** Personality belongs in the messages, the
transitions and the occasional surprise — never in extra chrome, and never in features added
just to justify the name.

## The palette

The app is black. `background` and `surface` are `#000000`, the theme is a single dark
scheme, and the window background is black too so there is no flash on launch.

The Material surfaces used on the About and History pages are **neutral greys** —
`#141414` for cards, `#242424` for the tonal ones. Material's own `surfaceContainer*` roles
carry a blue cast and read as a bluish near-black, which does not sit right here.

## Glass, but only on the home screen

The dock and the start/reset button are glass; every other page is Material 3. That split is
deliberate: the home screen is the app, the rest is paperwork.

The material is **frost**, not smoke. Frost is a milky film with a fine grain over it; smoke
is a dark wash. The grain is a 96×96 tile of white speckle (alpha 3–15) drawn as a tiled
`ImageShader` over the film, underneath the text so the labels stay clean.

There is no backdrop blur, because there is nothing to blur — every page behind the glass is
flat black. If the app ever puts artwork behind the glass, `dev.chrisbanes.haze` would give
the real thing on Android 12+.

## Type

| Where | Style |
| --- | --- |
| The greeting ("Good morning,") | `titleMedium`, 17 sp, 55% white |
| Your name | `displaySmall`, bold, white |
| The streak counter | `displayLarge` at 96 sp |
| The elapsed time | `titleMedium`, tabular figures |
| Page titles | `displaySmall`, bold |
| Card headlines | `titleLarge`, bold |
| Everything else | Material 3's own scale |

The header is the same height on every page, and every screen leaves the same gap at the top
(`screenTopGap()`), so switching tabs never shifts the layout.

## The voice

One dry sentence at the right moment, in `data/Personality.kt`:

- **Milestones** — "A whole week. Look at you go."
- **Level unlocks** — "Disciplined. The hard part starts now."
- **After a reset** — "That was quick. Back to square one."
- **Empty history** — "Nothing to report. Yet."

The line sits under the button, holds for four seconds, then fades. Two tiny beats go with it:
the tier avatar springs when the level changes, the counter squashes when a milestone lands.
Neither loops, and neither ever takes over the screen.

Easter eggs exist for the days that earn one — 42, 666, 1000 — and are silent otherwise.

Never a lecture, never a scolding. The app does not tell you off for breaking a streak; it
just notes it and gets out of the way.

## The icon

Three pips climbing a diagonal, the top one hollow — the next one to win. White on black, with
a flat monochrome layer for themed launcher icons.

The mark is scaled to 80% of the icon's visible area, and its geometry is checked against a
**circular** launcher mask as well as a squircle one: a full-bleed version had its frame
sliced by the circle, which read as a bug rather than a design.

## What is deliberately absent

No confetti, no emoji, no sound, no streak sharing, no daily notifications, no mascot. A
small app with a strong identity beats a bloated app trying too hard to be funny.
