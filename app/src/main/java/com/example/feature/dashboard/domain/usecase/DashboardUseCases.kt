package com.example.feature.dashboard.domain.usecase

import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.dashboard.domain.model.ActivityItem
import com.example.feature.dashboard.domain.model.ActivityType
import com.example.feature.dashboard.domain.repository.DashboardRepository
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expensesplit.domain.repository.ExpenseSplitRepository
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.model.SettlementStatus
import com.example.feature.recurringbill.domain.repository.RecurringBillRepository
import com.example.feature.recurringbill.domain.model.RecurringBill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DashboardUseCases @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val apartmentRepository: ApartmentRepository,
    private val splitRepository: ExpenseSplitRepository,
    private val recurringBillRepository: RecurringBillRepository
) {

    fun getRecentExpenses(apartmentId: String, limit: Int = 5) =
        dashboardRepository.getRecentExpenses(apartmentId, limit)

    fun getExpensesForMonth(apartmentId: String, startTimestamp: Long, endTimestamp: Long) =
        dashboardRepository.getExpensesForMonth(apartmentId, startTimestamp, endTimestamp)

    fun getSettlementsForMonth(apartmentId: String, startTimestamp: Long, endTimestamp: Long) =
        dashboardRepository.getSettlementsForMonth(apartmentId, startTimestamp, endTimestamp)

    fun getMemberBalances(apartmentId: String) =
        splitRepository.getMemberBalances(apartmentId)

    fun getDebts(apartmentId: String) =
        splitRepository.getDebtsForApartment(apartmentId)
        
    fun getMembers(apartmentId: String) =
        apartmentRepository.getApartmentMembers(apartmentId)


    /**
     * Aggregates recent activity by combining recent expenses, settlements, and member joins.
     */
    fun getRecentActivity(apartmentId: String, limit: Int = 10): Flow<List<ActivityItem>> {
        val expensesFlow = dashboardRepository.getRecentExpenses(apartmentId, limit)
        // For settlements we want recent ones, we can just grab month settlements and take top N
        val end = System.currentTimeMillis()
        val start = end - (30L * 24 * 60 * 60 * 1000) // last 30 days roughly
        val settlementsFlow = dashboardRepository.getSettlementsForMonth(apartmentId, start, end)
        val membersFlow = apartmentRepository.getApartmentMembers(apartmentId)
        
        return combine(expensesFlow, settlementsFlow, membersFlow) { expRes, setRes, memRes ->
            val activities = mutableListOf<ActivityItem>()
            
            (expRes as? Resource.Success)?.data?.forEach { exp ->
                activities.add(
                    ActivityItem(
                        id = "exp_${exp.expenseId}",
                        type = if (exp.createdAt == exp.updatedAt) ActivityType.EXPENSE_ADDED else ActivityType.EXPENSE_UPDATED,
                        userId = exp.paidBy,
                        title = exp.title,
                        amount = exp.amount,
                        timestamp = exp.updatedAt
                    )
                )
            }
            
            (setRes as? Resource.Success)?.data?.forEach { set ->
                activities.add(
                    ActivityItem(
                        id = "set_${set.settlementId}",
                        type = if (set.status == SettlementStatus.CONFIRMED) ActivityType.SETTLEMENT_CONFIRMED else ActivityType.SETTLEMENT_CREATED,
                        userId = set.debtorId,
                        title = "Settlement to ${set.creditorId}",
                        amount = set.amount,
                        timestamp = set.updatedAt
                    )
                )
            }
            
            (memRes as? Resource.Success)?.data?.forEach { mem ->
                activities.add(
                    ActivityItem(
                        id = "mem_${mem.id}",
                        type = ActivityType.MEMBER_JOINED,
                        userId = mem.userId,
                        title = "Joined apartment",
                        amount = null,
                        timestamp = mem.joinedAt
                    )
                )
            }
            
            activities.sortedByDescending { it.timestamp }.take(limit)
        }
    }

    fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> =
        recurringBillRepository.getRecurringBills(apartmentId)
}
