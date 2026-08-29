# Proguard rules for Skudo
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep Room Database and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * {
    <methods>;
}
-keep @androidx.room.Entity class * {
    <fields>;
    <methods>;
}
-dontwarn androidx.room.paging.**

# Keep Data Models & State for Gson Serialization
-keep class com.prem.skudo.model.** { *; }
-keep class com.prem.skudo.database.** { *; }
-keep class com.prem.skudo.viewmodel.GameState { *; }
-keep class com.prem.skudo.viewmodel.HomeState { *; }

# Keep Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Firebase
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
