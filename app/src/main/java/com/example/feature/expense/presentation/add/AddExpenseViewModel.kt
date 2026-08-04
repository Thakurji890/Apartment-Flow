package com.example.feature.expense.presentation.add

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
class AddExpenseViewModel @Inject constructor(
    private val expenseUseCases: ExpenseUseCases,
    private val apartmentUseCases: ApartmentUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId = savedStateHandle.get<String>("apartmentId") ?: ""

    private val _state = MutableStateFlow(AddExpenseState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddExpenseUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            apartmentUseCases.getApartmentMembers(apartmentId).collect { result ->
                if (result is Resource.Success) {
                    _state.update { it.copy(members = result.data ?: emptyList()) }
                }
            }
        }
    }

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.TitleChanged -> _state.update { it.copy(title = event.title, titleError = null) }
            is AddExpenseEvent.DescriptionChanged -> _state.update { it.copy(description = event.description) }
            is AddExpenseEvent.AmountChanged -> _state.update { it.copy(amount = event.amount, amountError = null) }
            is AddExpenseEvent.CategoryChanged -> _state.update { it.copy(category = event.category) }
            is AddExpenseEvent.PaidByChanged -> _state.update { it.copy(paidByUserId = event.userId, paidByError = null) }
            is AddExpenseEvent.DateChanged -> _state.update { it.copy(date = event.date) }
            is AddExpenseEvent.NotesChanged -> _state.update { it.copy(notes = event.notes) }
            is AddExpenseEvent.IsRecurringChanged -> _state.update { it.copy(isRecurring = event.isRecurring) }
            is AddExpenseEvent.Submit -> submitExpense()
        }
    }

    private fun submitExpense() {
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
            _state.update { it.copy(isLoading = true) }
            
            val expense = Expense(
                apartmentId = apartmentId,
                title = state.value.title,
                description = state.value.description,
                amount = amountValue ?: 0.0,
                categoryId = state.value.category.id,
                paidBy = state.value.paidByUserId,
                expenseDate = state.value.date,
                notes = state.value.notes,
                isRecurring = state.value.isRecurring
            )
            
            when (val result = expenseUseCases.insertExpense(expense)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(AddExpenseUiEvent.Success)
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(AddExpenseUiEvent.ShowSnackbar(result.message))
                }
                else -> {}
            }
        }
    }
}

data class AddExpenseState(
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
    val isLoading: Boolean = false
)

sealed class AddExpenseEvent {
    data class TitleChanged(val title: String) : AddExpenseEvent()
    data class DescriptionChanged(val description: String) : AddExpenseEvent()
    data class AmountChanged(val amount: String) : AddExpenseEvent()
    data class CategoryChanged(val category: ExpenseCategory) : AddExpenseEvent()
    data class PaidByChanged(val userId: String) : AddExpenseEvent()
    data class DateChanged(val date: Long) : AddExpenseEvent()
    data class NotesChanged(val notes: String) : AddExpenseEvent()
    data class IsRecurringChanged(val isRecurring: Boolean) : AddExpenseEvent()
    object Submit : AddExpenseEvent()
}

sealed class AddExpenseUiEvent {
    data class ShowSnackbar(val message: String) : AddExpenseUiEvent()
    object Success : AddExpenseUiEvent()
}
