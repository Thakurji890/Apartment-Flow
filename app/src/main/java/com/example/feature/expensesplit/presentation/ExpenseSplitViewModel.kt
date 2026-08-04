package com.example.feature.expensesplit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.model.SplitType
import com.example.feature.expensesplit.domain.usecase.CalculateSplitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ExpenseSplitState(
    val totalAmount: Double = 0.0,
    val splitType: SplitType = SplitType.EQUAL,
    val splits: List<Split> = emptyList(),
    val isValid: Boolean = true,
    val remainingAmount: Double = 0.0
)

@HiltViewModel
class ExpenseSplitViewModel @Inject constructor(
    private val calculateSplitsUseCase: CalculateSplitsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseSplitState())
    val state: StateFlow<ExpenseSplitState> = _state.asStateFlow()

    fun initSplits(totalAmount: Double, userIds: List<String>) {
        val initialSplits = userIds.map { Split(userId = it, type = SplitType.EQUAL) }
        val calculated = calculateSplitsUseCase(totalAmount, initialSplits, SplitType.EQUAL)
        _state.update {
            it.copy(
                totalAmount = totalAmount,
                splitType = SplitType.EQUAL,
                splits = calculated,
                remainingAmount = 0.0,
                isValid = true
            )
        }
    }

    fun onSplitTypeChanged(type: SplitType) {
        val currentSplits = _state.value.splits.map { it.copy(type = type) }
        val calculated = calculateSplitsUseCase(_state.value.totalAmount, currentSplits, type)
        _state.update {
            it.copy(
                splitType = type,
                splits = calculated
            )
        }
        validateSplits(calculated)
    }

    fun onSplitValueChanged(userId: String, value: Double) {
        val currentState = _state.value
        val updatedSplits = currentState.splits.map {
            if (it.userId == userId) {
                if (currentState.splitType == SplitType.EXACT || currentState.splitType == SplitType.CUSTOM) {
                    it.copy(amount = value)
                } else {
                    it.copy(value = value)
                }
            } else {
                it
            }
        }
        
        val calculated = if (currentState.splitType == SplitType.EXACT || currentState.splitType == SplitType.CUSTOM) {
            updatedSplits
        } else {
            calculateSplitsUseCase(currentState.totalAmount, updatedSplits, currentState.splitType)
        }

        _state.update { it.copy(splits = calculated) }
        validateSplits(calculated)
    }

    private fun validateSplits(splits: List<Split>) {
        val totalAmount = _state.value.totalAmount
        val splitsSum = splits.sumOf { it.amount }
        val diff = totalAmount - splitsSum
        val isValid = Math.abs(diff) < 0.01 // allow 1 cent precision issues if manually typed
        
        _state.update { 
            it.copy(
                isValid = isValid,
                remainingAmount = diff
            ) 
        }
    }
}
