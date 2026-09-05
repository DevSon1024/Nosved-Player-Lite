# =============================================================================
# Nosved Player Lite Lite – ProGuard / R8 Rules
# Target: Media3 1.11.0 + Kotlinx Serialization + Nextlib
# =============================================================================

# ── Debug info (uncomment for crash-readable stack traces) ───────────────────
# -keepattributes SourceFile,LineNumberTable
# -renamesourcefileattribute SourceFile

# =============================================================================
# 1. Media3 / ExoPlayer 1.11.0
# =============================================================================

# Core player + common
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ExoPlayer renderer factories & decoders (reflection at runtime)
-keepclassmembers class androidx.media3.exoplayer.** {
    <init>(...);
    public *;
}

# MediaCodec selector implementations (looked up by class name)
-keep class androidx.media3.exoplayer.mediacodec.** { *; }

# DefaultRenderersFactory / NextRenderersFactory extension discovery
-keepnames class androidx.media3.exoplayer.DefaultRenderersFactory
-keepnames class io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory

# MediaSession (notification controls + media button handling)
-keep class androidx.media3.session.** { *; }
-dontwarn androidx.media3.session.**

# HLS + DASH adaptive streaming extractors
-keep class androidx.media3.exoplayer.hls.** { *; }
-keep class androidx.media3.exoplayer.dash.** { *; }
-dontwarn androidx.media3.exoplayer.hls.**
-dontwarn androidx.media3.exoplayer.dash.**

# Extractor (fragmented MP4, MKV, WebM, etc.)
-keep class androidx.media3.extractor.** { *; }
-dontwarn androidx.media3.extractor.**

# media3-ui and media3-ui-compose (alpha)
-keep class androidx.media3.ui.** { *; }
-dontwarn androidx.media3.ui.**

# Native decoder extension JNI entry points
# AV1 / Opus / VP9 / FLAC – keep if those optional deps are enabled
-keep class androidx.media3.decoder.av1.**   { *; }
-keep class androidx.media3.decoder.opus.**  { *; }
-keep class androidx.media3.decoder.vp9.**   { *; }
-keep class androidx.media3.decoder.flac.**  { *; }
-dontwarn androidx.media3.decoder.**

# =============================================================================
# 2. Kotlinx Serialization
# =============================================================================

# Serialization runtime reflection (required for @Serializable classes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep all @Serializable-annotated classes and their companions
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep @kotlinx.serialization.Serializable class * {
    static ** Companion;
    static ** serializer();
}

# Serializer lookups by class name (navigation route arguments)
-keepclassmembers class kotlinx.serialization.** {
    volatile <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.* <fields>;
}

# Navigation: type-safe route classes (SearchResultsRoute, etc.)
-keep class com.devson.nvplayerlite.navigation.** { *; }

# =============================================================================
# 3. Nextlib (io.github.anilbeesetti.nextlib)
# =============================================================================

-keep class io.github.anilbeesetti.nextlib.** { *; }
-dontwarn io.github.anilbeesetti.nextlib.**

# =============================================================================
# 5. Room Database
# =============================================================================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# =============================================================================
# 6. Kotlin Coroutines & StateFlow
# =============================================================================

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# =============================================================================
# 7. Compose + Lifecycle
# =============================================================================

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# =============================================================================
# 8. General Android
# =============================================================================

# Parcelables (used by navigation argument bundles)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Enum values() / valueOf() – used by DataStore and settings enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
