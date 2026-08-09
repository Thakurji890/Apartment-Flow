awk '
/override suspend fun generateExpenseForBill/,/return Resource/ {
    if ($0 ~ /Resource.Success\(Unit\)/) {
        sub("Resource.Success\\(Unit\\)", "Resource.Success(expenseId)")
    }
}
{ print }
' app/src/main/java/com/example/feature/recurringbill/data/repository/RecurringBillRepositoryImpl.kt > temp.kt && mv temp.kt app/src/main/java/com/example/feature/recurringbill/data/repository/RecurringBillRepositoryImpl.kt
