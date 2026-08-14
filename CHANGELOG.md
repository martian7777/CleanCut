# Changelog

All notable changes to **CleanCut** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-08-14

### Added
- **On-Device AI Subject Segmentation**: Integrated Google ML Kit Subject Segmentation model for instant local background removal.
- **High-Fidelity Compositing Pipeline**: Two-pass segmentation system with low-res ML inference and bilinear mask upscaling against full-resolution source bitmap.
- **Jetpack Compose UI**: Modern Material 3 user interface with dynamic theme support, edge-to-edge layout, and smooth animations.
- **Preview & Comparison Controls**: Interactive side-by-side and toggle views between original photo and segmented cutout.
- **MediaStore Scoped Storage Export**: Transparent PNG export directly to device `Pictures/CleanCut` directory.
- **OOM Protection & Optimization**: Memory-efficient in-place alpha mutation and bitmap downscaling safeguards.
- **CI/CD Pipeline**: GitHub Actions workflow for automated Gradle builds, test validation, and artifact generation.
