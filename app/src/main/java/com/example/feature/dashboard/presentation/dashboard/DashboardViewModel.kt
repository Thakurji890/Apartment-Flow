package com.example.feature.dashboard.presentation.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.dashboard.domain.usecase.DashboardUseCases
import com.example.feature.dashboard.domain.model.ActivityItem
import com.example.feature.recurringbill.domain.model.RecurringBill
import com.example.feature.settlement.domain.model.SettlementStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val useCases: DashboardUseCases,
    private val authRepository: AuthRepository,
    private val apartmentRepository: ApartmentRepository, // Needed to fetch current apartment and user's apartments
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle.get<String>("apartmentId"))

    private val _state = MutableStateFlow(DashboardUiState(apartmentId = apartmentId))
    val state = _state.asStateFlow()

    private val _filterMonth = MutableStateFlow(YearMonth.now())
    private val _searchQuery = MutableStateFlow("")

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val currentUser = authRepository.getCurrentUser()
        _state.update { it.copy(currentUser = currentUser) }
        
        viewModelScope.launch {
            // Fetch apartment details
            when (val aptRes = apartmentRepository.getApartment(apartmentId)) {
                is Resource.Success -> {
                    _state.update { it.copy(apartmentName = aptRes.data?.name ?: "Apartment") }
                }
                else -> {}
            }
            // Fetch user's apartments for switcher
            apartmentRepository.getUserApartments().collectLatest { aptsRes ->
                if (aptsRes is Resource.Success) {
                    _state.update { it.copy(apartments = aptsRes.data ?: emptyList()) }
                }
            }
        }

        // Start combining flows for the dashboard
        viewModelScope.launch {
            val currentUserId = currentUser?.uid ?: return@launch
            
            combine(_filterMonth, _searchQuery) { month, query ->
                Pair(month, query)
            }.flatMapLatest { (currentMonth, query) ->
                val startOfMonth = currentMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val flow1 = combine(
                    useCases.getExpensesForMonth(apartmentId, startOfMonth, endOfMonth),
                    useCases.getSettlementsForMonth(apartmentId, startOfMonth, endOfMonth),
                    useCases.getMemberBalances(apartmentId)
                ) { expRes, setRes, balRes ->
                    Triple(expRes, setRes, balRes)
                }
                
                val flow2 = combine(
                    useCases.getDebts(apartmentId),
                    useCases.getMembers(apartmentId),
                    useCases.getRecentExpenses(apartmentId, 5)
                ) { debtRes, memRes, recExpRes ->
                    Triple(debtRes, memRes, recExpRes)
                }
                
                val flow3 = combine<List<ActivityItem>, Resource<List<RecurringBill>>, Pair<List<ActivityItem>, Resource<List<RecurringBill>>>>(
                    useCases.getRecentActivity(apartmentId, 10),
                    useCases.getRecurringBills(apartmentId)
                ) { acts, bills -> Pair(acts, bills) }

                combine(flow1, flow2, flow3) { t1, t2, t3 ->
                    val (expRes, setRes, balRes) = t1
                    val (debtRes, memRes, recExpRes) = t2
                    val (activities, recBillsRes) = t3
                    
                    val expenses = (expRes as? Resource.Success)?.data ?: emptyList()
                    val settlements = (setRes as? Resource.Success)?.data ?: emptyList()
                    val balances = (balRes as? Resource.Success)?.data ?: emptyList()
                    val debts = (debtRes as? Resource.Success)?.data ?: emptyList()
                    val members = (memRes as? Resource.Success)?.data ?: emptyList()
                    val recentExpenses = (recExpRes as? Resource.Success)?.data ?: emptyList()
                    val recurringBills = (recBillsRes as? Resource.Success<List<RecurringBill>>)?.data ?: emptyList()

                    // Handle Search Filtering Locally
                    val q = query.lowercase()
                    val filteredActivities = if (q.isBlank()) activities else activities.filter { 
                        it.title.lowercase().contains(q) || members.find { m -> m.userId == it.userId }?.displayName?.lowercase()?.contains(q) == true
                    }
                    val filteredExpenses = if (q.isBlank()) expenses else expenses.filter {
                        it.title.lowercase().contains(q) || it.paidBy.lowercase().contains(q)
                    }

                    val myBalance = balances.find { it.userId == currentUserId }
                    val currentMember = members.find { it.userId == currentUserId }

                    // Monthly Summary calculations
                    val monthTotal = expenses.sumOf { it.amount }
                    val daysInMonth = LocalDate.now().dayOfMonth.coerceAtLeast(1)
                    val avgDaily = if (monthTotal > 0) monthTotal / daysInMonth else 0.0
                    val highest = expenses.maxOfOrNull { it.amount } ?: 0.0
                    val activeMembersCount = members.count { it.status == "ACTIVE" }

                    // Settlement Summary
                    val pendingCount = settlements.count { it.status == SettlementStatus.PENDING }
                    val settledThisMonth = settlements.count { it.status == SettlementStatus.CONFIRMED }
                    val paidThisMonth = settlements.filter { it.status == SettlementStatus.CONFIRMED && it.debtorId == currentUserId }.sumOf { it.amount }
                    val receivedThisMonth = settlements.filter { it.status == SettlementStatus.CONFIRMED && it.creditorId == currentUserId }.sumOf { it.amount }
                    
                    val totalSpentByUserThisMonth = expenses.filter { it.paidBy == currentUserId }.sumOf { it.amount }

                    _state.value.copy(
                        isLoading = false,
                        currentMember = currentMember,
                        currentMonth = currentMonth,
                        searchQuery = query,
                        
                        amountYouOwe = myBalance?.totalOwed ?: 0.0,
                        amountOwedToYou = myBalance?.totalToReceive ?: 0.0,
                        netBalance = myBalance?.netBalance ?: 0.0,
                        totalSpent = totalSpentByUserThisMonth,
                        
                        currentMonthExpenses = monthTotal,
                        averageDailySpending = avgDaily,
                        highestExpense = highest,
                        numberOfExpenses = expenses.size,
                        numberOfActiveMembers = activeMembersCount,
                        
                        recentExpenses = if (q.isBlank()) recentExpenses else filteredExpenses.take(5),
                        outstandingDebts = debts,
                        members = members,
                        memberBalances = balances,
                        
                        pendingSettlementsCount = pendingCount,
                        settledThisMonthCount = settledThisMonth,
                        amountPaidThisMonth = paidThisMonth,
                        amountReceivedThisMonth = receivedThisMonth,
                        
                        recentActivities = filteredActivities,
                        recurringBills = recurringBills,
                        error = null
                    )
                }
            }.catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
    
    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        // Re-trigger flow by emitting current values
        _filterMonth.value = _filterMonth.value
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }
    
    fun previousMonth() {
        _filterMonth.value = _filterMonth.value.minusMonths(1)
    }
    
    fun nextMonth() {
        if (_filterMonth.value.isBefore(YearMonth.now())) {
            _filterMonth.value = _filterMonth.value.plusMonths(1)
        }
    }
}
