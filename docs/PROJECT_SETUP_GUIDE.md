# ApartmentFlow: Project Foundation & Setup Guide

**Prepared By:** Senior Android Engineer & Mobile Software Architect
**Project:** ApartmentFlow
**Date:** August 2026

This document serves as the foundational guide for setting up the ApartmentFlow Android project. It outlines the precise configuration required before any feature development begins, ensuring a scalable, secure, and maintainable architecture.

---

## 1. Android Studio Project Creation

When creating the new project in Android Studio, select the following options:

*   **Template:** **Empty Activity** (This provides a clean slate with Jetpack Compose configured).
*   **Name:** `ApartmentFlow`
*   **Package name:** `com.apartmentflow.app` (See Section 2).
*   **Save location:** Your preferred local directory.
*   **Language:** **Kotlin**
*   **Minimum SDK:** **API 26: Android 8.0 (Oreo)** (Covers >95% of active devices while allowing modern APIs without excessive legacy compat code).
*   **Build configuration language:** **Kotlin DSL (build.gradle.kts)** (Modern, type-safe, and standard for new projects).

## 2. Package Naming Convention

**Recommended Package Name:** `com.apartmentflow.app`

*   **Prefix (`com.`):** Standard commercial domain prefix.
*   **Project (`apartmentflow`):** The name of the project, all lowercase, no spaces or special characters.
*   **Suffix (`.app` or nothing):** `.app` is useful to distinguish the client application from potential future modules (e.g., `com.apartmentflow.core`).

## 3. Recommended Gradle Project Structure

The modern Android build system is split into multiple files. You should structure your project understanding these distinct roles:

*   **`settings.gradle.kts`:** Defines the project name and includes modules (e.g., `:app`). Also defines plugin management and repository resolution (Google, MavenCentral).
*   **`gradle/libs.versions.toml`:** The **Version Catalog**. The single source of truth for all dependency versions, libraries, and plugins across the project.
*   **`build.gradle.kts` (Project Level):** Defines plugins applied to the entire project (via the `alias` function referencing the version catalog) but with `apply = false`.
*   **`app/build.gradle.kts` (App/Module Level):** Defines Android-specific configuration (SDK versions, build types, signing configs) and applies the actual dependencies (implementations) needed for the `app` module.

## 4. Creating and Configuring a Firebase Project

1.  Navigate to the [Firebase Console](https://console.firebase.google.com/).
2.  Click **Add project** -> Name it `ApartmentFlow`.
3.  **Enable Google Analytics** for this project (Required for Crashlytics and Analytics).
4.  Choose/Create a Google Analytics account.
5.  Click **Create Project**.

## 5. Connecting Android Studio with Firebase

1.  In the Firebase Console, on the Project Overview page, click the **Android icon** to add an app.
2.  **Android package name:** `com.apartmentflow.app`
3.  **App nickname:** `ApartmentFlow (Android)`
4.  **Debug signing certificate SHA-1:** (Optional for now, but required later for Google Sign-In and Dynamic Links. Generate via `./gradlew signingReport`).
5.  Click **Register app**.

## 6. Downloading and Placing `google-services.json`

1.  After registering the app in the Firebase console, click **Download google-services.json**.
2.  Switch to the **Project** view in the Android Studio file explorer (top left dropdown).
3.  Expand the project root and locate the `app` folder.
4.  Drag and drop or copy-paste the downloaded `google-services.json` directly inside the `app/` directory (e.g., `ApartmentFlow/app/google-services.json`).

## 7. Configuring Firebase Services

In the Firebase Console side-menu, configure the following:

*   **Authentication:** Go to Build -> Authentication -> Get Started. Enable **Email/Password** and **Google** sign-in providers.
*   **Firestore Database:** Go to Build -> Firestore Database -> Create Database. Start in **Test mode** (or Production mode with basic rules granting access to authenticated users). Set the location to your target demographic region.
*   **Storage:** Go to Build -> Storage -> Get Started. Start in Test mode. Set the region (usually matching Firestore).
*   **Crashlytics:** Go to Release & Monitor -> Crashlytics -> Enable. (Will activate once the app runs with the dependency).
*   **Cloud Messaging:** Go to Engage -> Messaging. (FCM is enabled by default when you add the Firebase project).

## 8. Required Gradle Dependencies & Explanations

Here are the categories of dependencies needed for this stack:

*   **Core / Compose:** `core-ktx`, `lifecycle-runtime-ktx`, `activity-compose`, Compose BOM, Material 3, UI Tooling. (Foundation of the UI).
*   **Navigation:** `navigation-compose` (For routing between screens).
*   **Dependency Injection (Hilt):** `hilt-android`, `hilt-navigation-compose`, `hilt-compiler`. (For scalable dependency injection).
*   **Firebase BoM:** `firebase-bom` (Ensures all Firebase libraries use compatible versions).
*   **Firebase Services:** `firebase-auth`, `firebase-firestore-ktx`, `firebase-storage-ktx`, `firebase-messaging-ktx`, `firebase-analytics-ktx`, `firebase-crashlytics-ktx`. (Backend services).
*   **Local Data:** `room-runtime`, `room-ktx`, `room-compiler` (Offline database), `datastore-preferences` (Key-value storage for settings).
*   **Networking:** `retrofit`, `converter-moshi` or `converter-gson`, `okhttp`, `logging-interceptor`. (For any non-Firebase APIs, if required in the future).
*   **Image Loading:** `coil-compose`. (Modern, Coroutine-based image loading).
*   **Coroutines:** `kotlinx-coroutines-android`, `kotlinx-coroutines-play-services`. (Async programming and Firebase Task interop).

## 9. Version Catalog (`libs.versions.toml`)

Create or update `gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.3.0"
kotlin = "1.9.22"
coreKtx = "1.12.0"
lifecycleRuntimeKtx = "2.7.0"
activityCompose = "1.8.2"
composeBom = "2024.02.01"
navigationCompose = "2.7.7"
hilt = "2.51"
hiltNavigationCompose = "1.2.0"
firebaseBom = "32.7.2"
room = "2.6.1"
dataStore = "1.0.0"
coil = "2.6.0"
retrofit = "2.9.0"
okhttp = "4.12.0"
coroutines = "1.8.0"
ksp = "1.9.22-1.0.17"
googleServices = "4.4.1"
crashlyticsPlugin = "2.9.9"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }
firebase-storage = { group = "com.google.firebase", name = "firebase-storage" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }

# Room & DataStore
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "dataStore" }

# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "crashlyticsPlugin" }
```

## 10. Required Gradle Plugins

To wire everything together, your `app/build.gradle.kts` needs specific plugins:

*   `com.android.application`: The base plugin to build an Android app.
*   `org.jetbrains.kotlin.android`: Kotlin language support.
*   `com.google.devtools.ksp`: Kotlin Symbol Processing. Replaces `kapt`. Required for fast code generation for Room and Hilt.
*   `com.google.dagger.hilt.android`: Injects the Hilt compiler and Gradle tasks.
*   `com.google.gms.google-services`: Processes `google-services.json` and wires Firebase securely.
*   `com.google.firebase.crashlytics`: Uploads mapping files to Firebase so crash reports are readable.

## 11. ProGuard Configuration

In `app/proguard-rules.pro`, you must retain certain classes that rely on reflection or annotation processing. Example baseline for this stack:

```proguard
# Retrofit / Gson
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firebase/Firestore models (Keep your domain models)
-keep class com.apartmentflow.app.domain.model.** { *; }

# Hilt/Dagger (Auto-handled mostly, but good practice to keep @Inject)
-keep @javax.inject.Inject class *
```

## 12. Build Types

In `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-DEBUG"
        isDebuggable = true
    }
    release {
        isMinifyEnabled = true // Enable ProGuard
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        // signingConfig = signingConfigs.getByName("release")
    }
}
```

## 13. AndroidManifest.xml Configuration

Add the necessary permissions inside `<manifest>` but before `<application>`:

```xml
<!-- Internet & Network (Firebase, API calls) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Cloud Messaging (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Image Selection (Profile pictures, receipts) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```
*(Note: Storage permissions change drastically between Android 10, 11, and 13. Rely on `READ_MEDIA_IMAGES` for API 33+ and standard read access for legacy).*

## 14. Recommended Folder/Package Structure

Before writing code, create this exact package structure under `src/main/java/com/apartmentflow/app/`:

```text
com.apartmentflow.app
│
├── ApartmentFlowApp.kt       // Application class (annotated with @HiltAndroidApp)
│
├── di                        // Hilt Modules
│   ├── AppModules.kt
│   ├── FirebaseModule.kt
│   └── DatabaseModule.kt
│
├── domain                    // Pure Kotlin (No Android dependencies)
│   ├── model                 // Entities (User, Expense, Chore)
│   ├── repository            // Interfaces (AuthRepository, ExpenseRepository)
│   └── usecase               // Business Logic (CalculateSplitUseCase)
│
├── data                      // Implementations
│   ├── local                 // Room DAOs, DataStore, AppDatabase
│   ├── remote                // Firebase implementations, Retrofit APIs
│   ├── repository            // Implementations of domain/repository interfaces
│   └── mapper                // DTO <-> Domain mapping
│
└── presentation              // UI Layer
    ├── theme                 // Type, Color, Shape, Theme definitions
    ├── navigation            // NavGraphs, Routes
    ├── core                  // Reusable UI components (PrimaryButton, TopBar)
    ├── auth                  // Login, Signup (Screens + ViewModels)
    ├── dashboard             // Main overview
    ├── expense               // Adding/Viewing expenses
    └── chore                 // Adding/Viewing chores
```

## 15. Project Setup Verification Checklist

- [ ] Android Studio project created with API 26 minimum and Kotlin DSL.
- [ ] `google-services.json` placed in the `/app` directory.
- [ ] `libs.versions.toml` fully configured with latest stable dependencies.
- [ ] Project-level `build.gradle.kts` configured with all plugins (apply = false).
- [ ] App-level `build.gradle.kts` applies all plugins, implements all dependencies, and enables `buildFeatures { compose = true }`.
- [ ] Firebase project created and all required services initialized in the console.
- [ ] Debug and Release build types configured.
- [ ] `AndroidManifest.xml` updated with INTERNET and POST_NOTIFICATIONS permissions.
- [ ] Base folder structure created (`di`, `data`, `domain`, `presentation`).
- [ ] Custom Application class created and annotated with `@HiltAndroidApp`, and registered in `AndroidManifest.xml`.
- [ ] Project builds successfully (`./gradlew assembleDebug` passes with no errors).

**End of Document.**
You are now ready to begin Feature Development starting with the Domain Layer.
