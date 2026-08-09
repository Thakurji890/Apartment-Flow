sed -i 's/import com.example.feature.settlement.domain.model.SettlementStatus/import com.example.feature.settlement.domain.model.SettlementStatus\nimport com.example.feature.recurringbill.domain.repository.RecurringBillRepository\nimport com.example.feature.recurringbill.domain.model.RecurringBill/g' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
sed -i 's/private val splitRepository: ExpenseSplitRepository/private val splitRepository: ExpenseSplitRepository,\n    private val recurringBillRepository: RecurringBillRepository/g' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt

cat << 'INNER_EOF' >> add_func.sh
cat << 'TEXT' >> append.txt

    fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> =
        recurringBillRepository.getRecurringBills(apartmentId)
TEXT
sed -i '/fun getMembers(apartmentId: String) =/r append.txt' app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt
INNER_EOF
bash add_func.sh
