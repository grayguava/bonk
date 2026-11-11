# Bonk!

**A streak counter that greets you by name.**

Bonk! is a small, deliberately quiet Android app for one job: keeping a streak alive. It shows
a number (how many whole days your current streak has run) and gets out of the way.

## What it does

- **A streak that runs on a timer.** Press *Start streak* and the clock begins; the counter is
  the number of whole days elapsed. Press *Reset streak* when it ends, and you have to say why.
  The reason is required, so the history means something later.
- **Ten levels in five tiers.** *Awakening → Building → Strength → Mastery → Transcendence*,
  each with its own badge. The Levels tab tracks how far through the current level you are,
  to the second.
- **A history that remembers.** Every finished streak is a card: how long it lasted and why it
  ended. Tap it to see when it started and when it was reset.
- **A greeting that knows the time.** "Good morning," / "Late night," over your name, on the
  home screen only. Every other page names itself.
- **An About page** with the app details, the device it is running on, and a link to the repo.

## The tiers

| Tier | Levels |
| --- | --- |
| I · Awakening | 1 Fresh Start (0 days) · 2 Beginner (1) · 3 Initiate (3) |
| II · Building | 4 Week One (7) · 5 Disciplined (14) |
| III · Strength | 6 Consistent (30) · 7 Resilient (60) |
| IV · Mastery | 8 Dedicated (90) · 9 Unshaken (180) |
| V · Transcendence | 10 Year One (365) |

Full detail, including how each badge is drawn: [docs/tiers.md](docs/tiers.md).

## Installing

Grab an APK from the [releases page](https://github.com/grayguava/bonk/releases) and install it
(you will need to allow installs from your browser or file manager), or build it yourself:

```bash
adb install -r app-debug.apk
```

## Building from source

Requires **JDK 17 or newer** and the **Android SDK** (platform 37, build-tools 37). Nothing
else is required: no Android Studio needed.

```bash
export ANDROID_HOME="$HOME/Android/Sdk"     # wherever your SDK lives
./gradlew assembleDebug                     # -> app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Other useful tasks:

```bash
./gradlew installDebug      # build and install on every connected device
./gradlew clean             # drop build output
./gradlew test              # unit tests
```

If you prefer a GUI, the project opens in Android Studio (Meerkat or newer) and in IntelliJ
IDEA Community, which is considerably lighter.

The app is built with AGP 9 (which compiles Kotlin itself), Kotlin 2.4, Jetpack Compose and
Material 3. See [docs/architecture.md](docs/architecture.md) for how it all fits together.

## Layout

```
app/src/main/java/com/grayguava/bonk/
  MainActivity.kt
  ui/          the screens, the dock, the glass material
  data/        streaks, levels, the name, and the app's voice
  ui/theme/    colour scheme and shapes
docs/          architecture, tiers, design notes
```

## Contributing

Issues and pull requests are welcome. Keep the core UI calm: personality belongs in the
messages and the small surprises, not in extra screens. See [docs/design.md](docs/design.md)
for the rules the app follows.

## License

**GPL-3.0.** The full text is in the `LICENSE` file at the root of this repository.

The tab bar in `ui/BottomDock.kt` is derived from
[OpenTune](https://github.com/justshivv/Opentune) (MIT), which is compatible with the GPL.
