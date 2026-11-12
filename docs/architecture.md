# Architecture

Bonk! is a single-module Compose app with no third-party dependencies beyond the AndroidX and
Jetpack libraries. There is no DI framework and no database: the app is one
screenful of state and a file of preferences. The only network call is the one the update
check makes, and only when you ask it to.

```
com.grayguava.bonk
├── MainActivity.kt      sets the content, turns on edge-to-edge
├── ui/
│   ├── BonkScreen.kt    the shell: header, dock, tab dispatch
│   ├── HomeScreen.kt    the streak counter and the start/reset button
│   ├── HistoryScreen.kt finished streaks
│   ├── LevelsScreen.kt  the tiers, the level art and the level progress
│   ├── LevelArt.kt      the picture for a level, decoded from the assets
│   ├── AboutScreen.kt   app details, backups and the update check
│   ├── WelcomeScreen.kt asks for your name on a fresh install
│   ├── BottomDock.kt    the glass tab bar
│   ├── Glass.kt         the glass material: panels, buttons, icon buttons
│   ├── Layout.kt        screenTopGap()
│   └── theme/Theme.kt   the colour scheme
└── data/
    ├── Streak.kt         streak records, storage, duration formatting
    ├── Levels.kt         the ten levels and five tiers
    ├── Backup.kt         the backup document: export and import
    ├── Update.kt         the release check, the download
    ├── Install.kt        the install session, and how it went
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

## Backups

A backup is one json document: a format name, a format version, the time it was taken, your
name, the running streak and every finished streak. The version is in the document, so a file
from a newer app is refused rather than half-read, and the export and the import go through
the same helpers, so they cannot drift apart.

The file is written and read through the system file picker, so the app needs no storage
permission and cannot see the rest of the phone.

## Updates

The About page asks github for the newest published release, compares its version with the
one you are running, and says whether there is anything new. Nothing is downloaded until you
agree to it.

The bytes go straight into an **install session**, which the system holds, rather than into
a file in the app's cache, which the system may reclaim halfway through. The apk's checksum
is read from the release and checked against the download as it arrives; an apk that does
not match is thrown away and never handed over.

Android will not let the app install anything until the user says the app may, which the
app checks before it downloads and, if the answer is no, sends the user to the setting
where that is decided. The install itself is the system's: it shows its own screen, and the
user taps install. Nothing can be installed without that tap.

The permission this needs is `REQUEST_INSTALL_PACKAGES`, and Google Play only grants it to a
few kinds of app. If Bonk ever goes on Play, the updater has to be reconsidered: Play's own
in-app update would be the way, and it only sees versions published through Play.

An update keeps the app's data, so the streaks and the name survive it. The process is
killed by a successful install, so the app is opened again by hand afterwards.

## The dock

`BottomDock.kt` is a port of the tab bar from
[OpenTune](https://github.com/justshivv/Opentune) (GPL-3.0, the same licence as this app,
used as a reference).
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
