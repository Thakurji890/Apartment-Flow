package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApartmentRepository
import com.example.data.Bill
import com.example.data.Settlement
import com.example.data.BillCategory
import com.example.data.Debt
import com.example.data.Roommate
import com.example.data.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApartmentViewModel : ViewModel() {
    private val repository = ApartmentRepository.getInstance()

    val roommates: StateFlow<List<Roommate>> = repository.roommates
    val bills: StateFlow<List<Bill>> = repository.bills
    val settlements: StateFlow<List<Settlement>> = repository.settlements
    val notifications: StateFlow<List<Notification>> = repository.notifications
    val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.read }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val currentUserId: StateFlow<String> = repository.currentUserId

    // Apartment details
    val activeApartmentId: StateFlow<String?> = repository.activeApartmentId
    val activeApartmentName: StateFlow<String?> = repository.activeApartmentName
    val activeApartmentInviteCode: StateFlow<String?> = repository.activeApartmentInviteCode

    // Loading & Error states
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    // Currently selected category filter for Bills screen
    private val _selectedCategoryFilter = MutableStateFlow<BillCategory?>(null)
    val selectedCategoryFilter: StateFlow<BillCategory?> = _selectedCategoryFilter.asStateFlow()

    // Filtered bills
    val filteredBills: StateFlow<List<Bill>> = combine(bills, _selectedCategoryFilter) { billsList, filter ->
        if (filter == null) {
            billsList
        } else {
            billsList.filter { it.category == filter }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically calculated properties
    val totalSpent: StateFlow<Double> = bills.combine(roommates) { _, _ ->
        repository.getTotalSpent()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val debts: StateFlow<List<Debt>> = bills.combine(roommates) { _, _ ->
        repository.getDebts()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter bills to exclude cash settlements for history lists or show all
    val actualBillsOnly: StateFlow<List<Bill>> = bills.combine(roommates) { billsList, _ ->
        // With our proper Settlement class, bills list contains only real bills. So we just return everything.
        billsList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategoryFilter(category: BillCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun addBill(title: String, amount: Double, category: BillCategory, payerId: String, description: String = "", date: String? = null, splitAmongIds: List<String> = emptyList()) {
        repository.addBill(title, amount, category, payerId, description, date, splitAmongIds)
    }

    fun deleteBill(billId: String) {
        repository.deleteBill(billId)
    }

    fun addRoommate(name: String) {
        repository.addRoommate(name)
    }

    fun removeRoommate(roommateId: String) {
        repository.removeRoommate(roommateId)
    }

    fun settleUp(fromId: String, toId: String, amount: Double) {
        repository.performSettlement(fromId, toId, amount)
    }

    // Apartment Creation / Joining Flow
    fun createApartment(name: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.createApartment(name, onResult)
        }
    }

    fun joinApartment(inviteCode: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.joinApartment(inviteCode, onResult)
        }
    }

    fun leaveApartment() {
        viewModelScope.launch {
            repository.leaveApartment()
        }
    }

    fun updateProfile(newDisplayName: String, onComplete: (Boolean, String?) -> Unit) {
        repository.updateProfile(newDisplayName, onComplete)
    }

    fun updateApartmentName(newName: String, onComplete: (Boolean, String?) -> Unit) {
        repository.updateApartmentName(newName, onComplete)
    }

    val appLanguage: StateFlow<String> = repository.getLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")
        
    val themeMode: StateFlow<String> = repository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
        
    val currency: StateFlow<String> = repository.getCurrency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    fun saveLanguage(lang: String) {
        repository.saveLanguage(lang)
    }

    fun saveThemeMode(mode: String) {
        repository.saveThemeMode(mode)
    }

    fun saveCurrency(curr: String) {
        repository.saveCurrency(curr)
    }

    fun formatCurrency(amount: Double, currencyPref: String): String {
        val symbol = when {
            currencyPref.contains("€") -> "€"
            currencyPref.contains("£") -> "£"
            currencyPref.contains("₹") -> "₹"
            currencyPref.contains("CAD") -> "CA$"
            else -> "$"
        }
        return String.format(java.util.Locale.US, "%s%.2f", symbol, amount)
    }

    fun markNotificationsAsRead() {
        repository.markNotificationsAsRead()
    }

    fun resetData() {
        // Since we are backed by Firestore, resetting default mock data isn't used
        _selectedCategoryFilter.value = null
    }
}
