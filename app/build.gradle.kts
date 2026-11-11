plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// The version lives in the `version` file at the root of the repository. One place to
// bump, and the release workflow reads the same file to work out the tag.
val versionFile = rootProject.file("version")
val appVersion = versionFile.takeIf { it.exists() }?.readText()?.trim()
    ?: error("version file missing at ${versionFile.path}")
val versionParts = appVersion.split(".")
require(versionParts.size == 3 && versionParts.all { it.toIntOrNull() != null }) {
    "version must contain three numeric parts, for example 0.2.0, but was $appVersion"
}
// 0.1.0 -> 100, so the code always rises as the version does.
val appVersionCode = versionParts[0].toInt() * 10_000 + versionParts[1].toInt() * 100 + versionParts[2].toInt()

// The release key comes from the environment when there is one. CI passes it in as a
// secret; on a developer machine there is none, so a release build falls back to the
// debug key and still installs. The release workflow checks the secrets before it
// builds, so a missing key fails CI instead of publishing a debug-signed release.
//
// Read through providers, not System.getenv: the configuration cache then tracks the
// values as inputs, so a build with a different environment is not served a stale
// configuration.
val releaseKeystore = providers.environmentVariable("BONK_KEYSTORE").orNull?.let(::file)?.takeIf { it.exists() }

android {
    namespace = "com.grayguava.bonk"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.grayguava.bonk"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersion
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = providers.environmentVariable("BONK_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("BONK_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("BONK_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        debug {
            // Its own application id, so the debug build and the CI-signed release
            // can sit on the same phone: two builds with different signatures cannot
            // upgrade one another.
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // One APK per processor type, plus one that runs anywhere. Off by default so a
    // local build is a single file; the release workflow turns it on.
    splits {
        abi {
            isEnable = providers.gradleProperty("abiSplits").map { it.toBoolean() }.getOrElse(false)
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
