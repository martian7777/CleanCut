# Contributing to CleanCut

Thank you for your interest in contributing to **CleanCut**! CleanCut is dedicated to providing a blazing-fast, privacy-first, on-device background removal tool for Android.

We welcome contributions of all kinds: bug fixes, new features, UI/UX polish, documentation improvements, and performance optimizations.

---

## 📋 Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How to Contribute](#how-to-contribute)
   - [Reporting Bugs](#reporting-bugs)
   - [Suggesting Features](#suggesting-features)
   - [Submitting Pull Requests](#submitting-pull-requests)
3. [Development Setup](#development-setup)
4. [Architecture & Guidelines](#architecture--guidelines)
5. [Code Style & Conventions](#code-style--conventions)
6. [Testing & Verification](#testing--verification)
7. [Git & Commit Workflow](#git--commit-workflow)

---

## Code of Conduct

All contributors and maintainers are expected to adhere to our [Code of Conduct](CODE_OF_CONDUCT.md). Please treat everyone with respect and empathy.

---

## How to Contribute

### Reporting Bugs

Before creating an issue, please verify that the bug has not already been reported:
- Check existing [GitHub Issues](https://github.com/martian7777/CleanCut/issues).
- Use the **Bug Report** issue template.
- Include device details (e.g., Google Pixel 8, Samsung Galaxy S23), Android OS version, reproduction steps, expected behavior, and logcat output if applicable.

### Suggesting Features

We love ideas! To propose a new feature:
- Check existing issues/discussions to ensure it's not already in progress.
- Open an issue using the **Feature Request** template.
- Clearly articulate the problem the feature solves and its proposed user experience.

### Submitting Pull Requests

1. **Fork the repository** and create your branch from `main`:
   ```bash
   git checkout -b feature/my-amazing-feature
   ```
2. **Make your changes** following the code standards and architecture.
3. **Run local checks and tests**:
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```
4. **Commit with descriptive messages** following Conventional Commits.
5. **Push to your fork** and submit a Pull Request targeting `main`.
6. **Fill out the Pull Request template** completely.

---

## Development Setup

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK**: Java 17+
- **Android SDK**: API Level 35 (compileSdk), API Level 26 (minSdk)
- **Google Play Services**: ML Kit Subject Segmentation model downloads require Google Play Services on test devices/emulators.

### Building Locally

```bash
# Clone the repository
git clone https://github.com/martian7777/CleanCut.git
cd CleanCut

# Assemble debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## Architecture & Guidelines

CleanCut adheres strictly to **Clean Architecture** and **Unidirectional Data Flow (UDF)**:

- **Presentation Layer** (`feature/bgremoval/presentation` & `feature/bgremoval/ui`):
  - Jetpack Compose UI components.
  - State managed via `CleanCutViewModel` exposing `StateFlow<CleanCutUiState>`.
  - UI components must remain stateless and decoupled wherever possible.
- **Domain Layer** (`feature/bgremoval/domain`):
  - Pure Kotlin definitions (interfaces, models, error types).
  - No direct dependencies on Android framework or UI classes.
- **Data Layer** (`feature/bgremoval/data`):
  - Concrete implementations of domain interfaces (e.g., `MlKitBackgroundRemover`, `MediaStoreExportRepo`, `BitmapCompositor`).
  - Safe memory handling: Bitmap operations must guard against `OutOfMemoryError`.

---

## Code Style & Conventions

- **Kotlin Idioms**: Write clean, expressive, and idiomatic Kotlin. Prefer `val` over `var`, data classes for state, and sealed interfaces for domain events.
- **Coroutines & Flow**: Use structured concurrency. Offload heavy computation (ML inference, bitmap decoding, matrix math) to `Dispatchers.Default` or `Dispatchers.IO`.
- **Jetpack Compose**:
  - Follow Compose API guidelines.
  - Name composables as nouns (e.g., `CleanCutScreen`, `PreviewComparisonView`).
  - Provide preview annotations (`@Preview`) for reusable UI widgets.
  - Use Material 3 theming tokens (`MaterialTheme.colorScheme`, `MaterialTheme.typography`).
- **Formatting**: Adhere to official Kotlin style guide. Maintain clean indentation (4 spaces).

---

## Testing & Verification

- **Unit Tests**: Place domain and utility tests in `app/src/test/`.
- **Instrumentation Tests**: Place Compose and integration tests in `app/src/androidTest/`.
- Verify memory stability on large images (e.g., 20+ megapixel photos) to ensure no Out-Of-Memory regressions.

---

## Git & Commit Workflow

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```text
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

### Types:
- `feat`: A new feature
- `fix`: A bug fix
- `perf`: A code change that improves performance
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `docs`: Documentation only changes
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to build process, dependency updates, or auxiliary tools

### Examples:
- `feat(segmentation): add multi-subject selection support`
- `fix(compositor): prevent memory overflow on 48MP photos`
- `docs(readme): update setup instructions for JDK 17`

---

## Recognition & Thank You

Every contribution matters. Thank you for helping make CleanCut better!
