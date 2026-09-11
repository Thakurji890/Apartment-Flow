package com.example.feature.apartmentmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.apartmentmanager.data.ApartmentDataManager
import com.example.feature.apartmentmanager.model.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ApartmentUiState(
    val profile: ApartmentProfile = ApartmentProfile(),
    val roommates: List<ApartmentRoommate> = emptyList(),
    val expenses: List<ApartmentExpense> = emptyList(),
    val settlements: List<ApartmentSettlement> = emptyList(),
    val notifications: List<ApartmentNotification> = emptyList(),
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
    val showNotificationsDialog: Boolean = false,
    val statusMessage: String? = null
) {
    val activeRoommate: ApartmentRoommate?
        get() = roommates.find { it.id == activeRoommateId }

    val isActiveUserAdmin: Boolean
        get() = activeRoommate?.isAdmin == true

    val unreadNotificationCount: Int
        get() = notifications.count { !it.isRead }

    val pendingSettlementCount: Int
        get() = settlements.count { it.status == SettlementStatus.PENDING }

    val pendingExpenseCount: Int
        get() = expenses.count { it.status == ExpenseStatus.PENDING }
}

@HiltViewModel
class ApartmentManagerViewModel @Inject constructor(
    private val dataManager: ApartmentDataManager,
    private val firestore: FirebaseFirestore
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

        viewModelScope.launch {
            dataManager.notifications.collect { notifs ->
                _uiState.update { it.copy(notifications = notifs) }
            }
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
        val isAdmin = _uiState.value.isActiveUserAdmin
        val adminId = _uiState.value.activeRoommateId
        if (currentEdit != null) {
            val updated = currentEdit.copy(
                date = date,
                item = item,
                amount = amount,
                paidByRoommateId = paidById,
                sharedByRoommateIds = sharedByIds,
                notes = notes
            )
            dataManager.updateExpense(updated)
            val msg = if (isAdmin) {
                "Expense updated and approved: $item"
            } else {
                "Expense changes submitted for Admin approval: $item"
            }
            _uiState.update { it.copy(statusMessage = msg) }

            // Sync update to Firestore
            viewModelScope.launch {
                try {
                    firestore.collection("expenses").document(updated.id).update(
                        mapOf(
                            "date" to date,
                            "item" to item,
                            "amount" to amount,
                            "paidBy" to paidById,
                            "sharedBy" to sharedByIds,
                            "notes" to notes,
                            "status" to if (isAdmin) "APPROVED" else "PENDING",
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
                } catch (e: Exception) {
                    // Handled gracefully for offline mode
                }
            }
        } else {
            val autoApprove = isAdmin
            dataManager.addExpense(
                date = date,
                item = item,
                amount = amount,
                paidByRoommateId = paidById,
                sharedByRoommateIds = sharedByIds,
                notes = notes,
                autoApproveIfAdmin = autoApprove
            )
            val msg = if (isAdmin) {
                "Added & approved expense: $item"
            } else {
                "Submitted for Admin Approval: $item"
            }
            _uiState.update { it.copy(statusMessage = msg) }
        }
        closeExpenseDialog()
    }

    fun approveExpense(expenseId: String) {
        val adminId = _uiState.value.activeRoommateId
        val isAdmin = _uiState.value.isActiveUserAdmin
        if (!isAdmin) {
            _uiState.update { it.copy(statusMessage = "Permission denied: Only an Admin can approve expenses") }
            return
        }

        val success = dataManager.approveExpense(expenseId, adminId)
        if (success) {
            _uiState.update { it.copy(statusMessage = "Expense approved by Admin!") }

            // Update status in Firestore
            viewModelScope.launch {
                try {
                    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    firestore.collection("expenses").document(expenseId).update(
                        mapOf(
                            "status" to "APPROVED",
                            "approvedByAdminId" to adminId,
                            "approvedAt" to today,
                            "updatedAt" to System.currentTimeMillis(),
                            "rejectionReason" to null
                        )
                    ).await()
                } catch (e: Exception) {
                    // Handled gracefully for offline mode
                }
            }
        } else {
            _uiState.update { it.copy(statusMessage = "Failed to approve expense") }
        }
    }

    fun rejectExpense(expenseId: String, reason: String = "") {
        val adminId = _uiState.value.activeRoommateId
        val isAdmin = _uiState.value.isActiveUserAdmin
        if (!isAdmin) {
            _uiState.update { it.copy(statusMessage = "Permission denied: Only an Admin can reject expenses") }
            return
        }

        val success = dataManager.rejectExpense(expenseId, adminId, reason)
        if (success) {
            _uiState.update { it.copy(statusMessage = "Expense rejected by Admin") }

            // Update status in Firestore
            viewModelScope.launch {
                try {
                    firestore.collection("expenses").document(expenseId).update(
                        mapOf(
                            "status" to "REJECTED",
                            "approvedByAdminId" to adminId,
                            "rejectionReason" to reason.ifBlank { "Declined by Admin" },
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
                } catch (e: Exception) {
                    // Handled gracefully for offline mode
                }
            }
        } else {
            _uiState.update { it.copy(statusMessage = "Failed to reject expense") }
        }
    }

    fun deleteExpense(expenseId: String) {
        dataManager.deleteExpense(expenseId)
        _uiState.update { it.copy(statusMessage = "Expense removed") }
        viewModelScope.launch {
            try {
                firestore.collection("expenses").document(expenseId).delete().await()
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
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
        val isAdmin = _uiState.value.isActiveUserAdmin
        dataManager.addSettlement(
            date = date,
            fromRoommateId = fromId,
            toRoommateId = toId,
            amount = amount,
            note = note,
            autoApproveIfAdmin = isAdmin
        )
        val fromName = _uiState.value.roommates.find { it.id == fromId }?.name ?: "Roommate"
        val toName = _uiState.value.roommates.find { it.id == toId }?.name ?: "Roommate"
        val msg = if (isAdmin) {
            "Recorded & approved settlement: $fromName paid $toName"
        } else {
            "Submitted for Admin Approval: $fromName paid $toName"
        }
        _uiState.update { it.copy(statusMessage = msg) }
        closeSettlementDialog()
    }

    fun approveSettlement(settlementId: String) {
        val adminId = _uiState.value.activeRoommateId
        val success = dataManager.approveSettlement(settlementId, adminId)
        if (success) {
            _uiState.update { it.copy(statusMessage = "Settlement approved by Admin!") }
        } else {
            _uiState.update { it.copy(statusMessage = "Only an Admin can approve settlements") }
        }
    }

    fun rejectSettlement(settlementId: String, reason: String = "") {
        val adminId = _uiState.value.activeRoommateId
        val success = dataManager.rejectSettlement(settlementId, adminId, reason)
        if (success) {
            _uiState.update { it.copy(statusMessage = "Settlement rejected by Admin") }
        } else {
            _uiState.update { it.copy(statusMessage = "Only an Admin can reject settlements") }
        }
    }

    fun deleteSettlement(settlementId: String) {
        dataManager.deleteSettlement(settlementId)
        _uiState.update { it.copy(statusMessage = "Settlement removed") }
    }

    // --- Notification Dialog & Controls ---
    fun openNotificationsDialog() {
        _uiState.update { it.copy(showNotificationsDialog = true) }
    }

    fun closeNotificationsDialog() {
        _uiState.update { it.copy(showNotificationsDialog = false) }
    }

    fun markAllNotificationsAsRead() {
        dataManager.markAllNotificationsAsRead()
    }

    fun clearNotifications() {
        dataManager.clearNotifications()
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
