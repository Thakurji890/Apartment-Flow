sed -i '/fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> =/d' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
sed -i '/recurringBillRepository.getRecurringBills(apartmentId)/d' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
sed -i '$d' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
sed -i '$d' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt

cat << 'INNER' >> app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
    }

    fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> =
        recurringBillRepository.getRecurringBills(apartmentId)
}
INNER
