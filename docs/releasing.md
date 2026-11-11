# Releasing

How a Bonk! release is made. None of this is needed to build or run the app.

## The version

The version lives in the `version` file at the root of the repository, as three numeric parts
(`0.2.0`). The build reads it for `versionName` and derives `versionCode` from it
(`major*10000 + minor*100 + patch`), so the code always rises with the version.

The file is tracked, and a push that changes it is what starts a release. To make a release,
change the version, commit it and push it. To push anything else, leave the file alone and
nothing happens.

## The signing key

The release is signed with a key that lives only in the repository's secrets, so nobody needs
one to work on the app. A release build without a key falls back to the debug key and still
installs.

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

Keep the key and its password somewhere you control. If they are lost, a build signed with a
new key cannot upgrade an installed copy of the app, so people have to uninstall it first and
install again.

## The workflow

A push to `main` that touches the `version` file starts the release workflow:

- **preflight** reads the version, derives the tag `v0.2.0`, and refuses to run if that tag
  already exists.
- **test** runs the unit tests.
- **build** checks the secrets, decodes the key, assembles, refuses to publish an APK signed
  with the debug key, writes the checksum, and verifies it.
- **publish** verifies the checksum again and creates a **draft** release from
  `.github/RELEASE_TEMPLATE.md`, with `<commit>` and `<tag>` filled in.

The release is a draft on purpose: nobody sees it until you have written the notes and
published it by hand. The template leaves each section empty for you to fill.

The release carries two files:

```
v0.2.0_universal.apk
v0.2.0_universal.apk.sha256
```

The APK holds every processor type. The `.sha256` file is the standard `sha256sum` line, so
`sha256sum -c v0.2.0_universal.apk.sha256` checks it.

## Trying an update

The debug build always reports version 0.1.0, whatever the version file says, so the update
check always finds the newest published release. Install the debug build, open the about page
and press *Check for updates*. The apk it downloads is the real signed release, and it
installs beside the debug build rather than over it: the two have different application ids.

To try an update that replaces the app in place, build a release with a version of your own
and the release key:

```bash
export BONK_KEYSTORE="$PWD/bonk-release.jks"
export BONK_KEYSTORE_PASSWORD=...
export BONK_KEY_ALIAS=bonk
export BONK_KEY_PASSWORD=...
export BONK_VERSION=0.1.0
./gradlew :app:assembleRelease
```

The `BONK_VERSION` variable keeps the tracked version file untouched, which matters: a push
that changes that file starts a release. Uninstall the release app first, install the apk
that comes out, and the update check will find the newest release on github and put it in
place.

Android asks before installing anything, and wants the "install unknown apps" permission for
the app doing the installing.

## Building a signed release locally

```bash
export BONK_KEYSTORE="$PWD/bonk-release.jks"
export BONK_KEYSTORE_PASSWORD=...
export BONK_KEY_ALIAS=bonk
export BONK_KEY_PASSWORD=...
./gradlew :app:assembleRelease
```
