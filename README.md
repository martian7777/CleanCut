<div align="center">

# ✂️ CleanCut — On-Device AI Background Remover

**Privacy-First • Ultra-Fast • High-Fidelity Background Isolation for Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![ML Kit](https://img.shields.io/badge/Google%20ML%20Kit-Subject%20Segmentation-FF6F00.svg?style=for-the-badge&logo=google&logoColor=white)](https://developers.google.com/ml-kit)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

---

**CleanCut** is a modern, enterprise-grade Android application that performs instant, full-resolution background removal directly on your device. Powered by **Google ML Kit Subject Segmentation**, CleanCut isolates subjects (people, pets, products, objects) in milliseconds with zero network bandwidth, zero subscription fees, and 100% data privacy.

</div>

---

## ⚡ The Problem vs. The Solution

### 🚨 The Problem

1. **Cloud & Privacy Risks**: Most background removal tools upload private photos to external third-party servers, creating data privacy and security vulnerabilities.
2. **Subscription & Paywalls**: Popular background removal services lock high-resolution exports behind monthly subscriptions or per-image API credits.
3. **Network Dependency & Latency**: Cloud-based tools fail when offline or on weak cellular connections and introduce multi-second latency for file uploads and downloads.
4. **Resolution Degradation**: Many mobile tools compress full-resolution camera photos (e.g. 48 MP down to 1 MP) during segmentation, permanently ruining photo quality.
5. **Limited Subject Recognition**: Basic mobile tools only segment human faces or selfies, failing completely on products, pets, plants, or household items.

---

### 💡 The Solution: CleanCut

CleanCut redefines mobile background removal by executing the entire segmentation pipeline **100% locally on device** using hardware-accelerated machine learning algorithms.

* **🔒 Absolute Privacy**: Zero bytes leave your device. Works completely offline.
* **💎 Full-Resolution Preservation**: Runs segmentation on an optimized low-res confidence matrix while compositing the cutout mask directly against the full-resolution source bitmap.
* **🤖 Multi-Subject Intelligence**: Powered by Google ML Kit's Subject Segmentation model to recognize human figures, animals, goods, and distinct objects.
* **⚡ Zero Subscription Costs**: Open-source, free forever, no cloud API tokens required.
* **🎨 Modern Jetpack Compose UI**: Built with Material 3 design tokens, edge-to-edge UI layout, fluid state management, and real-time visual progress feedback.

---

## 🏗 Architecture & Technical Design

CleanCut follows **Clean Architecture** principles and **Unidirectional Data Flow (UDF)** to achieve complete separation of concerns, high testability, and seamless UI state synchronization.

```
                  ┌──────────────────────────────────────────────┐
                  │                 Presentation                 │
                  │   CleanCutScreen  <-->  CleanCutViewModel    │
                  └──────────────────────┬───────────────────────┘
                                         │  CleanCutUiState (UDF)
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │                    Domain                    │
                  │ BackgroundRemover (Interface)                │
                  │ CompositeResult • BackgroundRemovalStage     │
                  └──────────────────────┬───────────────────────┘
                                         │  Injected via Dependency
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │                     Data                     │
                  │ MlKitBackgroundRemover • BitmapCompositor    │
                  │ MediaStoreExportRepo  • ImageDecodeUtils     │
                  └──────────────────────┬───────────────────────┘
                                         │
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │            On-Device ML Engine               │
                  │    Google ML Kit Subject Segmentation        │
                  └──────────────────────────────────────────────┘
```

### 🔬 High-Fidelity Hybrid Compositing Pipeline

To avoid heavy memory consumption while maintaining original camera image fidelity, CleanCut utilizes a **two-pass hybrid pipeline**:

```
[Original 48MP Photo] ──► 1. Downscale Copy (max 1024px) ──► [ML Kit Segmenter]
                                                                  │
                                                                  ▼
[Original 48MP Photo] ◄── 2. Bilinear Mask Upscaling ◄─────── [Raw Confidence Mask]
         │
         ▼
3. In-Place Bitmap Composite ──► [Full-Res Transparent PNG Export]
```

1. **Decode & Cache**: The selected Uri is safely copied into local application cache.
2. **Low-Res Segmentation Pass**: A downscaled copy (max long-edge 1024px) is generated and fed to ML Kit's `SubjectSegmenter`, extracting a 2D float confidence mask.
3. **High-Res Compositing Pass**: The low-res confidence mask is upscaled to full source dimensions via bilinear interpolation (`BitmapCompositor`).
4. **In-Place Alpha Mutation**: Alpha values of the full-resolution source bitmap are mutated in-place based on confidence thresholds, preventing redundant memory allocations and avoiding `OutOfMemoryError`.

---

## 🛠 Tech Stack

| Category | Technology | Purpose / Usage |
| :--- | :--- | :--- |
| **Language** | [Kotlin 1.9+](https://kotlinlang.org/) | 100% idiomatic Kotlin with Coroutines & Flow |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Modern declarative layout engine |
| **Design System** | [Material 3](https://m3.material.io/) | Dynamic dark/light color schemes & components |
| **On-Device AI** | [Google ML Kit Subject Segmentation](https://developers.google.com/ml-kit/vision/subject-segmentation) | Hardware-accelerated subject detection & mask extraction |
| **Architecture** | Clean Architecture + UDF | Layered decoupling (Presentation, Domain, Data) |
| **State Management**| `StateFlow` + `ViewModel` | Reactive, lifecycle-aware UI state management |
| **Image Handling** | Android Graphics Bitmap API + [Coil](https://coil-kt.github.io/coil/) | High-performance bitmap decoding, scaling, and async display |
| **Storage & I/O** | Android MediaStore API | Scoped storage transparent PNG export to Photos/Gallery |
| **Async & Parallel**| Kotlin Coroutines & `Tasks.await()` | Non-blocking background thread processing |
| **Build System** | Gradle Kotlin DSL (`build.gradle.kts`) | Type-safe build scripts & version catalog dependency management |

---

## ✨ Features

- [x] 🔮 **Instant On-Device AI Segmentation**: Removes background in under 1 second.
- [x] 🐶 **People & Object Detection**: Works on portraits, group photos, pets, vehicles, products, and still life.
- [x] 🎚 **Interactive Side-by-Side & Toggle Preview**: Switch between original photo and transparent cutout instantly.
- [x] 💾 **High-Res Transparent PNG Export**: Saves cutouts directly to Android `Pictures/CleanCut` directory via MediaStore API.
- [x] 🛡 **OOM-Protected Memory Management**: Intelligent downscaling and bitmap recycling prevents memory crashes on high-res camera captures.
- [x] 🌗 **Adaptive Edge-to-Edge Theme**: Supports system dark and light modes seamlessly.

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK**: Version 17+
- **Min SDK**: API Level 26 (Android 8.0 Oreo)
- **Target SDK**: API Level 34 / 35 (Android 14 / 15)

### Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/CleanCut.git
   cd CleanCut
   ```

2. **Open in Android Studio**:
   Open Android Studio -> **File** -> **Open** -> Select `CleanCut` folder.

3. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📂 Project Structure

```text
CleanCut/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/cleancut/bgremover/
│   │   │   ├── MainActivity.kt                      # Main Activity entry point
│   │   │   ├── core/
│   │   │   │   ├── common/                          # Image decoding & Uri helper utilities
│   │   │   │   └── designsystem/                    # Material 3 colors, typography, theme
│   │   │   └── feature/bgremoval/
│   │   │       ├── data/                            # ML Kit Segmenter & MediaStore implementations
│   │   │       ├── domain/                          # Models, interfaces, and error domain types
│   │   │       ├── presentation/                    # CleanCutViewModel & CleanCutUiState
│   │   │       └── ui/                              # Compose screens & preview components
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── docs/                                            # Project architectural guardrails
└── build.gradle.kts
```

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for full details.

<div align="center">
  <sub>Built with ❤️ for privacy and high performance on Android.</sub>
</div>
