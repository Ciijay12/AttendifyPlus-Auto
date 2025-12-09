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

# Coroutines
-keep class kotlinx.coroutines.** { *; }
