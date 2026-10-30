# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer {
    *;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Navigation type-safe routes.
-keep class com.vibecode.minigo.ui.HomeDestination { *; }
-keep class com.vibecode.minigo.ui.SetupDestination { *; }
-keep class com.vibecode.minigo.ui.ResumeDestination { *; }
-keep class com.vibecode.minigo.ui.TutorialDestination { *; }
-keep class com.vibecode.minigo.ui.SettingsDestination { *; }
-keep class com.vibecode.minigo.ui.AboutDestination { *; }
-keep class com.vibecode.minigo.ui.GameDestination { *; }
