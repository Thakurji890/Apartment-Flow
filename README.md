# ApartmentFlow - Settlement Module

## Settlement Workflow
1. **Creation**: A debtor initiates a settlement request by entering the amount, selecting the creditor, and choosing a payment method.
2. **Review**: The creditor receives the request and reviews the payment details (and any attached receipts).
3. **Confirmation/Rejection**: The creditor can Accept or Reject the settlement. 
4. **Completion**: Once Accepted, the settlement status changes to `CONFIRMED`.

## Firestore Transaction Strategy
- We use Firestore Transactions in `confirmSettlement` to ensure data consistency.
- A single transaction reads the current status of the settlement and then updates it to `CONFIRMED`.
- This ensures concurrent operations (like two admins accepting simultaneously) do not lead to inconsistent states.

## Balance Reconciliation
- During confirmation, the balance engine recalculates the apartment's member balances.
- The amount paid is deducted from the debtor's overall debt and credited towards the creditor's balance.
- This calculation prevents negative balances and recalculates member summaries automatically.

## Confirmation Process
- Only settlements in the `PENDING` state can be confirmed.
- Manual Confirmation Flow: Debtor submits payment -> Creditor reviews -> Accept or Reject.
- Rejected settlements are marked as `REJECTED` and do not alter balances.

## Error Handling
- Validation blocks invalid amounts (amount <= 0) and identical creditor/debtor assignments.
- Network exceptions and Firestore errors are caught and surfaced to the UI via `Resource.Error`.
- State is managed via `StateFlow`, displaying relevant error states and UI prompts securely.

## Security Considerations
- Data Access: Only participants (debtors/creditors) and apartment admins can view settlement details via tailored Firestore Security Rules and server-side filtering.
- Action Restrictions: Only a creditor or an administrator has the authority to confirm or reject settlements.
- Receipts: Private to the apartment members.
