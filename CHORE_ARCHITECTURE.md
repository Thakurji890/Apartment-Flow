# Chore Management Architecture

## 1. Final Architecture
- **Layered Clean Architecture**: 
  - `data`: DTOs, `ChoreRepositoryImpl`, Mappers.
  - `domain`: Models (`Chore`, `ChoreStatus`, `ChoreRecurrence`), `ChoreRepository` interface, Use Cases (`CompleteChoreUseCase`, `GetFairnessSummaryUseCase`).
  - `presentation`: UI state, ViewModels, and Jetpack Compose screens.

## 2. Firestore Schema
- `apartments/{apartmentId}/chores/{choreId}`
  - **Fields**: `id` (String), `name` (String), `description` (String), `category` (String), `priority` (String), `assignedTo` (String), `dueDate` (Long), `recurrence` (String), `status` (String), `points` (Int), `rotationEnabled` (Boolean), `rotationOrder` (List<String>), `completedAt` (Long), `completedBy` (String).
  - **Why this schema?**: By treating every occurrence as a separate document, querying "My Pending Chores" or "Completed Chores History" becomes a simple single-collection query. When a recurring chore is completed, a new document is generated for the next occurrence.

## 3. Security Model
- **Rules**: 
  - `match /apartments/{apartmentId}/chores/{choreId}`:
    - `allow read, write: if request.auth.uid in get(/databases/$(database)/documents/apartments/$(apartmentId)).data.members;`
  - Prevents non-members from reading/writing chores.

## 4. Chore State Machine
- `PENDING` -> `IN_PROGRESS` -> `COMPLETED` (or `PENDING_VERIFICATION` -> `VERIFIED`)
- `PENDING` -> `SKIPPED` / `CANCELLED`
- Auto-transition to `OVERDUE` handled via UI/Domain logic when `dueDate < now` and status is `PENDING` or `IN_PROGRESS`.

## 5. Recurrence Algorithm
- When a chore with `recurrence != NONE` transitions to `COMPLETED` or `SKIPPED`:
  - A new `Chore` document is cloned.
  - `dueDate` is incremented based on the `recurrence` rule (e.g., +1 day for DAILY, +7 days for WEEKLY).
  - `status` is reset to `PENDING`.
  - ID is newly generated.

## 6. Rotation Algorithm
- If `rotationEnabled` is true, the `CompleteChoreUseCase` will inspect `rotationOrder`.
- Finds the current `assignedTo` index in `rotationOrder`.
- Sets the new clone's `assignedTo` to `rotationOrder[(currentIndex + 1) % rotationOrder.size]`.

## 7. Fairness/Points Calculation
- `GetFairnessSummaryUseCase` aggregates all `COMPLETED` chores for the apartment over a given time period.
- Sums the `points` field grouped by `completedBy` (User ID).
- Returns a `FairnessSummary` object containing points and completion counts per user.

## 8. Offline/Conflict Strategy
- Rely on Firestore's built-in offline persistence.
- If two users complete the same chore offline, the last synced write wins (Last-Write-Wins). Given chores are usually low-contention, LWW is acceptable.

## 9. Required Indexes
- Collection: `chores`
  - `apartmentId` ASC, `status` ASC, `dueDate` ASC
  - `apartmentId` ASC, `assignedTo` ASC, `status` ASC

## 10. Android Folder Structure
- `app/src/main/java/com/example/feature/chore/`
  - `data/dto/`, `data/mapper/`, `data/repository/`
  - `domain/model/`, `domain/repository/`, `domain/usecase/`
  - `presentation/list/`, `presentation/add_edit/`, `presentation/details/`, `presentation/summary/`

## 11. Data Flow
- **UI** calls `ViewModel`.
- **ViewModel** observes `GetChoresUseCase` (Flow from Repository).
- **Repository** attaches Firestore SnapshotListener.
- On completion: UI -> ViewModel -> `CompleteChoreUseCase` -> Updates current doc & creates next occurrence if recurring.
