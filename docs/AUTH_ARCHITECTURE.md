# ApartmentFlow: Authentication Architecture

**Prepared By:** Principal Android Engineer
**Project:** ApartmentFlow
**Date:** August 2026

## Overview

The authentication module establishes a secure, robust foundation for ApartmentFlow. It fully embraces **Clean Architecture**, **MVVM**, **Hilt**, and **Firebase Authentication**.

## Folder Structure

The authentication module is located under `com.example.feature.auth` and is structured package-by-feature across three main layers:

```
com.example.feature.auth
├── data
│   └── repository
│       └── AuthRepositoryImpl.kt      // Implementation of Firebase auth calls and DataStore preferences
├── domain
│   ├── model
│   │   └── User.kt                    // Pure Kotlin representation of an authenticated user
│   ├── repository
│   │   └── AuthRepository.kt          // Interface defining the authentication contract
│   └── usecase
│       └── UseCases.kt                // Granular use cases: SignInUseCase, SignUpUseCase, etc.
└── presentation
    ├── splash                         // Handles initial routing logic
    ├── onboarding                     // 3-page introduction and tutorial
    ├── welcome                        // Landing screen (Login / Sign Up router)
    ├── login                          // Email/Password login, "Remember Me"
    ├── signup                         // Account creation with inline validation
    ├── forgotpassword                 // Password reset flow
    └── emailverification              // Gates access to the app until email is verified
```

## Authentication Flow

1. **Splash Screen**: Acts as the traffic controller. It checks `AuthRepository.getCurrentUser()` and `AuthRepository.getOnboardingCompleted()`.
   - If user exists and verified -> `Dashboard`
   - If user exists but not verified -> `EmailVerification`
   - If no user, but onboarding incomplete -> `Onboarding`
   - Else -> `Welcome`
2. **Onboarding**: Displays educational pages about the app. Updates DataStore upon completion to never show again.
3. **Login / Sign Up**: Interfaces for credential entry. ViewModels perform live validation (email formatting, password complexity) before attempting a network request.
4. **Email Verification**: Once a user signs up, an email is dispatched. They are routed here and cannot proceed to the Dashboard until they verify their email. A `Check Verification` button queries Firebase.

## Architectural Decisions

1. **Why Use Cases for Auth?**
   Authentication actions (like login, reset password) are highly independent. Injecting `SignInUseCase` directly into `LoginViewModel` instead of injecting a massive `AuthRepository` adheres to the **Interface Segregation Principle**. This keeps ViewModels lean and testing trivial.

2. **Why DataStore?**
   We use `Preferences DataStore` over `SharedPreferences` because it natively supports Kotlin Coroutines and Flow, preventing UI blocking reads and ensuring thread safety. It is used to persist the `onboarding_completed` flag and the `remember_me` state.

3. **Why Sealed Classes for UI State & Events?**
   - `LoginState`: A data class representing the precise visual state of the screen (e.g., loading spinners, error text fields).
   - `UiEvent`: A sealed class for "fire-and-forget" actions like navigating away or showing a Snackbar. This prevents the "Snackbar shows again on rotation" bug common with LiveData.

## Security Considerations

1. **Firebase Authentication**: We do not roll our own crypto. Firebase securely salts and hashes passwords.
2. **Email Verification Gate**: By preventing unverified users from accessing the `Dashboard`, we protect the database from spam accounts generating dummy apartments.
3. **Password Strength Validation**: Enforced locally (minimum 8 chars, 1 letter, 1 number) before hitting the network to reduce Firebase API calls and ensure a baseline level of user security.
4. **Credential Handling**: We never log passwords. Variables containing passwords exist only in memory during the transaction.
