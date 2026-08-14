# 🛠 Development & Setup Guide

This guide walks you through setting up your local environment for CleanCut development.

---

## 1. System Requirements

- **Operating System**: macOS (Apple Silicon or Intel), Windows 10/11, or Linux (Ubuntu 22.04+ recommended)
- **Memory**: 16 GB RAM minimum (32 GB recommended for Android Studio + Emulators)
- **JDK**: Java Development Kit 17 (Eclipse Temurin or OpenJDK 17)
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer

---

## 2. Environment Setup

### 2.1 Install JDK 17
Ensure `JAVA_HOME` points to your JDK 17 installation:

```bash
java -version
# Expected: openjdk version "17.x.x"
```

### 2.2 Android SDK & Platform Tools
Make sure the following SDK components are installed via Android Studio SDK Manager:
- **Android SDK Platform 35** (Vanilla Ice Cream)
- **Android SDK Build-Tools 35.0.0**
- **Android SDK Platform-Tools**
- **Google Play Services** (required for ML Kit Subject Segmentation)

---

## 3. Cloning & Building

```bash
# 1. Clone the repository
git clone https://github.com/martian7777/CleanCut.git
cd CleanCut

# 2. Check build configuration
./gradlew tasks

# 3. Compile and build Debug APK
./gradlew assembleDebug

# 4. Run Unit Tests
./gradlew test
```

---

## 4. Emulator & Physical Device Testing

### Using an Emulator:
- Create an AVD (Android Virtual Device) with **Google Play Store image** (API 34 or 35).
- **Important**: ML Kit Subject Segmentation requires Google Play Services to download the on-device ML model during first use. Emulators without Google Play Store images will fail ML initialization.

### Using a Physical Device:
- Enable **Developer Options** and **USB Debugging** on your device.
- Connect your device via USB or Wi-Fi ADB.
- Run:
  ```bash
  ./gradlew installDebug
  ```

---

## 5. Common Troubleshooting

### Issue: `Execution failed for task ':app:compileDebugKotlin'`
- **Fix**: Verify JDK 17 is active. Check `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK` in Android Studio.

### Issue: ML Kit Segmentation returns model download failure
- **Fix**: Ensure the test device/emulator is connected to the internet on first run so Google Play Services can download the on-device Subject Segmentation model binary (~15MB). Subsequent executions run 100% offline.
