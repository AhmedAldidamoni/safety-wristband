-keep class com.safetywristband.tracker.data.remote.dto.** { *; }
-keep class com.safetywristband.tracker.data.local.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
}
