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
