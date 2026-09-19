# ApartmentFlow

[![Android CI](https://github.com/USER/REPO/actions/workflows/android-ci.yml/badge.svg)](https://github.com/USER/REPO/actions/workflows/android-ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-36-green.svg)](https://developer.android.com)

Welcome to **ApartmentFlow**, a modern open-source Android application designed to simplify shared apartment living! ApartmentFlow helps roommates track shared expenses, automate settlement workflows, balance member accounts, and manage household activities seamlessly.

We warmly welcome community developers to contribute! Whether you're fixing bugs, improving UI components, writing unit tests, or adding new features, check out our [Contribution Guide](CONTRIBUTING.md) to get started.

---

## 🚀 Key Features

- 💵 **Expense Tracking & Splitting**: Log joint household expenses, split bills among roommates, and automatically update individual balance summaries.
- 🤝 **Settlement Workflow**: Debtor-initiated settlements with creditor review and acceptance/rejection flow.
- ⚖️ **Balance Reconciliation**: Real-time balance calculations ensuring data consistency and preventing negative double-counting.
- 🔒 **Biometric & Security Controls**: Security manager options for app lock, local encrypted database sync, and Firebase security rules.
- 📱 **Modern Jetpack Compose UI**: Dynamic Material 3 design system with full dark mode support and responsive UI components.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Local Storage & Sync**: [Room Database](https://developer.android.com/training/data-storage/room) & DataStore Preferences
- **Backend & Cloud**: [Firebase Firestore](https://firebase.google.com/docs/firestore), [Firebase Auth](https://firebase.google.com/docs/auth), Firebase Storage
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Architecture**: Clean Architecture (Data, Domain, Presentation) with Unidirectional Data Flow (UDF)

---

## 💻 Quick Start & Local Setup

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/ApartmentFlow.git
cd ApartmentFlow
```

### 2. Set Up Local Secrets

Copy the example environment file to `.env`:

```bash
cp .env.example .env
```

### 3. Set Up Mock Google Services

Copy `mock-google-services.json` to `app/google-services.json` to enable local builds without production Firebase credentials:

```bash
cp mock-google-services.json app/google-services.json
```

### 4. Build & Run

Open the project in **Android Studio** (Ladybug / Koala or newer), sync Gradle, and run on an emulator or physical device.

Alternatively, build from the command line:

```bash
# Build Debug APK
./gradlew assembleDebug

# Run Lint Checks
./gradlew lint

# Run Unit Tests
./gradlew test
```

For detailed instructions on testing against Firebase or local emulators, see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## 📚 Documentation

For deeper insight into the architecture and workflow design of ApartmentFlow, refer to the documentation:

- 📖 [Contributing Guide](CONTRIBUTING.md)
- 📜 [Code of Conduct](CODE_OF_CONDUCT.md)
- 📐 [Android Architecture Guide](docs/ANDROID_ARCHITECTURE.md)
- 🔐 [Auth Architecture](docs/AUTH_ARCHITECTURE.md)
- 🔥 [Firestore Strategy](docs/FIRESTORE_ARCHITECTURE.md)
- 🎨 [Design System Guidelines](docs/DESIGN_SYSTEM.md)

---

## 🤝 Community & Contributing

Contributions, feature requests, and bug reports are welcome!

1. Check existing issues or open a new one using our [Issue Templates](.github/ISSUE_TEMPLATE/).
2. Fork the repository and create a feature branch (`feature/amazing-feature`).
3. Ensure `./gradlew assembleDebug` and `./gradlew lint` pass cleanly.
4. Submit a Pull Request following our [Pull Request Template](.github/pull_request_template.md).

---

## 📄 License

ApartmentFlow is released under the [MIT License](LICENSE).
