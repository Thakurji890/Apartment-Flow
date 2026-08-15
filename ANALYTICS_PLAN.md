# Analytics Module Architecture & Strategy

## 1. Analytics Architecture
- **Layered Architecture:** Follows MVVM + Clean Architecture (`AnalyticsRepository` -> Use Cases -> `AnalyticsViewModel` -> Jetpack Compose UI).
- **No Duplicate Logic:** Strictly leverages the existing Kotlin `CalculationEngine` for splits and shares. Cloud Functions are NOT used to duplicate debt logic.
- **Data Integrity:** Only references trusted existing collections: `expenses`, `settlements`, `recurringBills`.

## 2. Firestore Aggregation Schema & Strategy
- **Scalable Reads:** Uses Firebase SDK's native `AggregateQuery` (`AggregateField.sum("amount")`, `count()`) to calculate global sums (e.g., total apartment spending) without downloading thousands of documents.
- **Custom Filtering:** For custom date ranges (This Month, Last Month, etc.), the client queries the `expenses` collection using bounded `expenseDate` filters. Since users rarely generate more than a few hundred expenses a month, downloading these documents is highly efficient, precise, and easily allows generating categories, trends, and member splits on the fly.
- **Precomputed Summaries:** If needed, a future Cloud Function can write to `apartments/{id}/analytics/monthly_{YYYY_MM}` for infinite scalability. However, native `sum()` aggregation handles scale gracefully.

## 3. Data Flow
1. User selects a Time Period in `AnalyticsScreen`.
2. `AnalyticsViewModel` requests data from Use Cases.
3. Use Cases fetch `Expense`, `Settlement`, and `Member` lists scoped to the date bounds.
4. Kotlin collection aggregations build `CategorySpending`, `MemberSpending`, and `TrendPoint` models.
5. The UI reacts to the updated `AnalyticsUiState`.

## 4. Calculations Defined
- **Amount Paid:** `expenses.filter { it.paidBy == userId }.sumOf { it.amount }`
- **Actual Share:** Uses existing `CalculationEngine.calculateSplits()` based on EQUAL distribution among active members.
- **Outstanding:** The difference between Amount Paid and Actual Share.
- **Settlement Amount:** `settlements.filter { it.status == CONFIRMED }.sumOf { it.amount }`

## 5. Security Rules
- Relies on the existing `apartmentId` scoping. The client only queries documents explicitly tagged with the user's `apartmentId`, which is protected by existing rules.

## 6. Caching & Offline
- Uses Firestore's native local caching. The analytics will display "Last updated [time]" visually if network is unavailable (using a network state monitor or flow).

## 7. Export Architecture
- `AnalyticsExportUtil`: A generic CSV builder that converts the aggregated `AnalyticsUiState` into a text file.
- Uses `FileProvider` to write `report_YYYY_MM.csv` into the cache directory and triggers `Intent.ACTION_SEND` (Android Share Sheet).

## 8. Required Indexes
- `apartmentId` (ASC) + `expenseDate` (DESC) -> Already exists for Dashboard!
- `apartmentId` (ASC) + `paymentDate` (DESC) -> Already exists for Settlements!
