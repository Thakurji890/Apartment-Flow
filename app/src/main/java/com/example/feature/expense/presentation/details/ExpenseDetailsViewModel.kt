package com.example.feature.expense.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.usecase.ApartmentUseCases
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.usecase.ExpenseUseCases
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
    private val expenseUseCases: ExpenseUseCases,
    private val apartmentUseCases: ApartmentUseCases,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId = savedStateHandle.get<String>("expenseId") ?: ""
    private val apartmentId = savedStateHandle.get<String>("apartmentId") ?: ""

    private val _expense = MutableStateFlow<Expense?>(null)
    val expense = _expense.asStateFlow()

    private val _members = MutableStateFlow<List<ApartmentMember>>(emptyList())
    val members = _members.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ExpenseDetailsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val currentUserId = auth.currentUser?.uid

    init {
        loadData()
    }

    private fun loadData() {
        if (expenseId.isBlank()) {
            _error.value = "Invalid Expense ID"
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            // Load members first to map names
            if (apartmentId.isNotBlank()) {
                apartmentUseCases.getApartmentMembers(apartmentId).collect { result ->
                    if (result is Resource.Success) {
                        _members.value = result.data ?: emptyList()
                    }
                }
            }

            expenseUseCases.getExpense(expenseId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _expense.value = result.data
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                        _isLoading.value = false
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    fun deleteExpense() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = expenseUseCases.deleteExpense(expenseId)) {
                is Resource.Success -> {
                    _isLoading.value = false
                    _eventFlow.emit(ExpenseDetailsEvent.DeleteSuccess)
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _eventFlow.emit(ExpenseDetailsEvent.ShowSnackbar(result.message))
                }
                else -> {}
            }
        }
    }
}

sealed class ExpenseDetailsEvent {
    data class ShowSnackbar(val message: String) : ExpenseDetailsEvent()
    object DeleteSuccess : ExpenseDetailsEvent()
}
