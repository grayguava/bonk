# Releasing

How a Bonk! release is made. None of this is needed to build or run the app.

## The version

The version lives in the `version` file at the root of the repository, as three numeric parts
(`0.1.0`). The build reads it for `versionName` and derives `versionCode` from it
(`major*10000 + minor*100 + patch`), so the code always rises with the version.

The file is not tracked yet, so a push cannot start a release by accident. Add it back with
`git add -f version` when the first release is ready.

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

- **preflight** reads the version, derives the tag `v0.1.0`, and refuses to run if that tag
  already exists.
- **test** runs the unit tests.
- **build** checks the secrets, decodes the key, assembles, refuses to publish an APK signed
  with the debug key, writes the checksum, and verifies it.
- **publish** verifies the checksum again and creates a **draft** release from
  `.github/RELEASE_TEMPLATE.md`, with `<commit>` and `<tag>` filled in.

The release carries two files:

```
v0.1.0_universal.apk
v0.1.0_universal.apk.sha256
```

The APK holds every processor type. The `.sha256` file is the standard `sha256sum` line, so
`sha256sum -c v0.1.0_universal.apk.sha256` checks it.

## Building a signed release locally

```bash
export BONK_KEYSTORE="$PWD/bonk-release.jks"
export BONK_KEYSTORE_PASSWORD=...
export BONK_KEY_ALIAS=bonk
export BONK_KEY_PASSWORD=...
./gradlew :app:assembleRelease
```
