plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// The release key comes from the environment when there is one. CI passes it in as a
// secret; on a developer machine there is none, so a release build falls back to the
// debug key and still installs. The release workflow checks the secrets before it
// builds, so a missing key fails CI instead of publishing a debug-signed release.
val releaseKeystore = System.getenv("BONK_KEYSTORE")?.let(::file)?.takeIf { it.exists() }

android {
    namespace = "com.grayguava.bonk"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.grayguava.bonk"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("BONK_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("BONK_KEY_ALIAS")
                keyPassword = System.getenv("BONK_KEY_PASSWORD")
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
