# 🏗 CleanCut Architecture Specification

## 1. Architectural Philosophy

CleanCut is built using **Clean Architecture** principles combined with **Unidirectional Data Flow (UDF)**. The core philosophy is to keep domain and business rules independent of external frameworks, libraries, and UI mechanics, ensuring high maintainability, testability, and portability.

---

## 2. Layered Breakdown

```text
com.cleancut.bgremover/
├── core/
│   ├── common/             # Pure utility functions (Image decoders, Uri helpers, Safe math)
│   └── designsystem/       # Material 3 design tokens (Theme, Colors, Typography, Shapes)
└── feature/bgremoval/
    ├── domain/             # Business contracts, interfaces, state models
    ├── data/               # ML Kit, Bitmap processing, and MediaStore implementations
    ├── presentation/       # Jetpack Compose ViewModel and UDF StateFlow contracts
    └── ui/                 # Stateless Jetpack Compose screens, widgets, and previews
```

### 2.1 Domain Layer (`feature/bgremoval/domain`)
- **Responsibility**: Houses enterprise business rules, entity models, and interface contracts.
- **Rules**:
  - Pure Kotlin only.
  - Zero dependencies on Android framework classes (no `android.graphics.Bitmap`, no `android.content.Context` in domain interfaces where possible).
  - Defines `BackgroundRemover` interface, `CompositeResult`, and `BackgroundRemovalStage`.

### 2.2 Data Layer (`feature/bgremoval/data`)
- **Responsibility**: Implements repository and worker contracts defined in the domain layer.
- **Components**:
  - `MlKitBackgroundRemover`: Handles Google ML Kit Subject Segmentation model lifecycle, execution, and error mapping.
  - `BitmapCompositor`: High-performance graphics processing, bilinear confidence mask scaling, and in-place alpha compositing.
  - `MediaStoreExportRepo`: Interacts with Android's MediaStore content provider for scoped storage PNG export.

### 2.3 Presentation & UI Layer (`feature/bgremoval/presentation` & `feature/bgremoval/ui`)
- **Responsibility**: Renders UI states and emits user intents.
- **Pattern**: Unidirectional Data Flow (UDF):
  - `CleanCutViewModel` exposes immutable `StateFlow<CleanCutUiState>`.
  - UI emits user actions (`onImageSelected`, `onRemoveBackground`, `onSaveCutout`, `onTogglePreviewMode`) to the ViewModel.
  - Composables observe state changes and recompose efficiently.

---

## 3. Unidirectional Data Flow (UDF)

```text
┌──────────────┐      User Action / Intent      ┌───────────────────────┐
│              │ ─────────────────────────────► │                       │
│  UI Screen   │                                │  CleanCutViewModel    │
│  (Compose)   │ ◄───────────────────────────── │                       │
└──────────────┘    Immutable StateFlow State   └───────────────────────┘
                                                           │
                                                           │ Executes Coroutine
                                                           ▼
                                                ┌───────────────────────┐
                                                │  Domain / Data Layer  │
                                                │  (ML Kit & Pipeline)  │
                                                └───────────────────────┘
```

---

## 4. Error Handling Strategy

Errors in CleanCut are categorized into domain-level sealed hierarchies:
- **`SegmentationError.ModelUnavailable`**: Triggered when ML Kit model cannot be downloaded or initialized.
- **`SegmentationError.ImageTooLarge`**: Safeguard triggered when source dimensions exceed hardware processing limits.
- **`SegmentationError.NoSubjectFound`**: Triggered when no distinct subject/foreground can be isolated.
- **`ExportError.StoragePermissionDenied` / `ExportError.DiskFull`**: MediaStore export failures.

All errors are mapped cleanly to user-friendly messages and snackbar alerts in the UI.
