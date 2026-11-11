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

The debug build has its own application id, `com.grayguava.bonk.debug`, so it installs
alongside a release build of the same app instead of replacing it. Two builds signed with
different keys cannot upgrade one another, and this keeps that from biting.

If you prefer a GUI, the project opens in Android Studio (Meerkat or newer) and in IntelliJ
IDEA Community, which is considerably lighter.

The app is built with AGP 9 (which compiles Kotlin itself), Kotlin 2.4, Jetpack Compose and
Material 3. See [docs/architecture.md](docs/architecture.md) for how it all fits together.

## Releases

Nobody needs a signing key to work on the app. Debug builds are all you need locally, and the
release build falls back to the debug key when no key is configured, so it still installs.

The version lives in the `version` file at the root of the repository. Bump it and push to
`main`: the release workflow reads that file, refuses to run if the tag already exists,
builds a signed APK, and leaves a draft release with the notes template filled in for you to
finish and publish. The release carries two files:

```
v0.1.0_universal.apk
v0.1.0_universal.apk.sha256
```

The APK runs on every processor type. The `.sha256` file is the standard `sha256sum` line,
so `sha256sum -c v0.1.0_universal.apk.sha256` checks it.

The repository needs four secrets:

| Secret | What it is |
| --- | --- |
| `BONK_KEYSTORE_BASE64` | the keystore file, base64 encoded |
| `BONK_KEYSTORE_PASSWORD` | the keystore password |
| `BONK_KEY_ALIAS` | the key alias |
| `BONK_KEY_PASSWORD` | the key password |

Generate the keystore once, with the JDK's `keytool`:

```bash
keytool -genkey -v -keystore bonk-release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias bonk
base64 -w0 bonk-release.jks > bonk-release.jks.b64
```

The workflow refuses to publish an APK signed with the debug key, and the checksum is
verified twice, once before the artifact is uploaded and once after it is downloaded.

To build a signed release yourself, point the build at the keystore:

```bash
export BONK_KEYSTORE="$PWD/bonk-release.jks"
export BONK_KEYSTORE_PASSWORD=...
export BONK_KEY_ALIAS=bonk
export BONK_KEY_PASSWORD=...
./gradlew :app:assembleRelease
```

Keep the keystore somewhere you control. If it is lost, a build signed with a new key cannot
upgrade an installed copy of the app, so people have to uninstall it first and install again.

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
