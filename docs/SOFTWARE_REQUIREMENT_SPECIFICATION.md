# ApartmentFlow: Software Requirement Specification (SRS) & Architecture Document

**Prepared By:** Senior Android Engineer & Mobile Software Architect
**Project:** ApartmentFlow
**Date:** August 2026

---

## 1. Project Vision
To eliminate the friction and awkwardness of shared living by providing a transparent, fair, and automated platform for roommates to manage expenses, chores, and household responsibilities seamlessly.

## 2. Problem Statement
Roommates frequently struggle with tracking shared expenses, leading to disputes, delayed payments, and resentment. Coordinating chores and household needs (e.g., groceries) is historically disorganized, relying on disjointed group chats or physical whiteboards. There is a lack of a centralized, trustworthy system to manage the "business" of living together.

## 3. Objectives
- **Centralize Household Management:** Create a single hub for all apartment-related logistics.
- **Automate Financial Tracking:** Simplify expense splitting with various mathematical models (equal, exact amounts, percentages).
- **Ensure Transparency:** Provide a clear, immutable ledger of who paid for what and who owes whom.
- **Promote Fairness:** Organize household chores fairly with automated rotations and reminders.

## 4. Target Users
- College and University students living off-campus.
- Young professionals in co-living spaces.
- Couples managing shared finances.
- Long-term shared housing communities.

## 5. User Personas
- **The Organizer (Alex):** Proactive, sets up the apartment group, wants everything tracked accurately and fairly. Values robust reporting and ledger history.
- **The Forgetful Payer (Sam):** Well-meaning but disorganized. Needs push notifications and clear, actionable reminders to pay bills and complete assigned chores.
- **The Delegator (Jamie):** Wants chores assigned systematically to avoid the burden of having to manually ask people to do their part.

## 6. Core Features
- **Household Creation & Onboarding:** Securely create or join an apartment via a unique invite code or deep link.
- **Expense Engine:** Add expenses with customizable split logic, receipt attachments, and categorization (Rent, Groceries, Utilities, etc.).
- **Settlement System:** Calculates the most efficient path to settle debts (simplifying debts between multiple people).
- **Chore Tracker:** Assign, rotate, and check off recurring or one-time chores.
- **Real-Time Synchronization:** Instant updates across all roommate devices.
- **Push Notifications:** Reminders for overdue chores, new expenses, and settled debts.

## 7. Functional Requirements
- **FR1:** Users must be able to authenticate via Email/Password or Google Sign-In.
- **FR2:** Users must be able to generate a unique 6-digit alphanumeric code to invite others.
- **FR3:** The system must support split logic: Equal, Exact Amounts, and Percentages.
- **FR4:** The system must calculate an optimized "Who Owes Who" summary (Debt Simplification).
- **FR5:** Users must be able to upload receipt images (JPEG/PNG) to an expense.
- **FR6:** Users must be able to mark a debt as "Paid" and await confirmation from the receiver.

## 8. Non-functional Requirements
- **Reliability:** 99.9% uptime relying on Firebase backend infrastructure.
- **Performance:** Application must load the initial dashboard in < 2 seconds.
- **Offline Capability:** Core ledger and chores must be readable offline; actions queued for sync.
- **Scalability:** Architecture must support scaling to hundreds of thousands of concurrent users seamlessly.
- **Security:** Strict data isolation; users cannot access data of apartments they do not belong to.

## 9. Complete App Modules
1.  **Auth Module:** Login, Registration, Password Reset, Google Auth.
2.  **Onboarding Module:** Create Apartment, Join Apartment, Welcome Flow.
3.  **Dashboard Module:** High-level summary of balances, upcoming chores, and recent activity.
4.  **Expense Module:** Ledger, Add/Edit Expense, Split Calculator, Receipt Viewer.
5.  **Settlement Module:** Debt optimization matrix, Payment recording, Settle Up flow.
6.  **Chore Module:** Task list, Rotation logic, Assignment management.
7.  **Settings Module:** Profile management, Apartment management (kick users, reset code), Notification preferences.

## 10. User Roles
- **Admin (Creator):** Can edit apartment details, regenerate invite codes, and remove members.
- **Member:** Can add expenses, record payments, complete chores, and edit their own profile.

## 11. Complete Screen List
- `SplashScreen`
- `LoginScreen` / `SignupScreen`
- `ApartmentChoiceScreen` (Create or Join)
- `DashboardScreen` (Home)
- `ExpenseListScreen` (Ledger)
- `AddEditExpenseScreen`
- `SettleUpScreen`
- `ChoreListScreen`
- `AddEditChoreScreen`
- `ProfileSettingsScreen`
- `ApartmentSettingsScreen`

## 12. Navigation Flow
- **Authentication Graph:** `Splash` -> `Login/Signup` -> (If no apartment) -> `ApartmentChoice`
- **Main Graph (Bottom Navigation):**
    - **Tab 1:** `Dashboard`
    - **Tab 2:** `Expenses` (Nested: `AddExpense`, `SettleUp`)
    - **Tab 3:** `Chores` (Nested: `AddChore`)
    - **Tab 4:** `Settings` (Nested: `Profile`, `ApartmentMgmt`)

## 13. Firebase Services Required
- **Firebase Authentication:** Secure user identity (Google & Email/Password).
- **Cloud Firestore:** Real-time NoSQL database for syncing apartment state.
- **Cloud Storage:** Storing user avatars and receipt images.
- **Cloud Messaging (FCM):** Push notifications for expenses and chores.
- **Google Analytics:** Tracking core user journeys and engagement.
- **Crashlytics:** Monitoring application stability and fatal/non-fatal errors.

## 14. Firestore Collections Overview
- `users/{userId}`: Profile info, active `apartmentId`, FCM tokens.
- `apartments/{apartmentId}`: Apartment metadata, invite code, member list.
    - `expenses/{expenseId}` (Subcollection): Amount, payer, split details, timestamp.
    - `settlements/{settlementId}` (Subcollection): Payer, receiver, amount, status.
    - `chores/{choreId}` (Subcollection): Title, assignee, rotation frequency, status.

## 15. Offline Strategy
- **Local Database:** Room DB serves as the Single Source of Truth (SSOT).
- **Read:** The UI strictly observes Room via Kotlin `Flow`.
- **Write:** Actions write to Room first (optimistic update), then enqueue a background sync worker.
- **Sync:** Firebase offline persistence is enabled as a fallback, but explicit Room caching ensures deterministic offline behavior and relational querying.

## 16. Security Strategy
- **Firestore Security Rules:** Strict rules ensuring `request.auth.uid` is in the apartment's `members` array for all reads/writes in subcollections.
- **App Check:** Enforce Play Integrity API to prevent unauthorized backend access.
- **Local Security:** Encrypted DataStore for sensitive session tokens or preferences.

## 17. Clean Architecture Layers
- **Domain Layer (Pure Kotlin):** Contains `Entities` (e.g., `Expense`, `User`), `UseCases` (e.g., `CalculateDebtsUseCase`, `AssignChoreUseCase`), and `Repository Interfaces`.
- **Data Layer:** Implements Repositories. Contains Room `DAOs`, `FirebaseDataSources`, and `Mappers` to convert DTOs to Domain Entities.
- **Presentation Layer:** Jetpack Compose screens, `ViewModels`, and UI State classes.

## 18. MVVM Structure
- **Model:** Domain entities and Data layer.
- **View:** Jetpack Compose functions reacting to state.
- **ViewModel:** Manages UI State. Collects domain flows, handles user intents (events), and exposes a single `StateFlow<UiState>` and a `SharedFlow<UiEvent>` (for one-off actions like navigation/toasts).

## 19. Recommended Folder Structure
```text
com.apartmentflow.app
├── di                  # Hilt modules
├── domain
│   ├── model           # Core business objects
│   ├── repository      # Interfaces
│   └── usecase         # Business logic
├── data
│   ├── local           # Room DB, DAOs
│   ├── remote          # Firebase data sources
│   ├── repository      # Repository implementations
│   └── mapper          # Data to Domain mappers
└── presentation
    ├── theme           # Compose Theme, Colors, Typography
    ├── core            # Reusable UI components
    ├── auth            # Login/Signup UI & ViewModels
    ├── dashboard       # Home UI & ViewModels
    ├── expense         # Expense logic
    └── chore           # Chore logic
```

## 20. Third-party Libraries
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room
- **Image Loading:** Coil
- **Concurrency:** Kotlin Coroutines & Flow
- **UI & Navigation:** Jetpack Compose, Navigation Compose
- **Serialization:** Kotlinx Serialization
- **Backend:** Firebase BoM (Auth, Firestore, Storage, Messaging, Crashlytics)

## 21. Coding Standards
- Strict adherence to official Kotlin Style Guide and Compose API guidelines.
- Unidirectional Data Flow (UDF) for all Compose screens.
- State hoisting for reusable components.
- Zero business logic inside Composable functions.
- Use `ktlint` for static code analysis.

## 22. Git Branch Strategy
- **GitFlow Methodology:**
    - `main`: Production-ready code.
    - `develop`: Integration branch for upcoming releases.
    - `feature/{name}`: New features.
    - `bugfix/{name}`: Non-critical fixes.
    - `release/{version}`: Release preparation.

## 23. Development Phases
- **Phase 1 (Foundation):** Project setup, DI, Theme, Authentication, Create/Join Apartment.
- **Phase 2 (Ledger):** Room + Firestore sync, Add Expenses, Expense Feed.
- **Phase 3 (Settlements):** Debt calculation algorithm, Settle Up flow.
- **Phase 4 (Operations):** Chores, Rotations, User Profiles, Settings.
- **Phase 5 (Polish):** Push Notifications, Offline conflict resolution, Analytics, Final UI Polish.

## 24. Risks and Mitigation
- **Risk:** Floating point precision errors in financial calculations.
    - **Mitigation:** Use `Long` (representing cents) or `BigDecimal` for all money mathematics. Never use `Float` or `Double` for currency.
- **Risk:** Race conditions when two users edit an expense simultaneously.
    - **Mitigation:** Utilize Firestore Transactions for critical financial updates.
- **Risk:** Complex debt simplification algorithm causing UI lag.
    - **Mitigation:** Execute graph algorithms (who owes who) entirely on the `Default` dispatcher and emit results via `Flow`.

## 25. Future Scope
- **Bank Integrations:** OpenBanking/Plaid integration to automatically import shared expenses.
- **Payment Gateway:** Deep links to Venmo, Zelle, or CashApp for 1-click settlements.
- **Receipt OCR:** Automatically parse totals and line items from uploaded receipt photos.
- **Landlord Portal:** Allow landlords to receive rent directly through the platform.
