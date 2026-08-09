awk '
/insertRecurringBill/ { inInsert = 1 }
/updateRecurringBill/ { inUpdate = 1 }
/deleteRecurringBill/ { inDelete = 1 }
/skipOccurrence/ { inSkip = 1 }

/Resource\.Success\(expenseId\)/ {
    if (inInsert || inUpdate || inDelete || inSkip) {
        sub("Resource.Success(expenseId)", "Resource.Success(Unit)")
        if (inInsert) inInsert = 0
        if (inUpdate) inUpdate = 0
        if (inDelete) inDelete = 0
        if (inSkip) inSkip = 0
    }
}
{ print }
' app/src/main/java/com/example/feature/recurringbill/data/repository/RecurringBillRepositoryImpl.kt > temp.kt && mv temp.kt app/src/main/java/com/example/feature/recurringbill/data/repository/RecurringBillRepositoryImpl.kt
