# Offline Synchronization and Background Processing Architecture

## 1. Offline Architecture
ApartmentFlow uses a Local-First architecture leveraging **Room** and **Firebase Firestore**. 
- **Reads**: UI reads continuously via Kotlin Flows mapped from Firestore snapshot listeners (which have built-in offline caching) combined with Room entities for complex aggregations.
- **Writes**: Instead of writing directly to Firestore, write mutations route through an **Outbox Pattern** saved into the local Room database. 
- **Background Sync**: A SyncManager utilizing Android WorkManager continuously watches the network and processes the Outbox.

## 2. Room Schema
The `AppDatabase` will be updated to include an `OutboxEntity`. Since Firestore natively handles local caching for document reads effectively, we will not duplicate *all* collections into Room to avoid redundant caching, except where complex local joins are needed (like `ExpenseEntity` which already exists). 
- `OutboxEntity`: Stores pending mutations.
  - `id`: UUID (Primary Key)
  - `collection`: String (e.g., "expenses", "settlements")
  - `documentId`: String
  - `operation`: String (CREATE, UPDATE, DELETE)
  - `payload`: String (JSON representation of the entity)
  - `status`: String (PENDING, SYNCING, FAILED)
  - `errorReason`: String?
  - `createdAt`, `updatedAt`: Long

## 3. Sync State Machine
1. **PENDING**: Operation queued locally.
2. **SYNCING**: Picked up by WorkManager, network call in progress.
3. **SUCCESS**: Confirmed by server (item is deleted from Outbox).
4. **FAILED**: Non-retryable error (e.g., validation failed).
5. **CONFLICT**: Requires manual resolution.

## 4. Outbox Design
`OutboxRepository` provides methods to enqueue mutations.
When `addExpense` is called:
1. The `Expense` is saved to `ExpenseEntity` in Room (optimistic UI update).
2. The mutation is saved to `OutboxEntity`.
3. `SyncManager` requests an immediate one-time `SyncWorker`.

## 5. Conflict Strategy
- **Non-Financial (Shopping, Chores)**: Last-write-wins based on `updatedAt` timestamps.
- **Financial (Expenses, Settlements)**: Server-authoritative idempotency. Each Outbox operation generates an `idempotencyKey` (`deviceId + operationId`). Cloud Functions or Firestore Security Rules ensure the document is only applied once. If local state drifts significantly from server validation (e.g., balance limits), it marks as `CONFLICT`.

## 6. Retry Strategy
- Utilizes `WorkManager` default **Exponential Backoff** (`BackoffPolicy.EXPONENTIAL`).
- Network failures -> Automatic retry.
- Auth failures -> Halt until logged in.
- Permission/Validation failures -> Mark as `FAILED` (No retry).

## 7. WorkManager Architecture
- **SyncWorker**: Periodically and reactively flushes the Outbox.
- **UploadWorker**: Dedicated worker for pushing images/receipts to Firebase Storage, returning download URLs to be attached to `Outbox` payloads before syncing.

## 8. Connectivity Architecture
`ConnectivityMonitor` uses `ConnectivityManager.NetworkCallback` exposed as a `StateFlow<ConnectivityStatus>`. The UI uses this to display "You are offline" banners unobtrusively.

## 9. Security Strategy
- Outbox data is strictly scoped to the current user's authenticated UID.
- WorkManager cancels all jobs on logout and clears the Outbox database.
- Server-side Firestore rules remain the final source of truth for all mutations.

## 10. Data Flow
`UI` -> `ViewModel` -> `UseCase` -> `Repository` -> `OutboxDao` (Save to Local) -> `SyncWorker` -> `Firestore API` -> Server validation -> `SnapshotListener` updates Local Cache -> `UI` observes changes.

## 11. Migration Strategy
Using Room's `Migration` mechanism. Bumping version from 1 to 2. Adding `outbox` table.

## 12. Testing Strategy
Unit tests for `SyncWorker` simulating network failures. Integration tests using WorkManager Test API (`WorkManagerTestInitHelper`).

## 13. Android Folder Structure
```
app/src/main/java/com/example/core/
  connectivity/
    ConnectivityMonitor.kt
  sync/
    SyncManager.kt
    OutboxEntity.kt
    OutboxDao.kt
    OutboxRepository.kt
  worker/
    SyncWorker.kt
```