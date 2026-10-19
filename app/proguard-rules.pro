# ProGuard rules for WorkHoursTracker
-keepattributes *Annotation*

# Room Database - keep entity classes and DAO interfaces
-keep class com.example.workhourstracker.data.WorkShift { *; }
-keep class com.example.workhourstracker.data.WorkShiftDao { *; }
-keep class com.example.workhourstracker.data.AppDatabase { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin serialization (if used in the future)
-keepattributes Signature
-keepattributes InnerClasses

# JSON parsing (org.json is part of Android SDK, no special rules needed)

# Keep Compose runtime
-dontwarn androidx.compose.**
