# ProGuard / R8 rules for WorkHoursTracker
-keepattributes *Annotation*,Signature,InnerClasses,SourceFile,LineNumberTable

# Keep data models, Room Entities, DAOs, and UserPreferences
-keep class com.example.workhourstracker.data.** { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Kotlin Coroutines internal factories
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose runtime optimizations
-dontwarn androidx.compose.**

# R8 Size & String Pool Optimizations
-renamesourcefileattribute SourceFile

# Strip verbose and debug logging in release builds to reduce DEX size
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
