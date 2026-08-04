# Expense Splitting Module

## Overview
This module handles all logic for dividing expenses between apartment members and calculating resulting debts. It adheres to Clean Architecture with clear domain, data, and presentation layers.

## Features Supported
- **Equal Split**: Evenly divides the total among selected members.
- **Exact Split**: Members are assigned exact amounts (validated to match total).
- **Percentage Split**: Divides based on given percentages (validated to 100%).
- **Share-based Split**: Divides proportionally based on assigned shares.
- **Custom Split**: Arbitrary assignments.

## Calculation Algorithm
The `CalculationEngine` operates on **cents** (Long) instead of floating-point dollars to avoid precision issues (e.g. $10.00 / 3).
1. It calculates the exact cent amount for each member based on their share/percentage.
2. Any remaining cents caused by indivisible numbers (e.g. 1000 cents / 3 = 333 cents with 1 remainder) are distributed 1 cent at a time to members until the remainder is 0. 
3. This guarantees `Sum(Split Amounts) == Total Expense Amount`.

## Balance Engine Algorithm
The `BalanceEngine` takes `Payments` (who paid) and `Splits` (who owes what part) to determine net balances.
1. Each user's net balance = `Amount Paid - Amount Owed`.
2. Positive net balance = Creditor (is owed money).
3. Negative net balance = Debtor (owes money).
4. The algorithm then matches Debtors to Creditors greedily (highest debtor to highest creditor) to generate the minimum number of direct `Debt` transactions to settle the expense.

## Concurrency & Data Integrity
When saving splits, `ExpenseSplitRepositoryImpl` uses Firestore Transactions. 
- It guarantees that reading current member balances and applying the new debts happens atomically.
- This prevents race conditions if multiple people edit expenses simultaneously.
- When editing or deleting an expense, the previous debts are rolled back before the new ones are applied.

## Testing
Comprehensive Robolectric/JUnit tests exist for:
- CalculationEngine (rounding, equal, exact, percentage, shares).
- BalanceEngine (single payer, multi-payer complex scenarios).
