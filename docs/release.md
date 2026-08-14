# 🚀 Release & Deployment Guide

This document outlines the release process, versioning conventions, code signing, and CI/CD automation for **CleanCut**.

---

## 1. Versioning Strategy

CleanCut adheres strictly to [Semantic Versioning (SemVer 2.0.0)](https://semver.org/):

`MAJOR.MINOR.PATCH`

- **MAJOR**: Incompatible architectural changes or redesigns.
- **MINOR**: New features, new ML capabilities, or UI enhancements added in a backward-compatible manner.
- **PATCH**: Backward-compatible bug fixes, performance improvements, and security patches.

Version code and version name are maintained in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1
    versionName = "0.1.0"
}
```

---

## 2. Release Signing Configuration

Release builds are signed using a PKCS12 / JKS keystore. For local and CI builds, credentials are provided via environment variables:

| Environment Variable | Description |
| :--- | :--- |
| `CLEANCUT_KEYSTORE_PATH` | Absolute path to the keystore file (`.jks` or `.p12`) |
| `CLEANCUT_KEYSTORE_PASSWORD` | Password for the keystore |
| `CLEANCUT_KEY_ALIAS` | Alias name of the signing key |
| `CLEANCUT_KEY_PASSWORD` | Password for the key alias |

If `CLEANCUT_KEYSTORE_PATH` is unset, running `./gradlew assembleRelease` safely builds an unsigned release APK without failing.

---

## 3. Building Release Artifacts

```bash
# Build Signed/Unsigned Release APK
./gradlew assembleRelease

# Build Android App Bundle (AAB) for Google Play
./gradlew bundleRelease
```

Release output locations:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## 4. ProGuard / R8 Obfuscation & Shrinking

CleanCut enables R8 code shrinking and resource minification in release mode (`isMinifyEnabled = true`).

Key ProGuard rules in `app/proguard-rules.pro` preserve:
- ML Kit Subject Segmentation internal dynamic loading classes
- Coroutines reflection entry points
- Jetpack Compose runtime metadata

---

## 5. Release Checklist

1. [ ] Ensure all unit and UI tests pass: `./gradlew test`
2. [ ] Update `versionCode` and `versionName` in `app/build.gradle.kts`
3. [ ] Update [CHANGELOG.md](../CHANGELOG.md) with new features, bug fixes, and breaking changes
4. [ ] Create and push a git tag:
   ```bash
   git tag -a v0.1.0 -m "Release v0.1.0"
   git push origin v0.1.0
   ```
5. [ ] Verify the GitHub Actions release workflow completes and generates release assets.
