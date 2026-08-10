sed -i 's/useCases.skipOccurrence(bill)/useCases.skipOccurrence(bill.id, bill.nextDueDate)/g' app/src/main/java/com/example/feature/recurringbill/presentation/details/RecurringBillDetailsViewModel.kt
