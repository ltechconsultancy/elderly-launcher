# Elderly Launcher ProGuard Rules

# Kotlin
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep data classes
-keepclassmembers class com.elderlylauncher.data.** {
    <fields>;
    <init>(...);
}
-keep class com.elderlylauncher.data.AppInfo { *; }
-keep class com.elderlylauncher.data.QuickContact { *; }

# Keep enums
-keepclassmembers enum com.elderlylauncher.data.** {
    **[] $VALUES;
    public *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# DataStore
-keep class androidx.datastore.** { *; }

-keep class com.elderlylauncher.util.ElderlyNotificationListener { *; }
