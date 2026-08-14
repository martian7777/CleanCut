# Google Play Services / ML Kit rely on reflection for their dynamic optional-module
# system (ModuleInstallClient, OptionalModuleApi, SubjectSegmentation.getClient()) -
# R8 obfuscating/stripping their internals breaks that at runtime with obscure NPEs.
# Keep these vendor SDKs untouched; only our own app code gets minified/obfuscated.
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
