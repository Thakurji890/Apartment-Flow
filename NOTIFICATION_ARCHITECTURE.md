# Notification System Architecture

## 1. Notification Architecture
- **Client Side (Android)**: Subscribes to Firebase Cloud Messaging (FCM). Handles foreground and background messages via `FirebaseMessagingService`. Maintains local unread count and provides the UI for the Notification Center.
- **Backend (Cloud Functions)**: Acts as the trusted source. Listens to Firestore triggers (onCreate/onUpdate) for core entities (Expenses, Chores, Settlements). Evaluates user preferences, constructs FCM payloads, dispatches via Firebase Admin SDK, and persists the notification in the user's Inbox in Firestore.
- **Data Source (Firestore)**: Stores the persistent user inbox, user notification preferences, and active FCM tokens.

## 2. Firestore Schema
- `users/{userId}/fcmTokens/{tokenString}`
  - `token` (String), `deviceId` (String), `deviceModel` (String), `updatedAt` (Long)
- `users/{userId}/notificationPreferences/default`
  - `expensesEnabled` (Boolean), `settlementsEnabled` (Boolean), `choresEnabled` (Boolean), `quietHoursStart` (Int - hour), `quietHoursEnd` (Int - hour)
- `users/{userId}/notifications/{notificationId}`
  - `id` (String), `apartmentId` (String), `type` (String: EXPENSE, SETTLEMENT, CHORE, etc.), `title` (String), `body` (String), `createdAt` (Long), `isRead` (Boolean), `readAt` (Long), `priority` (String: LOW, NORMAL, HIGH, CRITICAL), `deepLink` (String), `relatedEntityType` (String), `relatedEntityId` (String), `expiresAt` (Long)

## 3. FCM Token Strategy
- **Registration**: Generated via `FirebaseMessaging.getInstance().token` upon login and initial app launch.
- **Storage**: Saved to `users/{userId}/fcmTokens/{token}`. This allows one user to have multiple active devices.
- **Cleanup**: On logout, the client deletes its specific token document from Firestore to stop receiving push notifications for that account.

## 4. Notification Event Matrix
- **Expense**: Created, Edited, Deleted.
- **Settlement**: Requested, Confirmed, Rejected.
- **Chore**: Assigned, Completed, Overdue.
- **Recurring Bill**: Generated, Due Soon.

## 5. Cloud Function Architecture
- **Trigger**: e.g., `functions.firestore.document('apartments/{aptId}/chores/{choreId}').onCreate(...)`
- **Logic**: 
  1. Identifies the `assignedTo` user.
  2. Fetches their `notificationPreferences`.
  3. Evaluates Quiet Hours.
  4. Generates an Idempotent Notification ID.
  5. Writes to `users/{assignedTo}/notifications/{id}`.
  6. Sends via `admin.messaging().sendMulticast(tokens)`.

## 6. Deep-Link Architecture
- Notifications contain a URI (e.g., `app://apartmentflow/chore/{apartmentId}/{choreId}`).
- `FirebaseMessagingService` wraps this URI in an `Intent(Intent.ACTION_VIEW)` inside a `PendingIntent`.
- Jetpack Navigation Compose handles `navArgument` deep links natively.

## 7. Notification Channel Strategy
- **Financial**: Importance HIGH. For settlements and bills.
- **Chores**: Importance DEFAULT. For assigned chores.
- **Apartment**: Importance DEFAULT. For new members.
- **Security**: Importance HIGH. For auth events.

## 8. Permission Strategy
- **Android 13+ (`POST_NOTIFICATIONS`)**: Request permission not on app start, but contextually. Example: When the user opens the "Notifications" screen for the first time, or actively adds their first expense/chore.

## 9. Preference Model
- Managed via UI toggles mapped to `users/{userId}/notificationPreferences/default`.
- Cloud functions read this before pushing.

## 10. Idempotency Strategy
- Notification IDs are deterministic concatenations: `hash({eventType}_{entityId}_{recipientId}_{period})`.
- The Cloud Function performs a `set(doc, {merge: true})`. If `createdAt` already exists, we skip pushing to FCM.

## 11. Security Rules
- `match /users/{userId}/notifications/{notificationId}`: `allow read, write: if request.auth.uid == userId;`
- `match /users/{userId}/fcmTokens/{tokenId}`: `allow read, write: if request.auth.uid == userId;`
- Service accounts (Cloud Functions) bypass these rules to create notifications safely.

## 12. Android Folder Structure
- `feature/notification/data/` (Dto, Mapper, FcmService, RepositoryImpl)
- `feature/notification/domain/` (Models, Repository, UseCases)
- `feature/notification/presentation/` (ViewModel, Screen, Badges)

## 13. Data Flow
- Cloud Function -> FCM -> Android `FirebaseMessagingService` -> System Tray.
- Cloud Function -> Firestore `notifications` -> Android `Flow` -> UI Notification Center.
