import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}
val splitApks = !project.hasProperty("noSplits") && !gradle.startParameter.taskNames.any {
    it.contains("debug", ignoreCase = true)
}

val appVersion = "1.4.0"

android {
    namespace = "com.devson.nvplayerlite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.devson.nvplayerlite"
        minSdk = 26
        targetSdk = 36
        versionCode = 140
        versionName = appVersion

        if (!splitApks) {
            // For debug builds - only include device ABI for faster builds
            ndk {
                abiFilters.add("arm64-v8a")
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String?
                keyPassword = keystoreProperties["keyPassword"] as String?
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String?
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "NosLite Beta")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", "Nosved Player Lite Lite")
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    if (splitApks) {
        splits {
            abi {
                isEnable = true
                reset()
                // x86 excluded: <1% of active devices; add back if sideload-on-emulator is required
                include("arm64-v8a", "armeabi-v7a", "x86_64")
                isUniversalApk = false
            }
        }
    }
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val abiName = outputImpl.filters.find { it.filterType == "ABI" }?.identifier ?: "universal"
            outputFileName = "NosvedPlayerLite_v${variant.versionName}-${abiName}.apk"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
        resValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "**/kotlin/**"
            excludes += "**/*.kotlin_metadata"
            excludes += "**/*.version"
            excludes += "**/kotlin-tooling-metadata.json"
            // Additional metadata stripping (same set NextPlayer uses)
            excludes += "DebugProbesKt.bin"
            excludes += "**/attach_hotspot_windows.dll"
            excludes += "META-INF/licenses/**"
            excludes += "META-INF/*.properties"
            excludes += "META-INF/gradle/**"
            excludes += "META-INF/proguard/**"
            excludes += "**.proto"
            excludes += "**/DebugProbesKt.bin"
        }
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf(
                "**/libavcodec.so",
                "**/libavfilter.so",
                "**/libavformat.so",
                "**/libavutil.so",
                "**/libswresample.so",
                "**/libswscale.so",
                "**/libpostproc.so"
            )
        }
    }
    ndkVersion = "27.0.12077973"
}

dependencies {
    // androidx
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Material3
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)

    // --- Media3 1.11.0 ---
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // alpha: Material3-based Compose player controls surface
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.extractor)
    // Inspector for non-playback media inspection / metadata extraction
    implementation(libs.androidx.media3.inspector)
    // Optional software-decoder extensions - uncomment to activate (increases APK size)
    // implementation(libs.androidx.media3.decoder.av1)
    // implementation(libs.androidx.media3.decoder.opus)
    // implementation(libs.androidx.media3.decoder.vp9)
    // implementation(libs.androidx.media3.decoder.flac)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Nextlib: Media3 ExoPlayer extensions with native decoders for broad format playback
    implementation(libs.nextlib.media3ext)
    implementation(libs.nextlib.mediainfo)
    
    // DataStore for Settings
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Room for Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)    
    
    // Compose foundation + navigation (material3 already covers Material components)
    // NOTE: libs.androidx.compose.material was androidx.wear.compose:compose-material (Wear OS).
    // Removed — it does not belong in a phone app and added ~500 KB to every APK.
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.compose)

    // Kotlinx Serialization (type-safe navigation routes)
    implementation(libs.kotlinx.serialization.json)

    // coil
    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    // WorkManager for background tasks
    implementation(libs.androidx.work.runtime.ktx)

    // documentfile
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}