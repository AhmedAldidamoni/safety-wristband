-keep class com.safewristband.tracker.data.remote.dto.** { *; }
-keep class com.safewristband.tracker.data.local.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
}
