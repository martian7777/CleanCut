plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.cleancut.bgremover"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cleancut.bgremover"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    // Release signing is only available where CLEANCUT_KEYSTORE_PATH is set (CI, from
    // secrets). A local `./gradlew assembleRelease` with no keystore present still
    // configures cleanly - it just produces an unsigned build.
    val releaseKeystorePath = System.getenv("CLEANCUT_KEYSTORE_PATH")

    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("CLEANCUT_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CLEANCUT_KEY_ALIAS")
                keyPassword = System.getenv("CLEANCUT_KEY_PASSWORD")
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    implementation("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.mlkit:vision-common:17.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
