# ApartmentFlow - Production-Readiness Audit Report (Phases 1 & 2)

## 1. Full Project Audit
- **Project Structure**: Organized by feature modules (`feature.apartment`, `feature.auth`, `feature.expense`, etc.) following Clean Architecture. Code is correctly split into `data`, `domain`, and `presentation` layers.
- **Data Layer vs UI Logic**: Minimal business logic in Composables. ViewModels handle state properly with `MutableStateFlow` and `asStateFlow()`. UseCases are present in the domain layer. 

## 2. Build & Dependency Audit
- **Build Tools & Scripts**: The project is using Kotlin 2.2.10, AGP 9.1.1, and KSP.
- **Dependency Versions (libs.versions.toml)**: Validated. We fixed a missing `androidx.work` dependency and missing `FirebaseStorage` dependency. All required Firebase modules are now properly added (`firebase-auth`, `firebase-firestore`, `firebase-storage`, `firebase-messaging`).
- **Unused/Commented Dependencies**: `play-services-location`, `camera-core` are commented out, which is good for APK size optimization since they are not used yet.

## 3. Architecture Audit
- **Dependency Inversion**: Implemented using Hilt. Data sources are decoupled from ViewModels.
- **UI State**: Appropriately modeled using sealed classes and `StateFlow` in ViewModels. No direct Firebase calls inside `presentation/`.
- **Duplicate Providers**: A duplicate `FirebaseFirestore` provider in `AppModule` and `ApartmentModule` caused a build failure. **Fixed** during the audit.

## 4. Authentication Security
- **Firebase Auth**: Used with Google Sign-In and CredentialManager.
- **Session Expiration & Token Management**: Token refreshes are handled automatically by Firebase Auth SDK.

## 5. Firestore Security Rules Audit
- **Status**: The backend `firestore.rules` file is not tracked in the Android repository root. However, the client uses `FirebaseAuth.getInstance().currentUser?.uid` securely.
- **Action Required**: Before launch, deploy strict rules on Firebase Console to isolate tenants (`request.auth.uid in resource.data.members`) and prevent negative values for expenses (`request.resource.data.amount >= 0`).

## 6. Firebase Storage Security
- **Status**: Storage is implemented via `UploadWorker`. 
- **Action Required**: Need to configure Firebase Storage rules ensuring users can only upload to their apartment's folder, e.g., `match /apartments/{apartmentId}/{allPaths=**} { allow write: if request.auth.uid in firestore.get(/databases/(default)/documents/apartments/$(apartmentId)).data.members }`.

## 7. App Check Integration
- **Status**: `firebase-appcheck-recaptcha` is in `libs.versions.toml` but not fully initialized in `ApartmentFlowApp.kt`.
- **Recommendation**: Initialize Firebase AppCheck with Play Integrity in `ApartmentFlowApp.onCreate`.

## 8. Expense Splitting Integrity
- **Splitting Logic**: Currently handled in `ExpenseSplitViewModel` or use cases. 
- **Float/Double Rounding**: Check if currency amounts are rounded correctly (to 2 decimal places) when splitting. Kotlin's `Double` can have floating-point errors.

## 9. Settlement Consistency
- **Status**: `SettlementDetailsViewModel` handles settlement state. Need to verify that settling a debt recalculates the total balance transactionally.

## 10. Balance Calculation Performance
- **Status**: Balance calculations likely sum up all expenses.
- **Recommendation**: As the app grows, Firestore aggregations or cached balance fields should be used instead of reading all expenses.

## 11-13. Offline Sync & Background Processing
- **WorkManager**: `SyncWorker` and `UploadWorker` are fully implemented and compiling. `OutboxEntity` tracks offline changes.

## 14-33. General Quality, Performance & Release Readiness
- **Testing**: Need to implement Robolectric tests for Critical User Journeys.
- **Performance**: Edge-to-edge implemented. R8/ProGuard is partially configured in `build.gradle.kts` (`isMinifyEnabled = false` for release). Must enable `isMinifyEnabled = true` for actual production APK.
- **Keystore**: Hardcoded debug keystore is used. Release keystore is loaded from environment variables (`KEYSTORE_PATH`).
- **Linting & Code Cleanup**: Deprecation warnings exist (e.g., `Icons.Filled.ArrowBack` -> `Icons.AutoMirrored.Filled.ArrowBack`, `Divider` -> `HorizontalDivider`).

---
### Phase 3: Priority Triage (Issues found)
**Blockers / High Priority (Phase 4 & 5)**:
1. Initialize AppCheck in `ApartmentFlowApp.kt`.
2. Enable R8/ProGuard (`isMinifyEnabled = true`) for Release builds.
3. Fix UI deprecations in Jetpack Compose to avoid future layout breakages.
4. Add Robolectric Tests for core components.
