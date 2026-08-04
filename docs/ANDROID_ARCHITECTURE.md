# ApartmentFlow: Android Architecture & System Design Document

**Prepared By:** Principal Android Engineer & Mobile Software Architect
**Project:** ApartmentFlow
**Date:** August 2026

---

## 1. Why Clean Architecture?
Clean Architecture separates the software into concentric layers, ensuring that the business logic (Domain) remains entirely independent of frameworks (Android UI, Firebase, Room). 
- **Testability:** Core logic can be unit-tested without Android emulators.
- **Maintainability:** Changes in the database or UI do not ripple through the entire codebase.
- **Scalability:** As the team and feature set grow, strict boundaries prevent spaghetti code.

## 2. MVVM (Model-View-ViewModel) Architecture
We pair Clean Architecture with MVVM for the presentation layer.
- **Model:** Represents the Domain and Data layers (the source of truth).
- **View:** The Jetpack Compose UI. It is completely passive, only rendering state and forwarding user intents.
- **ViewModel:** The intermediary. It transforms data from Use Cases into `UiState` that the View can easily consume, and handles business logic routing.

---

## 3. Top-Level Package Structure
```text
com.apartmentflow.app
├── core           # App-wide utilities, base classes, and extensions
├── di             # Global Hilt dependency injection modules
├── navigation     # Global navigation graphs and routing definitions
└── feature        # Feature-based modules (Package-by-Feature approach)
```

## 4. Package Contents Breakdown
- **core:** Contains elements shared across multiple features (e.g., `Result` wrapper, common extensions, base architecture classes, global constants, design system tokens).
- **di:** Contains global DI components like `NetworkModule`, `DatabaseModule`, `AppModule`.
- **navigation:** Defines route objects (type-safe navigation), `NavHost` configurations, and graph builders.
- **feature:** The heart of the app. Instead of grouping by layer (all ViewModels together), we group by feature. This significantly improves encapsulation.

---

## 5. Feature-Based Modules
To ensure scalability, we use a **Package-by-Feature** approach (which easily transitions into a Multi-Module architecture later).
Features include:
- `authentication` (Login, Signup, Password Reset)
- `onboarding` (Create/Join Apartment)
- `dashboard` (High-level summary)
- `expense` (Ledger, Add Expense, Split Logic)
- `settlement` (Debt calculations, Payments)
- `chore` (Task assignment, Rotations)
- `profile` (User settings, Avatars)
- `apartment_settings` (Invite codes, Member management)

## 6. Organization Within Feature Modules
Each feature contains its own Clean Architecture layers:
```text
feature/expense
├── data
│   ├── local         # Room DAOs specific to expenses
│   ├── remote        # Firebase data sources for expenses
│   ├── mapper        # Expense DTO <-> Domain mapping
│   └── repository    # ExpenseRepositoryImpl
├── domain
│   ├── model         # Expense, Split, Receipt
│   ├── repository    # ExpenseRepository interface
│   └── usecase       # AddExpenseUseCase, CalculateSplitUseCase
└── presentation
    ├── ExpenseListViewModel
    ├── ExpenseListScreen
    ├── ExpenseUiState
    └── components    # Composables specific to expenses (e.g., ExpenseCard)
```

---

## 7. Domain Layer
The Domain layer is the most pure, knowing nothing about Android, Room, or Firebase.
- **Use Cases (Interactors):** Single-responsibility classes containing specific business rules (e.g., `SimplifyDebtsUseCase`). They combine data from multiple repositories if necessary.
- **Repository Interfaces:** Contracts defining what data operations are required, without defining *how* they are fulfilled.
- **Models:** Pure Kotlin data classes representing the business entities (e.g., `User`, `Expense`).

## 8. Data Layer
The Data layer implements the Repository interfaces defined in the Domain layer.
- **Remote Data Source:** Handles Firebase Firestore/Storage calls. Returns DTOs (Data Transfer Objects).
- **Local Data Source:** Handles Room DB / DataStore operations. Returns Local Entities.
- **Repository Implementation:** Coordinates between local and remote sources (e.g., fetching from Firestore, saving to Room, returning domain models).
- **DTOs / Entities:** Data classes heavily annotated for frameworks (e.g., `@Entity`, `@DocumentId`, `@PropertyName`).
- **Mappers:** Extension functions converting DTOs/Entities to Domain Models, ensuring the Domain layer never sees a framework annotation.

## 9. Presentation Layer
- **ViewModels:** Survive configuration changes. Exposes state and handles intents.
- **UI State:** Immutable data classes representing the exact state of the screen (e.g., `isLoading`, `expensesList`, `errorMessage`).
- **Events (One-off):** Actions that should only be consumed once (e.g., showing a Snackbar, triggering navigation).
- **Compose Screens:** Stateless functions that take `UiState` as parameters and expose lambdas for events.
- **Components:** Reusable UI widgets.

---

## 10. Dependency Injection Structure (Hilt)
- **Modules:** Organized by responsibility (e.g., `FirebaseModule` for providing Auth/Firestore instances, `RepositoryModule` for binding interfaces to implementations).
- **Singleton Scope (`@Singleton`):** Used for components that must exist app-wide (e.g., Room Database, Firebase instances, DataStore).
- **ViewModel Scope (`@ViewModelScoped`):** Used for Use Cases and components that only need to live as long as a specific ViewModel.

---

## 11. State Management
- **StateFlow:** Used in the ViewModel to hold and emit the current `UiState`. It requires an initial state and is always observed by the UI (`collectAsStateWithLifecycle`).
- **SharedFlow:** Used for emitting one-time `UiEvent`s (like navigation or toasts) to ensure they aren't re-emitted on configuration changes.
- **Compose State:** Used strictly for local, transient UI state (e.g., whether a dropdown menu is expanded or text input in a text field) that the ViewModel doesn't need to know about.

## 12. Navigation Architecture
Using Navigation Compose with Type-Safe routes (Kotlin Serialization).
- **Root Graph:** The top-level `NavHost` deciding whether to show the Auth Graph or the Main Graph based on authentication state.
- **Auth Graph:** Nested graph containing `Splash`, `Login`, and `Signup`.
- **Main Graph:** Nested graph containing the `BottomNavigationBar` and its associated screens (`Dashboard`, `Expenses`, `Chores`, `Settings`). Deep links (like invite codes) feed directly into this graph.

---

## 13. Error Handling
- **Result Wrapper:** A generic `sealed class Result<T>` (`Success`, `Error`, `Loading`) used across layer boundaries.
- **Global Error Handling:** Network errors and crashes are logged to Crashlytics automatically.
- **Local Error Handling:** The ViewModel maps Domain errors into user-friendly strings and updates the `UiState` to show error banners or Snackbars.

## 14. Loading States
Follow the LCE (Loading, Content, Error) pattern.
- **Initial Load:** Show Skeleton loaders (Shimmer effects) for lists and dashboard elements to reduce perceived loading time.
- **Action Load:** Show a circular progress indicator (spinner) inside buttons when an action (e.g., submitting an expense) is in flight.

## 15. Offline Strategy
- **Room as SSOT (Single Source of Truth):** The UI *only* ever observes the Room database.
- **Firebase Persistence:** Enabled as a safety net.
- **Flow:** When an expense is added, the Repository writes it to Room (triggering an immediate UI update), and then asynchronously writes it to Firestore.

---

## 16. Repository Pattern
Abstracts the origin of data. The ViewModel asks for a `Flow<List<Expense>>`, and the Repository handles fetching from Room and syncing with Firestore in the background. The ViewModel doesn't know Firebase exists.

## 17. Use Case Pattern
Enforces the Single Responsibility Principle. Instead of a massive `ExpenseViewModel` with 10 functions, we inject `AddExpenseUseCase`, `DeleteExpenseUseCase`, and `GetExpensesUseCase`. This makes testing incredibly simple and keeps ViewModels lean.

## 18. Mapper Pattern
Crucial for Clean Architecture. Data models from Firestore often have different structural needs than UI or Domain models. Mappers explicitly translate `FirebaseExpense` -> `DomainExpense` -> `ExpenseUiModel`, preventing API changes from breaking the UI.

## 19. UI Component Reusability
Establish a `core/designsystem` package.
- Never hardcode colors or typography in feature screens.
- Build custom wrappers around Material 3 components (e.g., `ApartmentPrimaryButton`, `ApartmentTextField`) to ensure consistency and single-point-of-change updates.

---

## 20. Coding Standards
- **Immutability:** Use `val` everywhere. Data classes should be immutable.
- **UDF (Unidirectional Data Flow):** State flows down from the ViewModel, Events flow up from the UI.
- **No Business Logic in Views:** Composables must not contain `if (expense > 100)` business rules; that logic belongs in the ViewModel or Domain layer.

## 21. Naming Conventions
- **Classes/Interfaces:** PascalCase (`AddExpenseUseCase`).
- **Functions/Variables:** camelCase (`calculateTotal()`).
- **Composables:** PascalCase (`ExpenseCard`). Must be nouns or noun-phrases.
- **Layouts/Resources:** snake_case (`ic_expense_icon.xml`).
- **State Classes:** Suffix with `UiState` (`ExpenseListUiState`).

## 22. Logging Strategy
- Avoid `Log.d` in production.
- Use a logging wrapper (like Timber).
- Route `Error` and `Warning` level logs to Firebase Crashlytics as custom non-fatal exceptions, providing breadcrumbs for hard-to-reproduce bugs.

## 23. Testing Strategy
- **Domain Layer:** 100% Unit Test coverage using JUnit. Pure Kotlin makes this fast and easy.
- **ViewModels:** Unit tested using `Turbine` for Flow assertions and `CoroutinesTestRule`.
- **Data Layer:** Integration tests for Room DAOs (using Robolectric) and mocked remote sources.
- **UI Layer:** Compose UI testing for standalone components, and Roborazzi for automated screenshot regression testing of full screens.

## 24. Scalability Considerations
- The **Package-by-Feature** approach guarantees that as the app grows, we don't end up with a single `presentation` package containing 50 ViewModels.
- If compile times increase, this structure allows us to extract `feature/expense` into a completely separate Gradle module with minimal refactoring.
- Using Hilt allows us to easily swap out implementations (e.g., moving from Firebase to a custom REST API in the future simply requires a new Repository Implementation).

---

## 25. Architecture Review & Pre-Development Insights

**Review Summary:** The architecture is robust, strictly separated, and primed for scalability. 

**Improvements & Recommendations Before UI Development:**
1. **Design System First:** Before building the first screen, we must implement the `core/designsystem` package containing the Material 3 Theme, Typography, Colors, and baseline components. Failure to do so will result in fragmented UI code.
2. **Result Wrapper Standardization:** Define the generic `Result` class immediately. All repositories and Use Cases must agree on this contract before feature work starts.
3. **Offline Strategy Nuance:** While Room is the SSOT, ensure we design a mechanism for the UI to know if a local write is still "pending sync" to Firestore (e.g., a subtle cloud icon on an expense), to ensure user trust.

**Conclusion:** The project foundation is architecturally sound and ready for feature implementation.
