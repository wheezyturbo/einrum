# Einrúm Security ProGuard Rules

# Obfuscate all code except entry points
-keepattributes Signature, *Annotation*, InnerClasses
-renamesourcefileattribute SourceFile
-keepattributes SourceFile, LineNumberTable

# Standard Android entry points
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep MVI Immutable states to prevent reflection issues in Compose
-keep @androidx.compose.runtime.Immutable class * { *; }

# Prevent tampering with security utilities
-keep class com.einrum.core.common.SecurityUtils { *; }

# Remove all logging calls in production
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
