# 🧪 Testing & Quality Assurance Strategy

## 1. Testing Philosophy

CleanCut follows the **Test Pyramid** strategy with strong emphasis on:
1. **Fast, deterministic Unit Tests** for Domain models, UDF ViewModels, and Bitmap algorithms.
2. **Instrumentation Tests** for Jetpack Compose UI rendering and Android MediaStore interaction.
3. **Memory & Stress Benchmarks** to prevent `OutOfMemoryError` regressions on high-resolution camera images.

---

## 2. Test Architecture

```text
app/src/
├── test/                          # Local JVM Unit Tests (Robolectric / JUnit 4 & 5 / MockK / Turbine)
│   └── kotlin/com/cleancut/bgremover/
│       ├── core/common/           # Math & Image Decode utility unit tests
│       └── feature/bgremoval/
│           ├── domain/            # State & contract tests
│           └── presentation/      # ViewModel & StateFlow Turbine tests
└── androidTest/                   # On-Device Instrumentation Tests
    └── kotlin/com/cleancut/bgremover/
        └── feature/bgremoval/ui/  # Jetpack Compose UI & interaction tests
```

---

## 3. Running Tests

### 3.1 Unit Tests (Local JVM)
```bash
./gradlew test
```

### 3.2 Instrumentation Tests (Device/Emulator)
```bash
./gradlew connectedAndroidTest
```

### 3.3 Static Analysis & Lint Checks
```bash
./gradlew lintDebug
```

---

## 4. Key Testing Areas

### 4.1 ViewModel & StateFlow Testing with Turbine
Verify that `CleanCutViewModel` emits expected state transitions (`Idle` -> `Processing` -> `Segmented` or `Error`) when actions are dispatched.

```kotlin
@Test
fun `when image selected and remove background triggered, state transitions to Segmented`() = runTest {
    viewModel.uiState.test {
        assertEquals(CleanCutUiState.Idle, awaitItem())
        
        viewModel.onImageSelected(testUri)
        // Verify state updates
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 4.2 Bitmap Compositor & Edge Case Testing
- Verify bilinear interpolation across mismatched aspect ratios.
- Verify alpha masking with extreme confidence values (all 0.0f, all 1.0f, partial gradients).
- Verify in-place memory mutation does not cause pixel corruption.

### 4.3 Memory Leak & OOM Verification
- Test batch background removal on 10+ consecutive 48 MP photos to confirm native bitmap memory is reclaimed by the garbage collector.
