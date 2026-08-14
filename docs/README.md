# 📚 CleanCut Technical Documentation

Welcome to the **CleanCut** developer and engineering documentation hub. This directory contains in-depth technical guides, architectural specifications, ML pipeline documentation, and setup instructions.

---

## 📑 Documentation Index

| Guide | Description | Target Audience |
| :--- | :--- | :--- |
| 🏗 [Architecture Guide](architecture.md) | In-depth breakdown of Clean Architecture, UDF state flow, and layer responsibilities. | All Developers |
| 🔮 [ML Pipeline & Compositing](ml-pipeline.md) | Technical deep-dive into Google ML Kit Subject Segmentation, bilinear interpolation, and OOM-safe in-place alpha compositing. | ML & Graphics Engineers |
| 🛠 [Setup & Development Guide](setup.md) | Workstation setup, emulator configuration, Gradle build commands, and troubleshooting. | New Contributors |
| 🧪 [Testing & QA Strategy](testing.md) | Unit test strategy, coroutine testing, Compose UI testing, and memory regression benchmarks. | QA & Engineers |
| 🚀 [Release & Deployment](release.md) | Versioning strategy, release keystore configuration, ProGuard rules, and automated CI publishing. | Maintainers & DevOps |

---

## 🗺 System High-Level Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                      UI (Jetpack Compose)                   │
│   CleanCutScreen • PreviewComparisonView • ProgressIndicator│
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    CleanCutViewModel (UDF)                  │
│       Manages UI states: Idle, Loading, Segmented, Error    │
└──────────────────────────────┬──────────────────────────────┘
                               │ Domain Interface
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer (Pure Kotlin)               │
│ BackgroundRemover • CompositeResult • BackgroundRemovalStage│
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌──────────────────────────────┐    ┌──────────────────────────────┐
│  MlKitBackgroundRemover      │    │  MediaStoreExportRepo        │
│  • ML Kit Subject Segmenter  │    │  • MediaStore API            │
│  • Low-res Inference Pass    │    │  • Scoped Storage PNG Export │
│  • Bilinear Mask Upscaling   │    │  • Lossless Quality Write    │
│  • In-Place Alpha Compositor │    └──────────────────────────────┘
└──────────────────────────────┘
```
