# Elderly Launcher ProGuard Rules

# Kotlin
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep data classes for JSON serialization
-keepclassmembers class com.elderlylauncher.data.** {
    <fields>;
    <init>(...);
}

# Keep our app's model classes
-keep class com.elderlylauncher.data.AppInfo { *; }
-keep class com.elderlylauncher.data.QuickContact { *; }

# Coil
-dontwarn coil.**

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
