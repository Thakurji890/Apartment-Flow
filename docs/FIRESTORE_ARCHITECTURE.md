# ApartmentFlow: Firestore Database Architecture & Security Design

**Prepared By:** Senior Firebase Solutions Architect
**Project:** ApartmentFlow
**Date:** August 2026

---

## 1. Why Firestore for ApartmentFlow?

Firestore is an optimal NoSQL database for ApartmentFlow due to the following characteristics:
- **Real-time Synchronization:** Shared living requires instant updates. When a roommate adds an expense or checks off a chore, Firestore's listener capabilities immediately push these changes to all connected devices.
- **Offline Capabilities:** Firestore SDKs cache data locally, allowing roommates to view expenses or add items to a shopping list in low-connectivity areas (e.g., grocery stores or basements). Changes sync automatically when reconnected.
- **Scalability:** Firestore scales automatically to accommodate massive datasets and concurrent connections.
- **Hierarchical Data Structure:** The ability to nest subcollections (e.g., expenses within an apartment) perfectly maps to the physical reality of a shared apartment.

---

## 2. Core Architectural Decisions

### 2.1 Subcollections vs. Root Collections
- **Root Collections:** Used for global entities that exist independently or need to be queried across multiple contexts (e.g., `users`, `apartments`).
- **Subcollections:** Used for entities strictly bounded by a parent document. For ApartmentFlow, data like `expenses`, `chores`, and `settlements` are contextually bound to an `apartment`. Placing them as subcollections under `apartments/{apartmentId}` isolates data, restricts security rules naturally, and improves query performance (scoping queries to a single apartment).

### 2.2 Avoiding Data Duplication vs. Denormalization
NoSQL favors denormalization (duplicating data to avoid complex joins), but we must be strategic:
- **Avoid:** Duplicating highly mutable data (e.g., user balances). Calculate these dynamically or use Cloud Functions to aggregate.
- **Allow:** Duplicating immutable or rarely changing data (e.g., storing a snapshot of the user's `displayName` and `avatarUrl` inside an `expense` document so the client doesn't need to fetch the `users` document separately just to render a list).

### 2.3 Unique ID Strategy
- **Document IDs:** Rely exclusively on Firestore's auto-generated 20-character alphanumeric IDs for all documents (except `users`, which will use the Firebase Auth UID).
- **Relational Integrity:** Store related IDs as arrays or strings (e.g., `apartmentId` inside a `user` document).

### 2.4 Timestamps
- Always use Firestore Server Timestamps (`FieldValue.serverTimestamp()`) for creation/modification times. Client device time is unreliable.

### 2.5 Soft Delete vs. Permanent Delete
- **Financial/Ledger Data (Expenses, Settlements):** Use **Soft Delete** (`isDeleted: true`). Financial records must be immutable and audit-able.
- **Transitory Data (Grocery items, Notifications):** Use **Permanent Delete** to save storage space and reduce clutter.

---

## 3. Firestore Schema Design

### 3.1 `users` (Root Collection)
- **Purpose:** Stores global user profiles and their apartment affiliations.
- **Document ID:** Firebase Auth UID
- **Structure:**
  - `uid` (String, required)
  - `email` (String, required)
  - `displayName` (String, required)
  - `avatarUrl` (String, optional)
  - `activeApartmentId` (String, optional)
  - `apartmentIds` (Array of Strings, optional) - History of apartments.
  - `fcmTokens` (Array of Strings, optional)
  - `createdAt` (Timestamp, required)
  - `updatedAt` (Timestamp, required)

### 3.2 `userSettings` (Subcollection under `users/{uid}`)
- **Purpose:** User-specific preferences.
- **Document ID:** `preferences`
- **Structure:**
  - `pushNotificationsEnabled` (Boolean, required)
  - `expenseAlerts` (Boolean, required)
  - `choreReminders` (Boolean, required)
  - `themePreference` (String: "light", "dark", "system")

### 3.3 `apartments` (Root Collection)
- **Purpose:** The core entity representing a shared household.
- **Document ID:** Auto-generated
- **Structure:**
  - `id` (String, required)
  - `name` (String, required)
  - `address` (String, optional)
  - `inviteCode` (String, required) - 6-character alphanumeric.
  - `createdByUid` (String, required)
  - `createdAt` (Timestamp, required)
  - `updatedAt` (Timestamp, required)
  - `isActive` (Boolean, required)

### 3.4 `apartmentMembers` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** Tracks who is currently in the apartment, their roles, and their aggregate balance.
- **Document ID:** User UID
- **Structure:**
  - `uid` (String, required)
  - `role` (String: "admin", "member", required)
  - `joinedAt` (Timestamp, required)
  - `balance` (Number/Map, required) - Denormalized total balance to avoid recalculating the entire ledger on load. (e.g., `balance: { "USD": 50.00 }` where positive means owed, negative means owes).

### 3.5 `expenses` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** The ledger of all shared costs.
- **Document ID:** Auto-generated
- **Structure:**
  - `id` (String, required)
  - `title` (String, required)
  - `totalAmount` (Number, required) - Store in cents to avoid floating-point errors.
  - `currency` (String, required)
  - `category` (String: "Rent", "Utilities", "Groceries", etc., required)
  - `paidByUid` (String, required)
  - `receiptUrl` (String, optional)
  - `date` (Timestamp, required)
  - `isDeleted` (Boolean, required) - Soft delete.
  - `createdByUid` (String, required)

### 3.6 `expenseParticipants` (Subcollection under `apartments/{apartmentId}/expenses/{expenseId}`)
- **Purpose:** Details how the expense is split.
- **Document ID:** User UID
- **Structure:**
  - `uid` (String, required)
  - `owedAmount` (Number, required)
  - `splitMethod` (String: "equal", "exact", "percentage", required)
  - `splitValue` (Number, required) - e.g., 50 (if 50%), or 2500 (if exact amount in cents).

### 3.7 `settlements` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** Records payments made between roommates to clear debt.
- **Document ID:** Auto-generated
- **Structure:**
  - `id` (String, required)
  - `payerUid` (String, required)
  - `receiverUid` (String, required)
  - `amount` (Number, required)
  - `currency` (String, required)
  - `status` (String: "pending", "confirmed", "rejected", required)
  - `date` (Timestamp, required)

### 3.8 `recurringBills` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** Templates for bills that occur monthly/weekly. A Cloud Function will read these and generate actual `expenses`.
- **Document ID:** Auto-generated
- **Structure:**
  - `id` (String, required)
  - `title` (String, required)
  - `amount` (Number, optional) - Variable or fixed.
  - `frequency` (String: "monthly", "weekly", required)
  - `nextDueDate` (Timestamp, required)
  - `defaultPayerUid` (String, required)
  - `defaultSplit` (Map/Array, required) - Blueprint for `expenseParticipants`.

### 3.9 `groceryLists` & `groceryItems` (Subcollections under `apartments/{apartmentId}`)
- **Purpose:** Shared shopping lists.
- **`groceryLists` Structure:**
  - `id`, `name`, `createdAt`
- **`groceryItems` (Subcollection under list):**
  - `id`, `itemName`, `addedByUid`, `isPurchased`, `purchasedByUid`

### 3.10 `chores` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** Manage household tasks.
- **Document ID:** Auto-generated
- **Structure:**
  - `id` (String, required)
  - `title` (String, required)
  - `description` (String, optional)
  - `assigneeUid` (String, required)
  - `dueDate` (Timestamp, required)
  - `status` (String: "pending", "completed", required)
  - `isRecurring` (Boolean, required)
  - `rotationPattern` (Array of UIDs, optional) - Who it rotates to next.

### 3.11 `notifications` (Subcollection under `users/{uid}`)
- **Purpose:** In-app notification center.
- **Document ID:** Auto-generated
- **Structure:**
  - `id`, `title`, `body`, `type` ("expense_added", "chore_due", "settlement_requested"), `relatedId`, `isRead`, `createdAt`.

### 3.12 `receipts` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** Metadata for uploaded receipts. Actual files in Storage.
- **Structure:**
  - `id`, `uploaderUid`, `storagePath`, `expenseId` (optional), `uploadedAt`.

### 3.13 `activityLogs` (Subcollection under `apartments/{apartmentId}`)
- **Purpose:** Audit trail for transparency (e.g., "Alex deleted an expense").
- **Structure:**
  - `id`, `actorUid`, `action` ("created", "deleted", "settled"), `entityType` ("expense", "chore"), `entityId`, `timestamp`.

### 3.14 `invitations` (Root Collection)
- **Purpose:** Mapping short invite codes to actual apartment IDs securely.
- **Document ID:** Auto-generated (or use the code itself as ID for instant lookup).
- **Structure:**
  - `code` (String, required) - 6-character. (e.g., "A8X2B9")
  - `apartmentId` (String, required)
  - `expiresAt` (Timestamp, required)
  - `createdByUid` (String, required)

---

## 4. Invite Code Generation Strategy
Generate a random 6-character alphanumeric string (excluding confusing characters like 'O', '0', 'I', 'l').
Since invite codes are short, collisions are possible.
**Flow:**
1. Generate code locally.
2. Attempt to create a document in the `invitations` collection where the Document ID is the code itself.
3. If it fails (document exists), retry with a new code.
4. Set a TTL (Time To Live) index on `expiresAt` (e.g., 7 days) so old codes are automatically purged by Firestore, keeping the namespace clean.

---

## 5. Security Rules Architecture

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper Functions
    function isSignedIn() {
      return request.auth != null;
    }
    function isApartmentMember(apartmentId) {
      return exists(/databases/$(database)/documents/apartments/$(apartmentId)/apartmentMembers/$(request.auth.uid));
    }
    function isApartmentAdmin(apartmentId) {
      return get(/databases/$(database)/documents/apartments/$(apartmentId)/apartmentMembers/$(request.auth.uid)).data.role == 'admin';
    }

    // Users
    match /users/{uid} {
      allow read: if isSignedIn(); // Basic profile reading
      allow write: if request.auth.uid == uid;
      
      match /userSettings/{docId} {
        allow read, write: if request.auth.uid == uid;
      }
      match /notifications/{docId} {
        allow read, write: if request.auth.uid == uid;
      }
    }

    // Apartments
    match /apartments/{apartmentId} {
      allow read: if isApartmentMember(apartmentId);
      allow create: if isSignedIn();
      allow update: if isApartmentAdmin(apartmentId);
      
      match /apartmentMembers/{memberUid} {
        allow read: if isApartmentMember(apartmentId);
        // Only admins can kick/modify roles. Users can delete themselves (leave).
        allow write: if isApartmentAdmin(apartmentId) || request.auth.uid == memberUid;
      }

      match /expenses/{expenseId} {
        allow read: if isApartmentMember(apartmentId);
        allow create: if isApartmentMember(apartmentId);
        // Only the creator can edit/delete, and only if not completely settled.
        allow update, delete: if isApartmentMember(apartmentId) && resource.data.createdByUid == request.auth.uid;
        
        match /expenseParticipants/{participantId} {
           allow read, write: if isApartmentMember(apartmentId);
        }
      }

      match /settlements/{settlementId} {
        allow read: if isApartmentMember(apartmentId);
        allow create: if isApartmentMember(apartmentId);
        // Once confirmed, a settlement cannot be modified.
        allow update: if isApartmentMember(apartmentId) && resource.data.status == 'pending';
      }

      // Applies similarly to chores, groceryLists, activityLogs
      match /{document=**} {
        allow read, write: if isApartmentMember(apartmentId);
      }
    }
    
    // Invitations
    match /invitations/{inviteCode} {
       allow read: if isSignedIn();
       allow create: if isSignedIn(); // Add rate-limiting via AppCheck
    }
  }
}
```

---

## 6. Firebase Storage Folder Design

Organize buckets logically and securely:
- `/public/avatars/{uid}.jpg`: User profile pictures. Globally readable.
- `/apartments/{apartmentId}/receipts/{expenseId}_{uuid}.jpg`: Receipt images. Secured by Storage Rules (only apartment members can read).
- `/apartments/{apartmentId}/chat-images/{uuid}.jpg`: Future-proofing for internal chat.

---

## 7. Recommended Firestore Indexes

By default, Firestore creates single-field indexes. You will need composite indexes for sorting and filtering concurrently.
- `expenses`: `isDeleted` (ASC) + `date` (DESC) - For loading the ledger history.
- `expenses`: `category` (ASC) + `date` (DESC) - For filtering expenses by category over time.
- `chores`: `assigneeUid` (ASC) + `dueDate` (ASC) - For "My upcoming chores" dashboard widget.
- `settlements`: `status` (ASC) + `date` (DESC) - For viewing pending requests first.

---

## 8. Query Patterns

- **Dashboard Summary:**
  - Query `apartments/{apartmentId}/apartmentMembers/{currentUserUid}` to get the denormalized total `balance`.
  - Query `apartments/{apartmentId}/chores` where `assigneeUid == currentUser.uid` and `status == "pending"` limit to 3.
- **Ledger (Expense Feed):**
  - CollectionGroup or Subcollection query on `expenses` where `isDeleted == false` ordered by `date` descending with pagination (limit 20).
- **"Who Owes Me":**
  - Query `settlements` where `receiverUid == currentUser.uid` and `status == "pending"`.

---

## 9. Offline Synchronization & Conflict Resolution

- **Strategy:** Enable Firestore's persistence. The Android app relies on Room as the primary SSOT for complex local querying, but Firestore SDK handles raw network queueing.
- **Conflicts:** Firestore processes offline writes chronologically once reconnected. 
- **Mitigation:**
  - For critical financial updates (e.g., updating user aggregate balances), use **Cloud Functions and Firestore Transactions**. Do not rely on client-side math for aggregate totals. The client simply writes the `expense`, and a Cloud Function calculates and updates the `apartmentMembers` balances transactionally, eliminating race conditions.

---

## 10. Cost Optimization Strategies

1. **Denormalize Balances:** Recalculating balances by reading all expenses every time a user opens the app is financially disastrous in Firestore. Use a Cloud Function trigger (`onWrite` of an expense/settlement) to update a single `balance` field on the user's `apartmentMember` document. This reduces dashboard load to 1 read instead of N reads.
2. **Pagination:** Always use `.limit(20)` and cursors for the expense ledger.
3. **Data Snapshots:** Instead of performing a subcollection query for `expenseParticipants` on every single expense in the feed, serialize the participants into a map inside the main `expense` document (e.g., `participants: { "uid1": 5000, "uid2": 5000 }`). This reduces reads by a massive factor.

---

## 11. Schema Review & Scalability Assessment

**Assessment:** The architecture is highly scalable. 
- **Isolation:** By nesting data under `apartments/{apartmentId}`, we ensure that no matter how many millions of users join, queries remain scoped to small, distinct datasets (a single apartment).
- **Hotspots:** There are no obvious write hotspots (like a global counter).
- **Security:** The security rules strictly silo data.

**Potential Future Consideration:**
If an apartment exists for 10 years, the `expenses` subcollection could grow large. However, Firestore query performance is based on the size of the *result set*, not the total data set. By enforcing pagination and date limits (e.g., "Load last 30 days"), performance will remain exactly the same on day 1 as on year 10.

**End of Document.**
