# Contributing to ApartmentFlow

Thank you for your interest in contributing to **ApartmentFlow**! We welcome contributions from developers of all skill levels.

This document provides a comprehensive guide on setting up your development environment, testing locally, and submitting pull requests.

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before contributing.

---

## Prerequisites

Before building ApartmentFlow locally, ensure you have the following installed:

- **JDK 17 or higher** (JDK 17 recommended)
- **Android Studio** (Ladybug / Koala or newer recommended)
- **Android SDK Platform 36** (Build-Tools 36.0.0)
- **Git**

---

## Getting Started: Local Setup

### 1. Fork and Clone the Repository

1. Fork the repository on GitHub by clicking the **Fork** button at the top right of the repo page.
2. Clone your fork locally:

```bash
git clone https://github.com/YOUR_USERNAME/ApartmentFlow.git
cd ApartmentFlow
```

### 2. Configure Environment Secrets (`.env`)

ApartmentFlow uses the [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) to inject configuration parameters at build time.

Copy the example environment file to `.env`:

```bash
cp .env.example .env
```

The default values in `.env.example` are pre-configured with safe dummy placeholders:

```properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
WEB_CLIENT_ID=YOUR_OAUTH_WEB_CLIENT_ID.apps.googleusercontent.com
FIREBASE_API_KEY=AIzaSyDemoApiKeyForApartmentFlow00000
```

### 3. Configure Google Services (`google-services.json`)

To sync Gradle and build the project locally without production Firebase credentials:

Copy `mock-google-services.json` to `app/google-services.json`:

```bash
cp mock-google-services.json app/google-services.json
```

---

## Firebase Authentication & Local Testing Guidelines

ApartmentFlow uses Firebase Authentication (Google Sign-In, Email/Password) and Firestore for live data sync.

### Local Testing & Firebase Auth Bypass

To enable local development without needing live production Firebase credentials:

1. **Automatic Initialization Fallback**:
   - `FirebaseInitializer` includes an automatic fallback initialization mechanism. If `google-services.json` contains dummy credentials, `FirebaseApp` initializes safely in fallback mode and catches initialization notices gracefully.

2. **Google Sign-In Requirements**:
   - When using Google Sign-In, the app expects a valid Google OAuth Web Client ID set in `WEB_CLIENT_ID` inside your local `.env`.
   - If testing Google Sign-In against your own Firebase project, create a Firebase project, add an Android app with package name `com.aistudio.apartmentflow.lfsqkx` (or `com.example`), download its `google-services.json`, and set your OAuth Web Client ID in `.env`.

3. **Firebase Emulator Suite (Optional)**:
   - For offline testing of Auth and Firestore without hitting live cloud servers, you can start the Firebase Local Emulator:
     ```bash
     firebase emulators:start
     ```
   - Standard unit tests in `app/src/test/` run in JVM using Robolectric and local mock repositories without requiring live Firebase endpoints.

---

## Development Workflow & Verification

Before opening a Pull Request, please ensure your code compiles and passes all checks locally.

### Build the Debug APK

```bash
./gradlew assembleDebug
```

### Run Static Analysis (Lint)

```bash
./gradlew lint
```

### Run Unit Tests

```bash
./gradlew test
```

---

## Submitting Pull Requests

1. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Commit Your Changes**:
   Follow clear commit messaging guidelines (e.g. `feat: add balance filtering option`).
3. **Push to Your Fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
4. **Open a Pull Request**:
   - Open a PR targeting the `main` branch.
   - Complete the [Pull Request Template](.github/pull_request_template.md).
   - Ensure all automated GitHub Actions CI checks pass.

Thank you for helping make ApartmentFlow better for everyone!
