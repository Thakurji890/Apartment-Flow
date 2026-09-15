package com.example.feature.expense.presentation.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartmentmanager.data.ApartmentDataManager
import com.example.feature.apartmentmanager.model.ExpenseCategory
import com.example.feature.expense.domain.model.DefaultCategories
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.usecase.ExpenseUseCases
import com.example.feature.roommate.data.local.dao.RoommateDao
import com.example.feature.roommate.data.local.entity.RoommateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

data class AddExpenseState(
    val description: String = "",
    val descriptionError: String? = null,
    val amount: String = "",
    val amountError: String? = null,
    val date: Long = System.currentTimeMillis(),
    val formattedDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val paidByRoommateId: String = "",
    val paidByError: String? = null,
    val category: ExpenseCategory = ExpenseCategory.GROCERIES,
    val notes: String = "",
    val isRecurring: Boolean = false,
    val currencySymbol: String = "₹",
    val roommates: List<RoommateEntity> = emptyList(),
    val sharedWithRoommateIds: List<String> = emptyList(),
    val isLoading: Boolean = false
) {
    val amountValue: Double
        get() = amount.toDoubleOrNull() ?: 0.0

    val splitCount: Int
        get() = if (sharedWithRoommateIds.isEmpty()) roommates.size else sharedWithRoommateIds.size

    val splitEach: Double
        get() = if (splitCount > 0) amountValue / splitCount else 0.0

    val selectedRoommate: RoommateEntity?
        get() = roommates.find { it.id == paidByRoommateId }
}

sealed class AddExpenseEvent {
    data class DescriptionChanged(val description: String) : AddExpenseEvent()
    data class AmountChanged(val amount: String) : AddExpenseEvent()
    data class AddPresetAmount(val delta: Double) : AddExpenseEvent()
    data class DateChanged(val date: Long) : AddExpenseEvent()
    data class PaidByChanged(val roommateId: String) : AddExpenseEvent()
    data class CategoryChanged(val category: ExpenseCategory) : AddExpenseEvent()
    data class ToggleSharedRoommate(val roommateId: String) : AddExpenseEvent()
    object ToggleAllRoommates : AddExpenseEvent()
    data class NotesChanged(val notes: String) : AddExpenseEvent()
    data class IsRecurringChanged(val isRecurring: Boolean) : AddExpenseEvent()
    object ResetForm : AddExpenseEvent()
    object Submit : AddExpenseEvent()
}

sealed class AddExpenseUiEvent {
    data class ShowSnackbar(val message: String) : AddExpenseUiEvent()
    object Success : AddExpenseUiEvent()
}

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val dataManager: ApartmentDataManager,
    private val roommateDao: RoommateDao,
    private val expenseUseCases: ExpenseUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddExpenseUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        // Collect currency symbol
        viewModelScope.launch {
            dataManager.apartmentProfile.collect { profile ->
                _state.update { it.copy(currencySymbol = profile.currencySymbol) }
            }
        }

        // Combine Room database roommates and in-memory roommates
        viewModelScope.launch {
            combine(
                roommateDao.getAllRoommates(),
                dataManager.roommates
            ) { dbRoommates, dmRoommates ->
                if (dbRoommates.isNotEmpty()) {
                    dbRoommates
                } else {
                    val summaries = dataManager.getBalanceSummaries()
                    dmRoommates.map { rm ->
                        val summary = summaries.find { it.roommate.id == rm.id }
                        val netBal = summary?.netBalance ?: 0.0
                        val status = when {
                            netBal > 0.005 -> "Owed ${dataManager.apartmentProfile.value.currencySymbol}${"%.2f".format(netBal)}"
                            netBal < -0.005 -> "Owes ${dataManager.apartmentProfile.value.currencySymbol}${"%.2f".format(abs(netBal))}"
                            else -> "Settled"
                        }
                        RoommateEntity(
                            id = rm.id,
                            name = rm.name,
                            email = if (rm.notes.contains("@")) rm.notes else "${rm.name.lowercase()}@apartmentflow.app",
                            balanceStatus = status,
                            balanceAmount = netBal,
                            isAdmin = rm.isAdmin,
                            colorHex = rm.colorHex
                        )
                    }
                }
            }.collect { roommates ->
                _state.update { current ->
                    val defaultPaidBy = if (current.paidByRoommateId.isNotBlank() && roommates.any { it.id == current.paidByRoommateId }) {
                        current.paidByRoommateId
                    } else {
                        dataManager.activeRoommateId.value.ifBlank { roommates.firstOrNull()?.id ?: "" }
                    }
                    val shared = if (current.sharedWithRoommateIds.isEmpty()) {
                        roommates.map { it.id }
                    } else {
                        current.sharedWithRoommateIds
                    }
                    current.copy(
                        roommates = roommates,
                        paidByRoommateId = defaultPaidBy,
                        sharedWithRoommateIds = shared
                    )
                }
            }
        }
    }

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description, descriptionError = null) }
            }
            is AddExpenseEvent.AmountChanged -> {
                _state.update { it.copy(amount = event.amount, amountError = null) }
            }
            is AddExpenseEvent.AddPresetAmount -> {
                val current = state.value.amount.toDoubleOrNull() ?: 0.0
                val updated = current + event.delta
                _state.update { it.copy(amount = "%.2f".format(updated), amountError = null) }
            }
            is AddExpenseEvent.DateChanged -> {
                val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(event.date))
                _state.update { it.copy(date = event.date, formattedDate = formatted) }
            }
            is AddExpenseEvent.PaidByChanged -> {
                _state.update { it.copy(paidByRoommateId = event.roommateId, paidByError = null) }
            }
            is AddExpenseEvent.CategoryChanged -> {
                _state.update { it.copy(category = event.category) }
            }
            is AddExpenseEvent.ToggleSharedRoommate -> {
                val currentList = state.value.sharedWithRoommateIds.toMutableList()
                if (currentList.contains(event.roommateId)) {
                    if (currentList.size > 1) {
                        currentList.remove(event.roommateId)
                    }
                } else {
                    currentList.add(event.roommateId)
                }
                _state.update { it.copy(sharedWithRoommateIds = currentList) }
            }
            is AddExpenseEvent.ToggleAllRoommates -> {
                val allIds = state.value.roommates.map { it.id }
                val isAllSelected = state.value.sharedWithRoommateIds.size == allIds.size
                _state.update {
                    it.copy(
                        sharedWithRoommateIds = if (isAllSelected) {
                            listOf(it.paidByRoommateId.ifBlank { allIds.firstOrNull() ?: "" })
                        } else {
                            allIds
                        }
                    )
                }
            }
            is AddExpenseEvent.NotesChanged -> {
                _state.update { it.copy(notes = event.notes) }
            }
            is AddExpenseEvent.IsRecurringChanged -> {
                _state.update { it.copy(isRecurring = event.isRecurring) }
            }
            is AddExpenseEvent.ResetForm -> {
                _state.update {
                    AddExpenseState(
                        currencySymbol = it.currencySymbol,
                        roommates = it.roommates,
                        paidByRoommateId = it.roommates.firstOrNull()?.id ?: "",
                        sharedWithRoommateIds = it.roommates.map { rm -> rm.id }
                    )
                }
            }
            is AddExpenseEvent.Submit -> submitExpense()
        }
    }

    private fun submitExpense() {
        val amountVal = state.value.amount.toDoubleOrNull()
        val isDescValid = state.value.description.isNotBlank()
        val isAmountValid = amountVal != null && amountVal > 0.0
        val isPaidByValid = state.value.paidByRoommateId.isNotBlank()

        if (!isDescValid || !isAmountValid || !isPaidByValid) {
            _state.update {
                it.copy(
                    descriptionError = if (!isDescValid) "Please enter an expense description" else null,
                    amountError = if (!isAmountValid) "Please enter a valid amount greater than 0" else null,
                    paidByError = if (!isPaidByValid) "Please select which roommate paid" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currentState = state.value
            val sharedIds = if (currentState.sharedWithRoommateIds.isEmpty()) {
                currentState.roommates.map { it.id }
            } else {
                currentState.sharedWithRoommateIds
            }

            try {
                // 1. Add to DataManager (offline first, with auto-sync, notification, and balance calculations)
                dataManager.addExpense(
                    date = currentState.formattedDate,
                    item = currentState.description.trim(),
                    amount = amountVal ?: 0.0,
                    paidByRoommateId = currentState.paidByRoommateId,
                    sharedByRoommateIds = sharedIds,
                    notes = currentState.notes.trim(),
                    category = currentState.category
                )

                // 2. Also register in local/cloud repository
                try {
                    val expense = Expense(
                        apartmentId = dataManager.apartmentProfile.value.inviteCode,
                        title = currentState.description.trim(),
                        description = currentState.notes.trim(),
                        amount = amountVal ?: 0.0,
                        categoryId = currentState.category.name.lowercase(),
                        paidBy = currentState.paidByRoommateId,
                        expenseDate = currentState.date,
                        notes = currentState.notes.trim(),
                        isRecurring = currentState.isRecurring
                    )
                    expenseUseCases.insertExpense(expense)
                } catch (ignored: Exception) {}

                // 3. Update Roommate Room entities with new balance status
                val profile = dataManager.apartmentProfile.value
                val summaries = dataManager.getBalanceSummaries()
                val updatedEntities = currentState.roommates.map { rm ->
                    val summary = summaries.find { it.roommate.id == rm.id }
                    val netBal = summary?.netBalance ?: 0.0
                    val status = when {
                        netBal > 0.005 -> "Owed ${profile.currencySymbol}${"%.2f".format(netBal)}"
                        netBal < -0.005 -> "Owes ${profile.currencySymbol}${"%.2f".format(abs(netBal))}"
                        else -> "Settled"
                    }
                    rm.copy(
                        balanceStatus = status,
                        balanceAmount = netBal,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                roommateDao.insertRoommates(updatedEntities)

                _state.update { it.copy(isLoading = false) }
                _eventFlow.emit(AddExpenseUiEvent.Success)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _eventFlow.emit(AddExpenseUiEvent.ShowSnackbar(e.message ?: "Failed to save expense"))
            }
        }
    }
}
