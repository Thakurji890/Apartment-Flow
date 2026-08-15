package com.example.feature.analytics.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.analytics.domain.model.*
import com.example.feature.analytics.domain.usecase.AnalyticsUseCases
import com.example.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class AnalyticsState(
    val apartmentId: String = "",
    val isLoading: Boolean = true,
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.THIS_MONTH,
    val summary: AnalyticsSummary = AnalyticsSummary(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val memberSpending: List<MemberSpending> = emptyList(),
    val trends: List<TrendPoint> = emptyList(),
    val error: String? = null,
    val personalSpending: PersonalSpending = PersonalSpending(0.0, 0.0, 0.0, 0.0, 0, 0.0)
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val useCases: AnalyticsUseCases,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle.get<String>("apartmentId"))
    private val _state = MutableStateFlow(AnalyticsState(apartmentId = apartmentId))
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun setPeriod(period: AnalyticsPeriod) {
        _state.update { it.copy(selectedPeriod = period, isLoading = true) }
        loadData()
    }

    private fun loadData() {
        val period = _state.value.selectedPeriod
        val bounds = getPeriodBounds(period)
        val start = bounds.first
        val end = bounds.second
        
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch

            combine(
                useCases.getAnalyticsSummary(apartmentId, start, end),
                useCases.getCategorySpending(apartmentId, start, end),
                useCases.getMemberSpending(apartmentId, start, end),
                useCases.getTrends(apartmentId, start, end)
            ) { sumRes, catRes, memRes, trendRes ->
                if (sumRes is Resource.Error) {
                    _state.value.copy(isLoading = false, error = sumRes.message)
                } else {
                    val members = (memRes as? Resource.Success)?.data ?: emptyList()
                    val mySpending = members.find { it.userId == user.uid }
                    val pSpending = if (mySpending != null) {
                        PersonalSpending(
                            totalShare = mySpending.actualShare,
                            amountPaid = mySpending.amountPaid,
                            debtOutstanding = if (mySpending.netBalance < 0) -mySpending.netBalance else 0.0,
                            owedToMe = if (mySpending.netBalance > 0) mySpending.netBalance else 0.0,
                            expenseCount = mySpending.expenseCount,
                            averagePersonalExpense = if (mySpending.expenseCount > 0) mySpending.actualShare / mySpending.expenseCount else 0.0
                        )
                    } else {
                        PersonalSpending(0.0, 0.0, 0.0, 0.0, 0, 0.0)
                    }
                    
                    _state.value.copy(
                        isLoading = false,
                        error = null,
                        summary = (sumRes as? Resource.Success)?.data ?: AnalyticsSummary(),
                        categorySpending = (catRes as? Resource.Success)?.data ?: emptyList(),
                        memberSpending = members,
                        trends = (trendRes as? Resource.Success)?.data ?: emptyList(),
                        personalSpending = pSpending
                    )
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun getPeriodBounds(period: AnalyticsPeriod): Pair<Long, Long> {
        val now = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        
        val start: LocalDate
        val end: LocalDate = now
        
        when (period) {
            AnalyticsPeriod.THIS_WEEK -> {
                start = now.minusDays(now.dayOfWeek.value.toLong() - 1)
            }
            AnalyticsPeriod.THIS_MONTH -> {
                start = now.withDayOfMonth(1)
            }
            AnalyticsPeriod.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                start = lastMonth.withDayOfMonth(1)
                return Pair(
                    start.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    lastMonth.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()
                )
            }
            AnalyticsPeriod.LAST_3_MONTHS -> {
                start = now.minusMonths(3).withDayOfMonth(1)
            }
            AnalyticsPeriod.LAST_6_MONTHS -> {
                start = now.minusMonths(6).withDayOfMonth(1)
            }
            AnalyticsPeriod.THIS_YEAR -> {
                start = now.withDayOfYear(1)
            }
            AnalyticsPeriod.CUSTOM -> {
                // Default to last 30 days for custom if not fully implemented picker
                start = now.minusDays(30)
            }
        }
        
        return Pair(
            start.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            end.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()
        )
    }
}
