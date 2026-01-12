# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keep interface org.koin.** { *; }

# Retrofit (if used later)
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Data Classes (Entities/Models) - preventing obfuscation of fields for JSON serialization/reflection
-keep class com.attendifyplus.data.local.entities.** { *; }
-keep class com.attendifyplus.data.model.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep class names and members for classes that are serialized/deserialized by Firebase
-keepclassmembers class com.attendifyplus.data.model.AppUpdateConfig {
    <fields>;
    <init>();
}
