plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// The version lives in the `version` file at the root of the repository, which the
// release workflow watches. A push that changes it starts a release; the fallback
// below is only for a build made without the file.
//
// A local build can take a version of its own, so an update can be tried without
// touching the tracked file: BONK_VERSION=0.1.0 ./gradlew assembleRelease
val versionFile = rootProject.file("version")
val localVersion = (project.findProperty("bonkVersion") as String?)?.trim()
    ?: providers.environmentVariable("BONK_VERSION").orNull?.trim()
val appVersion = localVersion?.takeIf { it.isNotEmpty() }
    ?: versionFile.takeIf { it.exists() }
        ?.readText()?.trim()?.takeIf { it.isNotEmpty() }
    ?: "0.2.0"
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

    buildFeatures {
        compose = true
    }
}

// The debug build always says it is 0.1.0, so the update check finds the newest
// published release and the whole flow can be tried on a device.
androidComponents {
    onVariants { variant ->
        if (variant.buildType == "debug") {
            variant.outputs.forEach { output ->
                output.versionName.set("0.1.0")
                output.versionCode.set(100)
            }
        }
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

    testImplementation(libs.junit)
    testImplementation(libs.json)
}
