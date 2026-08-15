# ONNX Runtime's native layer resolves these classes/methods via JNI reflection at
# runtime; R8 stripping or renaming them causes a JNI DETECTED ERROR abort in release
# builds (java_class == null in GetMethodID from OrtSession.run).
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**
