cat << 'TEXT' >> append.txt

    fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> =
        recurringBillRepository.getRecurringBills(apartmentId)
TEXT
sed -i '/fun getMembers(apartmentId: String) =/r append.txt' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
