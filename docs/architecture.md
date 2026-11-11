# Architecture

Bonk! is a single-module Compose app with no third-party dependencies beyond the AndroidX and
Jetpack libraries. There is no DI framework, no database and no networking: the app is one
screenful of state and a file of preferences.

```
com.grayguava.bonk
├── MainActivity.kt      sets the content, turns on edge-to-edge
├── ui/
│   ├── BonkScreen.kt    the shell: header, dock, tab dispatch
│   ├── HomeScreen.kt    the streak counter and the start/reset button
│   ├── HistoryScreen.kt finished streaks
│   ├── LevelsScreen.kt  the tiers, their badges and the level progress
│   ├── AboutScreen.kt   app details and repo links
│   ├── WelcomeScreen.kt asks for your name on a fresh install
│   ├── BottomDock.kt    the glass tab bar
│   ├── Glass.kt         the glass material: panels, buttons, icon buttons
│   ├── Layout.kt        screenTopGap()
│   ├── Swipe.kt         swipe between tabs
│   └── theme/Theme.kt   the colour scheme
└── data/
    ├── Streak.kt         streak records, storage, duration formatting
    ├── Levels.kt         the ten levels and five tiers
    ├── Personality.kt    the app's voice
    └── UserStore.kt      your name
```

## State

Everything lives in `BonkScreen` as Compose state and is written straight through to
`SharedPreferences` when it changes:

| State | Where it lives |
| --- | --- |
| The running streak's start time | `StreakStore.currentStart` |
| Finished streaks | `StreakStore.history`, a JSON array, newest first |
| Your name | `UserStore.name` |

There is no `ViewModel` and no `StateFlow`. The state is small enough that recomposition
costs nothing, and the app never survives a configuration change with anything worth
restoring. The persisted copy is the source of truth.

`StreakStore` and `UserStore` are thin wrappers over two `SharedPreferences` files (`bonk`
and `bonk_user`). They are deliberately simple: the schema is small, and swapping them for
DataStore later is a contained change.

## Navigation

Three tabs, dispatched by index in `BonkScreen`:

```kotlin
when (selectedTab) {
    0 -> HomeScreen(...)
    1 -> HistoryScreen(...)
    else -> LevelsScreen(...)
}
```

The About page is not a tab. It is a boolean that replaces the whole scaffold, with a
`BackHandler` so the system back button returns instead of exiting.

## The streak model

A streak is a **timer**, not a checklist: it starts when you press *Start streak* and ends
when you press *Reset streak*. There is no daily action to remember, so nothing expires on
its own.

- `streakDays(start, end)` = whole days elapsed, floored. A streak started a second ago is
  0 days.
- The big number on the home screen is that day count; the line under it is the elapsed
  time, ticking every second.
- The tier and level come from `levelFor(days)` on the same number, so the home header and
  the Levels page can never disagree.

## The dock

`BottomDock.kt` is a port of the tab bar from
[OpenTune](https://github.com/justshivv/Opentune) (MIT-licensed, used as a reference).
The interesting part is the **lens**: the indicator is a box whose two edges travel
separately: the leading edge springs out ahead and the trailing edge catches up late, so
the lens stretches toward the new tab and wobbles home.

The springs, the crossfades and the shape metrics are all in `BottomDock.kt` with the
original numbers next to them.

## The glass

`Glass.kt` owns one material, shared by the dock and the floating buttons: a film, a lit
rim gradient and a drop shadow. The panels add a fine noise tile over the film, which is
what makes it read as frost rather than smoke.

There is no backdrop blur: the app's pages are flat black, so there is nothing to blur.
See [design.md](design.md).

## Things worth knowing before changing this code

- **`Icon` ignores size modifiers.** It appends `requiredSize(painter.intrinsicSize)`, so a
  drawable sized with `fillMaxWidth` stays at 24 dp. Use `Image` when you control the size.
- **A shape's fill and its stroke must share the same `Shape`, with no `clip` in between.** A
  clip before the border eats the outer half of the stroke and leaves the fill's edge half a
  pixel proud of it.
- **The dock's lens must be inset.** OpenTune places it at `inset + slide`; offsetting by the
  slide alone leaves it flush against the pill's edge, which reads as a misaligned border.
- **Content height is not content height.** A card's height can be set by a label wrapping
  to two lines rather than by the text you expect.
