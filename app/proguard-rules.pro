# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Data classes / models
-keep class com.therapist.assistant.data.** { *; }

# Keep Parcelable/Serializable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Compose / Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ZXing
-keep class com.google.zxing.** { *; }

# App entry
-keep class com.therapist.assistant.MainActivity { *; }
-keep class com.therapist.assistant.TherapistApp { *; }

# Enums used by Room
-keepclassmembers enum * { *; }


# Keep app domain models and protocol catalog (used from Compose UI)
-keep class com.therapist.assistant.util.** { *; }
-keep class com.therapist.assistant.assessment.** { *; }
-keep class com.therapist.assistant.viewmodel.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# درمانگرنو — سخت‌تر
-keepclassmembers class * implements android.os.Parcelable { public static final ** CREATOR; }
-keep class com.therapist.assistant.data.** { *; }
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-optimizationpasses 5
-allowaccessmodification
