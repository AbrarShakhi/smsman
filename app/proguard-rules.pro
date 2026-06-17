# SMS Man — R8 keep rules
#
# Most rules are auto-applied by the libraries we depend on (Hilt, Room, Compose, kotlinx-serialization,
# WorkManager, Coil, DataStore). The entries below cover only what those defaults don't catch.

# --- Navigation 3 ----------------------------------------------------------
# Routes are looked up by class in entryProvider { entry<RouteType> { ... } }. R8 may strip routes
# that are referenced only through reflection-style type matching, so keep them explicitly.
-keep class com.abrarshakhi.smsman.ui.nav.* { *; }
-keepclassmembers class com.abrarshakhi.smsman.ui.nav.* { *; }

# --- Domain models ---------------------------------------------------------
# Hilt and Room R8 rules cover their generated types but our domain models occasionally cross
# Parcel and Json boundaries; keep them lossless.
-keep class com.abrarshakhi.smsman.domain.model.** { *; }

# --- kotlinx-serialization @Serializable -----------------------------------
# Belt-and-suspenders: the serialization plugin emits the right rules itself, but anything we
# encode by class type benefits from this defensive keep so refactors don't break R8 release builds.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,InnerClasses
-keepclassmembers,allowobfuscation class * {
    @kotlinx.serialization.Serializable <methods>;
}

# --- Coroutines ------------------------------------------------------------
# Stack traces look nicer when DebugProbesKt stays present (only matters in debug, but harmless).
-dontwarn kotlinx.coroutines.debug.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}

# --- AndroidX MMS PDU library (when M4 lands) ------------------------------
# Placeholder: vendoring AOSP com.android.mms.* requires keeping its reflective PDU codec entries.
# Activate once the codec is checked in.
# -keep class com.android.mms.pdu.** { *; }

# --- Defaults that AGP forgets to include in some setups -------------------
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
