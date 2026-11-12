# Design notes

The rule the app follows: **keep the core UI calm.** Personality belongs in the messages, the
transitions and the occasional surprise, never in extra chrome and never in features added
just to justify the name.

## The palette

The app is black. `background` and `surface` are `#000000`, the theme is a single dark
scheme, and the window background is black too so there is no flash on launch.

The Material surfaces used on the About and History pages are **neutral greys**:
`#141414` for cards, `#242424` for the tonal ones. Material's own `surfaceContainer*` roles
carry a blue cast and read as a bluish near-black, which does not sit right here.

The dialogs are `#0A0A0A` on a pure black page, which is very nearly nothing at all, so they
carry a hairline of white at 12% around them. Without it they are a shape you half guess at.

## Glass, but only on the home screen

The dock and the start/reset button are glass; every other page is Material 3. That split is
deliberate: the home screen is the app, the rest is paperwork.

The material is **frost**, not smoke. Frost is a milky film with a fine grain over it; smoke
is a dark wash. The grain is a 96×96 tile of white speckle (alpha 3 to 15) drawn as a tiled
`ImageShader` over the film, underneath the text so the labels stay clean.

There is no backdrop blur, because there is nothing to blur. Every page behind the glass is
flat black. If the app ever puts artwork behind the glass, `dev.chrisbanes.haze` would give
the real thing on Android 12+.

## The pages

Changing page fades the old one out over 150 ms while the new one fades in over 200 ms,
and the new one slides a sixteenth of the width in the direction of travel. The two
overlap, so moving along the dock reads as a move and never as a cut or a wait.

The slide is the smallest that still reads. Any larger and the app starts to hurry; any
smaller and the fade alone does the work, which is fine too.

The pages are reached from the dock only. A swipe does not change page: the app has three
of them and a wrong swipe on a list is worse than no swipe at all.

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

- **Milestones**: "A whole week. Look at you go."
- **Level unlocks**: "Monk in Training. The hard part starts now."
- **After a reset**: "That was quick. Back to square one."
- **Empty history**: "Nothing to report. Yet."

The line sits under the button, holds for four seconds, then fades. Two tiny beats go with it:
the level's character springs when the level changes, the counter squashes when a milestone
lands. Neither loops, and neither ever takes over the screen.

Easter eggs exist for the days that earn one (42, 666, 1000) and are silent otherwise.

Never a lecture, never a scolding. The app does not tell you off for breaking a streak; it
just notes it and gets out of the way.

## The icon

Three pips climbing a diagonal, the top one hollow: the next one to win. White on black, with
a flat monochrome layer for themed launcher icons.

The mark is scaled to 80% of the icon's visible area, and its geometry is checked against a
**circular** launcher mask as well as a squircle one: a full-bleed version had its frame
sliced by the circle, which read as a bug rather than a design.

## What is deliberately absent

No confetti, no sound, no streak sharing, no daily notifications, no mascot. A small app
with a strong identity beats a bloated app trying too hard to be funny.

The one emoji in the app is the party hat on the update popup, and it is there because a
release is worth marking. It is the exception that proves the rule: nowhere else does the
app put on a hat.

The messages at the bottom of the settings page stay for three seconds, a quarter less than
Material's four, so a note is read and gone rather than lingering over the page.
