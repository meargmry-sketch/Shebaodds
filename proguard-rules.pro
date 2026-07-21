# ============================================================
# SHEBAODDS - PROGUARD RULES
# Full configuration for Sportsbook + 51+ Casino Games
# ============================================================

# ============================================================
# GENERAL ANDROID RULES
# ============================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Fragment
-keep public class * extends android.view.View
-keep public class * extends android.widget.BaseAdapter

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve the names of classes that are referenced in the layout XML.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ============================================================
# COMPOSE RULES (Jetpack Compose)
# ============================================================
# Keep Compose @Composable functions (they are often called via reflection)
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Composable public *;
}

# ============================================================
# KOTLIN COROUTINES
# ============================================================
# Keep coroutine continuation classes (required for suspend functions)
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.internal.** { *; }
-keep class kotlinx.coroutines.scheduling.** { *; }
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.coroutines.internal.**

# ============================================================
# ROOM DATABASE
# ============================================================
# Keep Room entities, DAOs, and database classes
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase {
    public <init>(...);
}
-keep class * implements androidx.room.Dao {
    <methods>;
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.ColumnInfo class * { *; }
-keep @androidx.room.PrimaryKey class * { *; }
-keep @androidx.room.ForeignKey class * { *; }
-keep @androidx.room.Index class * { *; }
-dontwarn androidx.room.**

# ============================================================
# MOSHI (JSON Serialization)
# ============================================================
# Keep Moshi adapter classes and generated adapters
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep class **.MoshiJsonAdapter { *; }
# Keep all classes annotated with @JsonClass (including Kotlin data classes)
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-dontwarn com.squareup.moshi.**

# ============================================================
# RETROFIT & OKHTTP
# ============================================================
# Keep Retrofit interfaces and service methods
-keep interface * extends retrofit2.Call { *; }
-keep class * extends retrofit2.Call { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================================
# WEBVIEW (if any casino games use embedded web content)
# ============================================================
# Keep JavaScript interface classes (if any)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep WebView classes and their inner classes
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**

# ============================================================
# BIOMETRIC AUTHENTICATION
# ============================================================
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ============================================================
# GSON / OTHER SERIALIZATION (if used)
# ============================================================
# Keep generic signatures of TypeToken and its subclasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-dontwarn com.google.gson.**

# ============================================================
# SOCKET.IO (if used for WebSocket updates)
# ============================================================
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# ============================================================
# GOOGLE GENERATIVE AI (Gemini API)
# ============================================================
-keep class com.google.ai.** { *; }
-dontwarn com.google.ai.**

# ============================================================
# THIRD-PARTY LIBRARIES (general)
# ============================================================
# Keep all classes from commonly used libraries
-keep class com.example.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.ui.** { *; }
-keep class com.example.util.** { *; }
-keep class com.example.viewmodel.** { *; }
-keep class com.example.repository.** { *; }

# ============================================================
# OPTIONAL: Keep debug symbols for crash reporting (optional)
# ============================================================
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile

# ============================================================
# FINAL: Do NOT obfuscate the application entry point
# ============================================================
-keep class com.example.MainActivity { *; }
-keep class com.example.ui.theme.** { *; }