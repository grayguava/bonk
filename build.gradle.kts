// AGP 9 ships its own Kotlin compiler (built-in Kotlin). To use a newer Kotlin
// than the one AGP bundles, put KGP on the buildscript classpath here.
// Keep in sync with `kotlin` in gradle/libs.versions.toml.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// The root project produces no artefacts, only reports, and those go with the rest
// of the machine-local build state so the root of the repository stays readable.
layout.buildDirectory.set(layout.projectDirectory.dir(".gradle/build"))
