package com.example.feature.apartmentmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.apartmentmanager.data.ApartmentDataManager
import com.example.feature.apartmentmanager.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ApartmentUiState(
    val profile: ApartmentProfile = ApartmentProfile(),
    val roommates: List<ApartmentRoommate> = emptyList(),
    val expenses: List<ApartmentExpense> = emptyList(),
    val settlements: List<ApartmentSettlement> = emptyList(),
    val activeRoommateId: String = "1",
    val selectedTab: Int = 0, // 0: Balances, 1: Expenses, 2: Settlements, 3: Admin
    val expenseFilterRoommateId: String? = null,
    val searchQuery: String = "",
    val showAddExpenseDialog: Boolean = false,
    val editingExpense: ApartmentExpense? = null,
    val showAddSettlementDialog: Boolean = false,
    val editingSettlement: ApartmentSettlement? = null,
    val settlementPrefillFromId: String? = null,
    val settlementPrefillToId: String? = null,
    val settlementPrefillAmount: Double? = null,
    val showAddRoommateDialog: Boolean = false,
    val editingRoommate: ApartmentRoommate? = null,
    val showEditApartmentDialog: Boolean = false,
    val showResetConfirmationDialog: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class ApartmentManagerViewModel @Inject constructor(
    private val dataManager: ApartmentDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApartmentUiState())
    val uiState: StateFlow<ApartmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dataManager.apartmentProfile,
                dataManager.roommates,
                dataManager.expenses,
                dataManager.settlements,
                dataManager.activeRoommateId
            ) { profile, roommates, expenses, settlements, activeId ->
                _uiState.update { current ->
                    current.copy(
                        profile = profile,
                        roommates = roommates,
                        expenses = expenses,
                        settlements = settlements,
                        activeRoommateId = activeId
                    )
                }
            }.collect()
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setExpenseFilterRoommateId(id: String?) {
        _uiState.update { it.copy(expenseFilterRoommateId = id) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setActiveRoommate(id: String) {
        dataManager.setActiveRoommate(id)
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // --- Balance Calculation Helpers ---
    fun getBalanceSummaries(): List<RoommateBalanceSummary> {
        return dataManager.getBalanceSummaries()
    }

    fun getDebtSettlementSuggestions(): List<DebtTransfer> {
        return dataManager.getDebtSettlementSuggestions()
    }

    fun getTotalPoolSpending(): Double {
        return _uiState.value.expenses.sumOf { it.amount }
    }

    // --- Expense Dialog & CRUD ---
    fun openAddExpenseDialog() {
        _uiState.update { it.copy(showAddExpenseDialog = true, editingExpense = null) }
    }

    fun openEditExpenseDialog(expense: ApartmentExpense) {
        _uiState.update { it.copy(showAddExpenseDialog = true, editingExpense = expense) }
    }

    fun closeExpenseDialog() {
        _uiState.update { it.copy(showAddExpenseDialog = false, editingExpense = null) }
    }

    fun saveExpense(
        date: String,
        item: String,
        amount: Double,
        paidById: String,
        sharedByIds: List<String>,
        notes: String
    ) {
        val currentEdit = _uiState.value.editingExpense
        if (currentEdit != null) {
            dataManager.updateExpense(
                currentEdit.copy(
                    date = date,
                    item = item,
                    amount = amount,
                    paidByRoommateId = paidById,
                    sharedByRoommateIds = sharedByIds,
                    notes = notes
                )
            )
            _uiState.update { it.copy(statusMessage = "Expense updated: $item") }
        } else {
            dataManager.addExpense(
                date = date,
                item = item,
                amount = amount,
                paidByRoommateId = paidById,
                sharedByRoommateIds = sharedByIds,
                notes = notes
            )
            _uiState.update { it.copy(statusMessage = "Added expense: $item") }
        }
        closeExpenseDialog()
    }

    fun deleteExpense(expenseId: String) {
        dataManager.deleteExpense(expenseId)
        _uiState.update { it.copy(statusMessage = "Expense removed") }
    }

    // --- Settlement Dialog & CRUD ---
    fun openAddSettlementDialog(prefillFromId: String? = null, prefillToId: String? = null, prefillAmount: Double? = null) {
        _uiState.update { 
            it.copy(
                showAddSettlementDialog = true, 
                editingSettlement = null,
                settlementPrefillFromId = prefillFromId,
                settlementPrefillToId = prefillToId,
                settlementPrefillAmount = prefillAmount
            ) 
        }
    }

    fun closeSettlementDialog() {
        _uiState.update { 
            it.copy(
                showAddSettlementDialog = false, 
                editingSettlement = null,
                settlementPrefillFromId = null,
                settlementPrefillToId = null,
                settlementPrefillAmount = null
            ) 
        }
    }

    fun saveSettlement(
        date: String,
        fromId: String,
        toId: String,
        amount: Double,
        note: String
    ) {
        dataManager.addSettlement(
            date = date,
            fromRoommateId = fromId,
            toRoommateId = toId,
            amount = amount,
            note = note
        )
        val fromName = _uiState.value.roommates.find { it.id == fromId }?.name ?: "Roommate"
        val toName = _uiState.value.roommates.find { it.id == toId }?.name ?: "Roommate"
        _uiState.update { it.copy(statusMessage = "Recorded settlement: $fromName paid $toName") }
        closeSettlementDialog()
    }

    fun deleteSettlement(settlementId: String) {
        dataManager.deleteSettlement(settlementId)
        _uiState.update { it.copy(statusMessage = "Settlement removed") }
    }

    // --- Roommate Dialog & CRUD ---
    fun openAddRoommateDialog() {
        _uiState.update { it.copy(showAddRoommateDialog = true, editingRoommate = null) }
    }

    fun openEditRoommateDialog(roommate: ApartmentRoommate) {
        _uiState.update { it.copy(showAddRoommateDialog = true, editingRoommate = roommate) }
    }

    fun closeRoommateDialog() {
        _uiState.update { it.copy(showAddRoommateDialog = false, editingRoommate = null) }
    }

    fun saveRoommate(name: String, notes: String, isAdmin: Boolean) {
        val editing = _uiState.value.editingRoommate
        if (editing != null) {
            dataManager.updateRoommate(editing.copy(name = name, notes = notes, isAdmin = isAdmin))
            _uiState.update { it.copy(statusMessage = "Roommate updated: $name") }
        } else {
            dataManager.addRoommate(name = name, notes = notes, isAdmin = isAdmin)
            _uiState.update { it.copy(statusMessage = "Added new roommate: $name") }
        }
        closeRoommateDialog()
    }

    fun deleteRoommate(roommateId: String) {
        dataManager.deleteRoommate(roommateId)
        _uiState.update { it.copy(statusMessage = "Roommate removed") }
    }

    // --- Apartment Settings Dialog ---
    fun openEditApartmentDialog() {
        _uiState.update { it.copy(showEditApartmentDialog = true) }
    }

    fun closeEditApartmentDialog() {
        _uiState.update { it.copy(showEditApartmentDialog = false) }
    }

    fun updateApartmentProfile(name: String, flatNumber: String, currencySymbol: String, inviteCode: String) {
        dataManager.updateApartmentProfile(
            ApartmentProfile(
                name = name.ifBlank { "Apartment Flat 402" },
                flatNumber = flatNumber.ifBlank { "Flat 402" },
                currencySymbol = currencySymbol.ifBlank { "₹" },
                inviteCode = inviteCode.ifBlank { "APT402" }
            )
        )
        _uiState.update { it.copy(statusMessage = "Apartment details updated!") }
        closeEditApartmentDialog()
    }

    // --- Reset to Original Sheet ---
    fun openResetConfirmationDialog() {
        _uiState.update { it.copy(showResetConfirmationDialog = true) }
    }

    fun closeResetConfirmationDialog() {
        _uiState.update { it.copy(showResetConfirmationDialog = false) }
    }

    fun resetToDefaultSheetData() {
        dataManager.resetToDefaultData()
        _uiState.update { 
            it.copy(
                showResetConfirmationDialog = false,
                statusMessage = "Restored original sheet data!"
            ) 
        }
    }
}
