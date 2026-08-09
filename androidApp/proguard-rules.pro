# kotlinx-serialization: conserva los serializers generados de los DTOs
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.maestro.shared.**$$serializer { *; }
-keepclassmembers class com.maestro.shared.** { *** Companion; }
-keepclasseswithmembers class com.maestro.shared.** { kotlinx.serialization.KSerializer serializer(...); }

# Ktor usa reflexión ligera sobre el engine
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.debug.**

# SLF4J (dependencia transitiva de Ktor, sin binding en Android)
-dontwarn org.slf4j.**
