package com.example.feature.expense.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.usecase.ApartmentUseCases
import com.example.feature.expense.domain.model.DefaultCategories
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.model.ExpenseCategory
import com.example.feature.expense.domain.usecase.ExpenseUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val expenseUseCases: ExpenseUseCases,
    private val apartmentUseCases: ApartmentUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId = savedStateHandle.get<String>("expenseId") ?: ""
    private val apartmentId = savedStateHandle.get<String>("apartmentId") ?: ""

    private val _state = MutableStateFlow(EditExpenseState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditExpenseUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentExpense: Expense? = null

    init {
        loadData()
    }

    private fun loadData() {
        if (expenseId.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Load members
            if (apartmentId.isNotBlank()) {
                apartmentUseCases.getApartmentMembers(apartmentId).collect { result ->
                    if (result is Resource.Success) {
                        _state.update { it.copy(members = result.data ?: emptyList()) }
                    }
                }
            }

            // Load expense
            expenseUseCases.getExpense(expenseId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val expense = result.data
                        if (expense != null) {
                            currentExpense = expense
                            _state.update {
                                it.copy(
                                    title = expense.title,
                                    description = expense.description,
                                    amount = expense.amount.toString(),
                                    category = DefaultCategories.getCategoryById(expense.categoryId),
                                    paidByUserId = expense.paidBy,
                                    date = expense.expenseDate,
                                    notes = expense.notes,
                                    isRecurring = expense.isRecurring,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _eventFlow.emit(EditExpenseUiEvent.ShowSnackbar(result.message ?: "Failed to load"))
                    }
                    else -> {}
                }
            }
        }
    }

    fun onEvent(event: EditExpenseEvent) {
        when (event) {
            is EditExpenseEvent.TitleChanged -> _state.update { it.copy(title = event.title, titleError = null) }
            is EditExpenseEvent.DescriptionChanged -> _state.update { it.copy(description = event.description) }
            is EditExpenseEvent.AmountChanged -> _state.update { it.copy(amount = event.amount, amountError = null) }
            is EditExpenseEvent.CategoryChanged -> _state.update { it.copy(category = event.category) }
            is EditExpenseEvent.PaidByChanged -> _state.update { it.copy(paidByUserId = event.userId, paidByError = null) }
            is EditExpenseEvent.DateChanged -> _state.update { it.copy(date = event.date) }
            is EditExpenseEvent.NotesChanged -> _state.update { it.copy(notes = event.notes) }
            is EditExpenseEvent.IsRecurringChanged -> _state.update { it.copy(isRecurring = event.isRecurring) }
            is EditExpenseEvent.Submit -> submitExpense()
        }
    }

    private fun submitExpense() {
        if (currentExpense == null) return
        
        val amountValue = state.value.amount.toDoubleOrNull()
        
        val isTitleValid = state.value.title.isNotBlank()
        val isAmountValid = amountValue != null && amountValue > 0
        val isPaidByValid = state.value.paidByUserId.isNotBlank()
        
        if (!isTitleValid || !isAmountValid || !isPaidByValid) {
            _state.update {
                it.copy(
                    titleError = if (!isTitleValid) "Title is required" else null,
                    amountError = if (!isAmountValid) "Valid amount > 0 required" else null,
                    paidByError = if (!isPaidByValid) "Please select who paid" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            val updatedExpense = currentExpense!!.copy(
                title = state.value.title,
                description = state.value.description,
                amount = amountValue ?: 0.0,
                categoryId = state.value.category.id,
                paidBy = state.value.paidByUserId,
                expenseDate = state.value.date,
                notes = state.value.notes,
                isRecurring = state.value.isRecurring
            )
            
            when (val result = expenseUseCases.updateExpense(updatedExpense)) {
                is Resource.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    _eventFlow.emit(EditExpenseUiEvent.Success)
                }
                is Resource.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _eventFlow.emit(EditExpenseUiEvent.ShowSnackbar(result.message ?: "Update failed"))
                }
                else -> {}
            }
        }
    }
}

data class EditExpenseState(
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val amount: String = "",
    val amountError: String? = null,
    val category: ExpenseCategory = DefaultCategories.getCategoryById("others"),
    val paidByUserId: String = "",
    val paidByError: String? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isRecurring: Boolean = false,
    val members: List<ApartmentMember> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

sealed class EditExpenseEvent {
    data class TitleChanged(val title: String) : EditExpenseEvent()
    data class DescriptionChanged(val description: String) : EditExpenseEvent()
    data class AmountChanged(val amount: String) : EditExpenseEvent()
    data class CategoryChanged(val category: ExpenseCategory) : EditExpenseEvent()
    data class PaidByChanged(val userId: String) : EditExpenseEvent()
    data class DateChanged(val date: Long) : EditExpenseEvent()
    data class NotesChanged(val notes: String) : EditExpenseEvent()
    data class IsRecurringChanged(val isRecurring: Boolean) : EditExpenseEvent()
    object Submit : EditExpenseEvent()
}

sealed class EditExpenseUiEvent {
    data class ShowSnackbar(val message: String) : EditExpenseUiEvent()
    object Success : EditExpenseUiEvent()
}
