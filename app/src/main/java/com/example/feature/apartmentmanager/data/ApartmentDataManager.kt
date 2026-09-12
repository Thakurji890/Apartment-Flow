package com.example.feature.apartmentmanager.data

import android.content.Context
import android.content.SharedPreferences
import com.example.feature.apartmentmanager.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

class ApartmentDataManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("apartment_flow_manager_prefs", Context.MODE_PRIVATE)

    private val _apartmentProfile = MutableStateFlow(ApartmentProfile())
    val apartmentProfile: StateFlow<ApartmentProfile> = _apartmentProfile.asStateFlow()

    private val _roommates = MutableStateFlow<List<ApartmentRoommate>>(emptyList())
    val roommates: StateFlow<List<ApartmentRoommate>> = _roommates.asStateFlow()

    private val _expenses = MutableStateFlow<List<ApartmentExpense>>(emptyList())
    val expenses: StateFlow<List<ApartmentExpense>> = _expenses.asStateFlow()

    private val _settlements = MutableStateFlow<List<ApartmentSettlement>>(emptyList())
    val settlements: StateFlow<List<ApartmentSettlement>> = _settlements.asStateFlow()

    private val _notifications = MutableStateFlow<List<ApartmentNotification>>(emptyList())
    val notifications: StateFlow<List<ApartmentNotification>> = _notifications.asStateFlow()

    private val _envelopeBudgets = MutableStateFlow<List<SharedEnvelopeBudget>>(emptyList())
    val envelopeBudgets: StateFlow<List<SharedEnvelopeBudget>> = _envelopeBudgets.asStateFlow()

    private val _activeRoommateId = MutableStateFlow("1") // Defaults to Aniket (Admin)
    val activeRoommateId: StateFlow<String> = _activeRoommateId.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (!prefs.contains("has_data_v1")) {
            resetToDefaultData()
            return
        }

        try {
            // Load Profile
            val profileJson = prefs.getString("profile", null)
            if (profileJson != null) {
                val obj = JSONObject(profileJson)
                _apartmentProfile.value = ApartmentProfile(
                    name = obj.optString("name", "Apartment Flat 402"),
                    flatNumber = obj.optString("flatNumber", "Flat 402"),
                    currencySymbol = obj.optString("currencySymbol", "₹"),
                    currencyCode = obj.optString("currencyCode", "INR"),
                    inviteCode = obj.optString("inviteCode", "APT402")
                )
            }

            // Load Roommates
            val roommatesJson = prefs.getString("roommates", null)
            if (roommatesJson != null) {
                val array = JSONArray(roommatesJson)
                val list = mutableListOf<ApartmentRoommate>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ApartmentRoommate(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            notes = obj.optString("notes", ""),
                            isAdmin = obj.optBoolean("isAdmin", false),
                            colorHex = obj.optLong("colorHex", 0xFF006A6AL)
                        )
                    )
                }
                _roommates.value = list
            }

            // Load Expenses
            val expensesJson = prefs.getString("expenses", null)
            if (expensesJson != null) {
                val array = JSONArray(expensesJson)
                val list = mutableListOf<ApartmentExpense>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val sharedArray = obj.optJSONArray("sharedBy") ?: JSONArray()
                    val sharedList = mutableListOf<String>()
                    for (j in 0 until sharedArray.length()) {
                        sharedList.add(sharedArray.getString(j))
                    }
                    val statusStr = obj.optString("status", "APPROVED")
                    val expStatus = try {
                        ExpenseStatus.valueOf(statusStr)
                    } catch (e: Exception) {
                        ExpenseStatus.APPROVED
                    }
                    val catStr = obj.optString("category", "GROCERIES")
                    val expCat = try {
                        ExpenseCategory.valueOf(catStr)
                    } catch (e: Exception) {
                        ExpenseCategory.GROCERIES
                    }
                    list.add(
                        ApartmentExpense(
                            id = obj.getString("id"),
                            date = obj.getString("date"),
                            item = obj.getString("item"),
                            amount = obj.getDouble("amount"),
                            paidByRoommateId = obj.getString("paidBy"),
                            sharedByRoommateIds = sharedList,
                            notes = obj.optString("notes", ""),
                            category = expCat,
                            status = expStatus,
                            approvedByAdminId = if (obj.has("approvedBy")) obj.optString("approvedBy") else null,
                            approvedAt = if (obj.has("approvedAt")) obj.optString("approvedAt") else null,
                            rejectionReason = if (obj.has("rejectionReason")) obj.optString("rejectionReason") else null
                        )
                    )
                }
                _expenses.value = list
            }

            // Load Settlements
            val settlementsJson = prefs.getString("settlements", null)
            if (settlementsJson != null) {
                val array = JSONArray(settlementsJson)
                val list = mutableListOf<ApartmentSettlement>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val statusStr = obj.optString("status", "APPROVED")
                    val status = try {
                        SettlementStatus.valueOf(statusStr)
                    } catch (e: Exception) {
                        SettlementStatus.APPROVED
                    }
                    list.add(
                        ApartmentSettlement(
                            id = obj.getString("id"),
                            date = obj.getString("date"),
                            fromRoommateId = obj.getString("from"),
                            toRoommateId = obj.getString("to"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            status = status,
                            approvedByAdminId = if (obj.has("approvedBy")) obj.optString("approvedBy") else null,
                            approvedAt = if (obj.has("approvedAt")) obj.optString("approvedAt") else null,
                            rejectionReason = if (obj.has("rejectionReason")) obj.optString("rejectionReason") else null
                        )
                    )
                }
                _settlements.value = list
            }

            // Load Notifications
            val notifJson = prefs.getString("notifications", null)
            if (notifJson != null) {
                val array = JSONArray(notifJson)
                val list = mutableListOf<ApartmentNotification>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val catStr = obj.optString("category", "GENERAL")
                    val cat = try {
                        NotificationCategory.valueOf(catStr)
                    } catch (e: Exception) {
                        NotificationCategory.GENERAL
                    }
                    list.add(
                        ApartmentNotification(
                            id = obj.getString("id"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            title = obj.getString("title"),
                            message = obj.getString("message"),
                            category = cat,
                            authorName = obj.optString("authorName", "System"),
                            requiresAdminAction = obj.optBoolean("requiresAdminAction", false),
                            isRead = obj.optBoolean("isRead", false)
                        )
                    )
                }
                _notifications.value = list
            }

            // Load Envelope Budgets
            val budgetsJson = prefs.getString("envelope_budgets", null)
            if (budgetsJson != null) {
                val array = JSONArray(budgetsJson)
                val list = mutableListOf<SharedEnvelopeBudget>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val catStr = obj.optString("category", "GROCERIES")
                    val cat = try {
                        ExpenseCategory.valueOf(catStr)
                    } catch (e: Exception) {
                        ExpenseCategory.GROCERIES
                    }
                    list.add(
                        SharedEnvelopeBudget(
                            id = obj.getString("id"),
                            category = cat,
                            monthlyCap = obj.getDouble("monthlyCap"),
                            alertThresholdPercent = obj.optInt("alertThresholdPercent", 80),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                _envelopeBudgets.value = list
            } else {
                _envelopeBudgets.value = defaultEnvelopeBudgets()
            }

            _activeRoommateId.value = prefs.getString("active_roommate_id", "1") ?: "1"

        } catch (e: Exception) {
            resetToDefaultData()
        }
    }

    private fun saveData() {
        val editor = prefs.edit()
        editor.putBoolean("has_data_v1", true)

        // Save Profile
        val profObj = JSONObject()
        profObj.put("name", _apartmentProfile.value.name)
        profObj.put("flatNumber", _apartmentProfile.value.flatNumber)
        profObj.put("currencySymbol", _apartmentProfile.value.currencySymbol)
        profObj.put("currencyCode", _apartmentProfile.value.currencyCode)
        profObj.put("inviteCode", _apartmentProfile.value.inviteCode)
        editor.putString("profile", profObj.toString())

        // Save Roommates
        val rmArray = JSONArray()
        _roommates.value.forEach { rm ->
            val obj = JSONObject()
            obj.put("id", rm.id)
            obj.put("name", rm.name)
            obj.put("notes", rm.notes)
            obj.put("isAdmin", rm.isAdmin)
            obj.put("colorHex", rm.colorHex)
            rmArray.put(obj)
        }
        editor.putString("roommates", rmArray.toString())

        // Save Expenses
        val expArray = JSONArray()
        _expenses.value.forEach { exp ->
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("date", exp.date)
            obj.put("item", exp.item)
            obj.put("amount", exp.amount)
            obj.put("paidBy", exp.paidByRoommateId)
            val sharedArray = JSONArray()
            exp.sharedByRoommateIds.forEach { sharedArray.put(it) }
            obj.put("sharedBy", sharedArray)
            obj.put("notes", exp.notes)
            obj.put("category", exp.category.name)
            obj.put("status", exp.status.name)
            exp.approvedByAdminId?.let { obj.put("approvedBy", it) }
            exp.approvedAt?.let { obj.put("approvedAt", it) }
            exp.rejectionReason?.let { obj.put("rejectionReason", it) }
            expArray.put(obj)
        }
        editor.putString("expenses", expArray.toString())

        // Save Settlements
        val setArray = JSONArray()
        _settlements.value.forEach { set ->
            val obj = JSONObject()
            obj.put("id", set.id)
            obj.put("date", set.date)
            obj.put("from", set.fromRoommateId)
            obj.put("to", set.toRoommateId)
            obj.put("amount", set.amount)
            obj.put("note", set.note)
            obj.put("status", set.status.name)
            set.approvedByAdminId?.let { obj.put("approvedBy", it) }
            set.approvedAt?.let { obj.put("approvedAt", it) }
            set.rejectionReason?.let { obj.put("rejectionReason", it) }
            setArray.put(obj)
        }
        editor.putString("settlements", setArray.toString())

        // Save Notifications
        val notifArray = JSONArray()
        _notifications.value.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("timestamp", n.timestamp)
            obj.put("title", n.title)
            obj.put("message", n.message)
            obj.put("category", n.category.name)
            obj.put("authorName", n.authorName)
            obj.put("requiresAdminAction", n.requiresAdminAction)
            obj.put("isRead", n.isRead)
            notifArray.put(obj)
        }
        editor.putString("notifications", notifArray.toString())

        // Save Envelope Budgets
        val budgetArray = JSONArray()
        _envelopeBudgets.value.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("category", b.category.name)
            obj.put("monthlyCap", b.monthlyCap)
            obj.put("alertThresholdPercent", b.alertThresholdPercent)
            obj.put("notes", b.notes)
            budgetArray.put(obj)
        }
        editor.putString("envelope_budgets", budgetArray.toString())

        editor.putString("active_roommate_id", _activeRoommateId.value)

        editor.apply()
    }

    fun setActiveRoommate(id: String) {
        _activeRoommateId.value = id
        prefs.edit().putString("active_roommate_id", id).apply()
    }

    fun updateApartmentProfile(profile: ApartmentProfile) {
        _apartmentProfile.value = profile
        saveData()
    }

    // --- Roommates CRUD ---
    fun addRoommate(name: String, notes: String = "", isAdmin: Boolean = false) {
        val newId = UUID.randomUUID().toString().take(6)
        val colors = listOf(0xFF006A6AL, 0xFF2E7D32L, 0xFF1565C0L, 0xFFE65100L, 0xFF6A1B9AL, 0xFFC2185BL)
        val color = colors[(_roommates.value.size) % colors.size]
        val newRoommate = ApartmentRoommate(newId, name, notes, isAdmin, color)
        _roommates.value = _roommates.value + newRoommate

        val adder = _roommates.value.find { it.id == _activeRoommateId.value }?.name ?: "Admin"
        addNotification(
            title = "New Roommate Added",
            message = "$name joined the apartment (added by $adder).",
            category = NotificationCategory.GENERAL,
            authorName = adder
        )
        saveData()
    }

    fun updateRoommate(roommate: ApartmentRoommate) {
        _roommates.value = _roommates.value.map { if (it.id == roommate.id) roommate else it }
        val updater = _roommates.value.find { it.id == _activeRoommateId.value }?.name ?: "Admin"
        addNotification(
            title = "Roommate Profile Updated",
            message = "${roommate.name}'s details were updated by $updater.",
            category = NotificationCategory.GENERAL,
            authorName = updater
        )
        saveData()
    }

    fun deleteRoommate(roommateId: String) {
        if (_roommates.value.size <= 1) return // Keep at least one
        val rm = _roommates.value.find { it.id == roommateId }
        _roommates.value = _roommates.value.filter { it.id != roommateId }
        if (_activeRoommateId.value == roommateId) {
            _activeRoommateId.value = _roommates.value.firstOrNull()?.id ?: ""
        }
        val remover = _roommates.value.find { it.id == _activeRoommateId.value }?.name ?: "Admin"
        if (rm != null) {
            addNotification(
                title = "Roommate Removed",
                message = "${rm.name} was removed from the apartment roster by $remover.",
                category = NotificationCategory.GENERAL,
                authorName = remover
            )
        }
        saveData()
    }

    // --- Expenses CRUD & Approval Workflow ---
    fun addExpense(
        date: String,
        item: String,
        amount: Double,
        paidByRoommateId: String,
        sharedByRoommateIds: List<String>,
        notes: String = "",
        category: ExpenseCategory = ExpenseCategory.GROCERIES,
        autoApproveIfAdmin: Boolean = true
    ) {
        val activeUser = _roommates.value.find { it.id == _activeRoommateId.value }
        val isAdmin = activeUser?.isAdmin == true
        val status = if (isAdmin && autoApproveIfAdmin) ExpenseStatus.APPROVED else ExpenseStatus.PENDING
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        val expense = ApartmentExpense(
            id = UUID.randomUUID().toString().take(8),
            date = date,
            item = item,
            amount = amount,
            paidByRoommateId = paidByRoommateId,
            sharedByRoommateIds = sharedByRoommateIds,
            notes = notes,
            category = category,
            status = status,
            approvedByAdminId = if (status == ExpenseStatus.APPROVED) activeUser?.id else null,
            approvedAt = if (status == ExpenseStatus.APPROVED) today else null
        )
        // Add to the front so newest appears first
        _expenses.value = listOf(expense) + _expenses.value

        val payer = _roommates.value.find { it.id == paidByRoommateId }?.name ?: "Roommate"
        val currency = _apartmentProfile.value.currencySymbol

        if (status == ExpenseStatus.PENDING) {
            addNotification(
                title = "Purchase Awaiting Admin Approval",
                message = "$payer submitted purchase '$item' for $currency${"%.2f".format(amount)} (Split among ${sharedByRoommateIds.size} roommates). Admin approval is required to update balances.",
                category = NotificationCategory.PURCHASE,
                authorName = payer,
                requiresAdminAction = true
            )
        } else {
            addNotification(
                title = "New Purchase Logged & Approved",
                message = "Admin ${activeUser?.name ?: "Admin"} recorded purchase '$item' for $currency${"%.2f".format(amount)} (Split among ${sharedByRoommateIds.size} roommates).",
                category = NotificationCategory.PURCHASE,
                authorName = activeUser?.name ?: "Admin"
            )
        }

        saveData()
    }

    fun updateExpense(expense: ApartmentExpense) {
        val activeUser = _roommates.value.find { it.id == _activeRoommateId.value }
        val isAdmin = activeUser?.isAdmin == true
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        // If non-admin changes an expense, it reverts to pending for admin approval
        val updatedExpense = if (!isAdmin) {
            expense.copy(
                status = ExpenseStatus.PENDING,
                approvedByAdminId = null,
                approvedAt = null,
                rejectionReason = null
            )
        } else {
            expense.copy(
                status = ExpenseStatus.APPROVED,
                approvedByAdminId = activeUser.id,
                approvedAt = today,
                rejectionReason = null
            )
        }

        _expenses.value = _expenses.value.map { if (it.id == expense.id) updatedExpense else it }
        val updater = activeUser?.name ?: "Roommate"
        val currency = _apartmentProfile.value.currencySymbol

        if (updatedExpense.status == ExpenseStatus.PENDING) {
            addNotification(
                title = "Purchase Edit Awaiting Admin Approval",
                message = "$updater modified purchase '${expense.item}' ($currency${"%.2f".format(expense.amount)}). Admin approval is required.",
                category = NotificationCategory.PURCHASE,
                authorName = updater,
                requiresAdminAction = true
            )
        } else {
            addNotification(
                title = "Purchase Updated by Admin",
                message = "Admin $updater modified purchase '${expense.item}' ($currency${"%.2f".format(expense.amount)}).",
                category = NotificationCategory.PURCHASE,
                authorName = updater
            )
        }
        saveData()
    }

    fun approveExpense(expenseId: String, adminId: String): Boolean {
        val admin = _roommates.value.find { it.id == adminId }
        if (admin == null || !admin.isAdmin) return false

        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        var target: ApartmentExpense? = null

        _expenses.value = _expenses.value.map { e ->
            if (e.id == expenseId) {
                target = e.copy(
                    status = ExpenseStatus.APPROVED,
                    approvedByAdminId = adminId,
                    approvedAt = today,
                    rejectionReason = null
                )
                target!!
            } else e
        }

        target?.let { e ->
            val payer = _roommates.value.find { it.id == e.paidByRoommateId }?.name ?: "Roommate"
            val currency = _apartmentProfile.value.currencySymbol
            addNotification(
                title = "Purchase Approved by Admin",
                message = "Admin ${admin.name} approved $payer's purchase '${e.item}' ($currency${"%.2f".format(e.amount)}). Ledger balances updated!",
                category = NotificationCategory.ADMIN_ACTION,
                authorName = admin.name
            )
        }
        saveData()
        return true
    }

    fun rejectExpense(expenseId: String, adminId: String, reason: String = ""): Boolean {
        val admin = _roommates.value.find { it.id == adminId }
        if (admin == null || !admin.isAdmin) return false

        var target: ApartmentExpense? = null

        _expenses.value = _expenses.value.map { e ->
            if (e.id == expenseId) {
                target = e.copy(
                    status = ExpenseStatus.REJECTED,
                    approvedByAdminId = adminId,
                    rejectionReason = reason.ifBlank { "Declined by Admin" }
                )
                target!!
            } else e
        }

        target?.let { e ->
            val payer = _roommates.value.find { it.id == e.paidByRoommateId }?.name ?: "Roommate"
            val currency = _apartmentProfile.value.currencySymbol
            val noteReason = if (reason.isNotBlank()) " Reason: $reason" else ""
            addNotification(
                title = "Purchase Rejected by Admin",
                message = "Admin ${admin.name} rejected $payer's purchase '${e.item}' ($currency${"%.2f".format(e.amount)}).$noteReason",
                category = NotificationCategory.ADMIN_ACTION,
                authorName = admin.name
            )
        }
        saveData()
        return true
    }

    fun deleteExpense(expenseId: String) {
        val exp = _expenses.value.find { it.id == expenseId }
        _expenses.value = _expenses.value.filter { it.id != expenseId }
        val remover = _roommates.value.find { it.id == _activeRoommateId.value }?.name ?: "Admin"
        val currency = _apartmentProfile.value.currencySymbol
        if (exp != null) {
            addNotification(
                title = "Purchase Deleted",
                message = "$remover deleted '${exp.item}' ($currency${"%.2f".format(exp.amount)}).",
                category = NotificationCategory.PURCHASE,
                authorName = remover
            )
        }
        saveData()
    }

    // --- Settlements CRUD & Approval Workflow ---
    fun addSettlement(
        date: String,
        fromRoommateId: String,
        toRoommateId: String,
        amount: Double,
        note: String = "",
        autoApproveIfAdmin: Boolean = true
    ) {
        val activeUser = _roommates.value.find { it.id == _activeRoommateId.value }
        val isAdmin = activeUser?.isAdmin == true
        val status = if (isAdmin && autoApproveIfAdmin) SettlementStatus.APPROVED else SettlementStatus.PENDING
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        val settlement = ApartmentSettlement(
            id = UUID.randomUUID().toString().take(8),
            date = date,
            fromRoommateId = fromRoommateId,
            toRoommateId = toRoommateId,
            amount = amount,
            note = note,
            status = status,
            approvedByAdminId = if (status == SettlementStatus.APPROVED) activeUser?.id else null,
            approvedAt = if (status == SettlementStatus.APPROVED) today else null
        )
        _settlements.value = listOf(settlement) + _settlements.value

        val fromName = _roommates.value.find { it.id == fromRoommateId }?.name ?: "Roommate"
        val toName = _roommates.value.find { it.id == toRoommateId }?.name ?: "Roommate"
        val currency = _apartmentProfile.value.currencySymbol

        if (status == SettlementStatus.PENDING) {
            addNotification(
                title = "Settlement Awaiting Admin Approval",
                message = "$fromName submitted payment of $currency${"%.2f".format(amount)} to $toName. Admin approval is required to update balances.",
                category = NotificationCategory.SETTLEMENT,
                authorName = fromName,
                requiresAdminAction = true
            )
        } else {
            addNotification(
                title = "Settlement Recorded & Approved",
                message = "Admin ${activeUser?.name ?: "Admin"} recorded and approved $fromName's payment of $currency${"%.2f".format(amount)} to $toName.",
                category = NotificationCategory.SETTLEMENT,
                authorName = activeUser?.name ?: "Admin"
            )
        }

        saveData()
    }

    /**
     * Records all suggested minimal settlements as a batch (smart settlement proposal execution).
     */
    fun recordBatchSettlements(transfers: List<DebtTransfer>, note: String = "Month-End Smart Settlement"): Int {
        if (transfers.isEmpty()) return 0
        val activeUser = _roommates.value.find { it.id == _activeRoommateId.value }
        val isAdmin = activeUser?.isAdmin == true
        val status = if (isAdmin) SettlementStatus.APPROVED else SettlementStatus.PENDING
        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val currency = _apartmentProfile.value.currencySymbol

        val newSettlements = transfers.map { transfer ->
            ApartmentSettlement(
                id = UUID.randomUUID().toString().take(8),
                date = today,
                fromRoommateId = transfer.fromRoommate.id,
                toRoommateId = transfer.toRoommate.id,
                amount = transfer.amount,
                note = note,
                status = status,
                approvedByAdminId = if (status == SettlementStatus.APPROVED) activeUser?.id else null,
                approvedAt = if (status == SettlementStatus.APPROVED) today else null
            )
        }

        _settlements.value = newSettlements + _settlements.value

        val totalAmount = transfers.sumOf { it.amount }
        val author = activeUser?.name ?: "Admin"
        addNotification(
            title = if (isAdmin) "Smart Batch Settlement Recorded" else "Batch Settlement Submitted for Approval",
            message = "$author initiated batch settlement of ${transfers.size} transactions ($currency${"%.2f".format(totalAmount)}).",
            category = NotificationCategory.SETTLEMENT,
            authorName = author,
            requiresAdminAction = !isAdmin
        )
        saveData()
        return transfers.size
    }

    fun approveSettlement(settlementId: String, adminId: String): Boolean {
        val admin = _roommates.value.find { it.id == adminId }
        if (admin == null || !admin.isAdmin) return false

        val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        var target: ApartmentSettlement? = null

        _settlements.value = _settlements.value.map { s ->
            if (s.id == settlementId) {
                target = s.copy(
                    status = SettlementStatus.APPROVED,
                    approvedByAdminId = adminId,
                    approvedAt = today,
                    rejectionReason = null
                )
                target!!
            } else s
        }

        target?.let { s ->
            val fromName = _roommates.value.find { it.id == s.fromRoommateId }?.name ?: "Roommate"
            val toName = _roommates.value.find { it.id == s.toRoommateId }?.name ?: "Roommate"
            val currency = _apartmentProfile.value.currencySymbol
            addNotification(
                title = "Settlement Approved by Admin",
                message = "Admin ${admin.name} approved $fromName's payment of $currency${"%.2f".format(s.amount)} to $toName. Ledger balances updated!",
                category = NotificationCategory.ADMIN_ACTION,
                authorName = admin.name
            )
        }
        saveData()
        return true
    }

    fun rejectSettlement(settlementId: String, adminId: String, reason: String = ""): Boolean {
        val admin = _roommates.value.find { it.id == adminId }
        if (admin == null || !admin.isAdmin) return false

        var target: ApartmentSettlement? = null

        _settlements.value = _settlements.value.map { s ->
            if (s.id == settlementId) {
                target = s.copy(
                    status = SettlementStatus.REJECTED,
                    approvedByAdminId = adminId,
                    rejectionReason = reason.ifBlank { "Declined by Admin" }
                )
                target!!
            } else s
        }

        target?.let { s ->
            val fromName = _roommates.value.find { it.id == s.fromRoommateId }?.name ?: "Roommate"
            val toName = _roommates.value.find { it.id == s.toRoommateId }?.name ?: "Roommate"
            val currency = _apartmentProfile.value.currencySymbol
            val noteReason = if (reason.isNotBlank()) " Reason: $reason" else ""
            addNotification(
                title = "Settlement Rejected by Admin",
                message = "Admin ${admin.name} rejected $fromName's payment of $currency${"%.2f".format(s.amount)} to $toName.$noteReason",
                category = NotificationCategory.ADMIN_ACTION,
                authorName = admin.name
            )
        }
        saveData()
        return true
    }

    fun deleteSettlement(settlementId: String) {
        val s = _settlements.value.find { it.id == settlementId }
        _settlements.value = _settlements.value.filter { it.id != settlementId }
        val remover = _roommates.value.find { it.id == _activeRoommateId.value }?.name ?: "Admin"
        val currency = _apartmentProfile.value.currencySymbol
        if (s != null) {
            val fromName = _roommates.value.find { it.id == s.fromRoommateId }?.name ?: "Roommate"
            val toName = _roommates.value.find { it.id == s.toRoommateId }?.name ?: "Roommate"
            addNotification(
                title = "Settlement Deleted",
                message = "$remover deleted payment record of $currency${"%.2f".format(s.amount)} ($fromName → $toName).",
                category = NotificationCategory.SETTLEMENT,
                authorName = remover
            )
        }
        saveData()
    }

    // --- Notification Helpers ---
    fun addNotification(
        title: String,
        message: String,
        category: NotificationCategory,
        authorName: String = "System",
        requiresAdminAction: Boolean = false
    ) {
        val notif = ApartmentNotification(
            id = UUID.randomUUID().toString().take(8),
            timestamp = System.currentTimeMillis(),
            title = title,
            message = message,
            category = category,
            authorName = authorName,
            requiresAdminAction = requiresAdminAction,
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        saveData()
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
        saveData()
    }

    // --- Balance Calculations (Matches Spreadsheet Table 1) ---
    fun getBalanceSummaries(): List<RoommateBalanceSummary> {
        val rms = _roommates.value
        val exps = _expenses.value.filter { it.status == ExpenseStatus.APPROVED }
        val sets = _settlements.value

        return rms.map { rm ->
            // Total Paid (Groceries)
            val totalPaidGroceries = exps
                .filter { it.paidByRoommateId == rm.id }
                .sumOf { it.amount }

            // Total Owed (Their Share)
            val totalOwedShare = exps
                .filter { it.sharedByRoommateIds.contains(rm.id) && it.sharingCount > 0 }
                .sumOf { it.shareEach }

            // Paid to Others (ONLY Approved Settlements where from == rm.id)
            val paidToOthers = sets
                .filter { it.fromRoommateId == rm.id && it.status == SettlementStatus.APPROVED }
                .sumOf { it.amount }

            // Received from Others (ONLY Approved Settlements where to == rm.id)
            val receivedFromOthers = sets
                .filter { it.toRoommateId == rm.id && it.status == SettlementStatus.APPROVED }
                .sumOf { it.amount }

            // Net Balance = (Total Paid + Paid To Others) - (Total Owed + Received From Others)
            val net = (totalPaidGroceries + paidToOthers) - (totalOwedShare + receivedFromOthers)
            val roundedNet = (net * 100.0).roundToInt() / 100.0

            RoommateBalanceSummary(
                roommate = rm,
                totalPaidGroceries = (totalPaidGroceries * 100.0).roundToInt() / 100.0,
                totalOwedShare = (totalOwedShare * 100.0).roundToInt() / 100.0,
                paidToOthers = (paidToOthers * 100.0).roundToInt() / 100.0,
                receivedFromOthers = (receivedFromOthers * 100.0).roundToInt() / 100.0,
                netBalance = roundedNet
            )
        }
    }

    fun getDebtSettlementSuggestions(): List<DebtTransfer> {
        val summaries = getBalanceSummaries()
        return com.example.feature.apartmentmanager.domain.DebtSimplificationEngine.simplifyDebts(summaries).transfers
    }

    fun getDebtSimplificationResult(): com.example.feature.apartmentmanager.domain.DebtSimplificationEngine.SimplificationResult {
        val summaries = getBalanceSummaries()
        return com.example.feature.apartmentmanager.domain.DebtSimplificationEngine.simplifyDebts(summaries)
    }

    // --- Shared Envelope Budget CRUD & Spending Calculations ---
    fun getCategorySpending(category: ExpenseCategory): Double {
        return _expenses.value
            .filter { it.status == ExpenseStatus.APPROVED && it.category == category }
            .sumOf { it.amount }
    }

    fun getAllCategorySpending(): Map<ExpenseCategory, Double> {
        val result = mutableMapOf<ExpenseCategory, Double>()
        ExpenseCategory.entries.forEach { cat ->
            result[cat] = getCategorySpending(cat)
        }
        return result
    }

    fun setEnvelopeBudget(category: ExpenseCategory, monthlyCap: Double, alertThresholdPercent: Int = 85, notes: String = "") {
        val current = _envelopeBudgets.value
        val existingIndex = current.indexOfFirst { it.category == category }
        val updated = if (existingIndex >= 0) {
            current.map {
                if (it.category == category) it.copy(monthlyCap = monthlyCap, alertThresholdPercent = alertThresholdPercent, notes = notes)
                else it
            }
        } else {
            current + SharedEnvelopeBudget(
                id = UUID.randomUUID().toString().take(8),
                category = category,
                monthlyCap = monthlyCap,
                alertThresholdPercent = alertThresholdPercent,
                notes = notes
            )
        }
        _envelopeBudgets.value = updated
        val currency = _apartmentProfile.value.currencySymbol
        addNotification(
            title = "Household Budget Envelope Set",
            message = "${category.displayName} budget cap set to $currency${"%.2f".format(monthlyCap)} (Alert at $alertThresholdPercent%).",
            category = NotificationCategory.ADMIN_ACTION,
            authorName = "System"
        )
        saveData()
    }

    fun removeEnvelopeBudget(budgetId: String) {
        _envelopeBudgets.value = _envelopeBudgets.value.filter { it.id != budgetId }
        saveData()
    }

    private fun defaultEnvelopeBudgets(): List<SharedEnvelopeBudget> = listOf(
        SharedEnvelopeBudget(id = "b1", category = ExpenseCategory.GROCERIES, monthlyCap = 7000.0, alertThresholdPercent = 85, notes = "Monthly household groceries & veggies"),
        SharedEnvelopeBudget(id = "b2", category = ExpenseCategory.UTILITIES, monthlyCap = 2500.0, alertThresholdPercent = 80, notes = "Electricity, water, gas cylinders"),
        SharedEnvelopeBudget(id = "b3", category = ExpenseCategory.INTERNET_WIFI, monthlyCap = 1200.0, alertThresholdPercent = 90, notes = "High-speed broadband fibre"),
        SharedEnvelopeBudget(id = "b4", category = ExpenseCategory.HOUSEHOLD_SUPPLIES, monthlyCap = 1000.0, alertThresholdPercent = 80, notes = "Cleaning, detergents, toiletries")
    )

    // --- Reset to the User's Exact 4 Spreadsheets ---
    fun resetToDefaultData() {
        val defaultProfile = ApartmentProfile(
            name = "Apartment Flat 402",
            flatNumber = "Flat 402",
            currencySymbol = "₹",
            inviteCode = "APT402"
        )
        _apartmentProfile.value = defaultProfile

        // The 5 Roommates from Table 3
        val aniket = ApartmentRoommate("1", "Aniket", "Admin & Flat Coordinator", isAdmin = true, colorHex = 0xFFD32F2FL)
        val amit = ApartmentRoommate("2", "Amit", "Roommate", isAdmin = false, colorHex = 0xFF1976D2L)
        val rahul = ApartmentRoommate("3", "Rahul", "Roommate", isAdmin = false, colorHex = 0xFF388E3CL)
        val debang = ApartmentRoommate("4", "Debang", "Roommate", isAdmin = false, colorHex = 0xFFF57C00L)
        val akshay = ApartmentRoommate("5", "Akshay", "Roommate", isAdmin = false, colorHex = 0xFF7B1FA2L)

        val defaultRoommates = listOf(aniket, amit, rahul, debang, akshay)
        _roommates.value = defaultRoommates
        _activeRoommateId.value = "1"

        val all5 = listOf("1", "2", "3", "4", "5")
        val noAmit = listOf("1", "3", "4", "5") // Aniket, Rahul, Debang, Akshay
        val noDebang = listOf("1", "2", "3", "5") // Aniket, Amit, Rahul, Akshay
        val aniketDebang = listOf("1", "4")
        val aniketAmitRahul = listOf("1", "2", "3")
        val noRahul = listOf("1", "2", "4", "5") // Aniket, Amit, Debang, Akshay
        val aniketDebangAkshay = listOf("1", "4", "5")
        val amitDebangAkshay = listOf("2", "4", "5")
        val aniketAmitDebang = listOf("1", "2", "4")

        // The exact 48 items from Table 2
        val defaultExpenses = listOf(
            ApartmentExpense("e1", "21/07/2026", "Rice", 150.00, "1", noAmit),
            ApartmentExpense("e2", "22/07/2026", "Puri and other item", 260.00, "1", all5),
            ApartmentExpense("e3", "26/07/2026", "chicken and items", 220.00, "1", noDebang),
            ApartmentExpense("e4", "26/07/2026", "oil", 50.00, "5", noDebang),
            ApartmentExpense("e5", "26/07/2026", "oil", 50.00, "3", noDebang),
            ApartmentExpense("e6", "27/07/2026", "Rice and daal", 100.00, "3", all5),
            ApartmentExpense("e7", "27/07/2026", "Grocery", 360.00, "4", all5),
            ApartmentExpense("e8", "28/07/2026", "Onion", 20.00, "1", all5),
            ApartmentExpense("e9", "29/07/2026", "Print", 45.00, "4", aniketDebang),
            ApartmentExpense("e10", "30/07/2026", "Grocery", 290.00, "2", all5),
            ApartmentExpense("e11", "01/08/2026", "Grocery", 125.00, "1", noAmit),
            ApartmentExpense("e12", "02/08/2026", "Jeera", 35.00, "3", noAmit),
            ApartmentExpense("e13", "02/08/2026", "Roti & Sabji", 155.00, "4", all5),
            ApartmentExpense("e14", "04/08/2026", "chawal and Allu", 140.00, "2", all5),
            ApartmentExpense("e15", "04/08/2026", "oil + allu", 155.00, "4", all5),
            ApartmentExpense("e16", "05/08/2026", "Onion and Chilli", 100.00, "2", noDebang),
            ApartmentExpense("e17", "05/08/2026", "Atta", 192.00, "2", noDebang),
            ApartmentExpense("e18", "07/08/2026", "Chicken and items", 350.00, "2", noDebang),
            ApartmentExpense("e19", "08/08/2026", "Rice and daal", 203.00, "1", noDebang),
            ApartmentExpense("e20", "10/08/2026", "Chicken and items", 250.00, "2", aniketAmitRahul),
            ApartmentExpense("e21", "11/08/2026", "Rice", 60.00, "1", all5),
            ApartmentExpense("e22", "12/08/2026", "Rice", 170.00, "1", all5),
            ApartmentExpense("e23", "13/08/2026", "Egg", 50.00, "1", noDebang),
            ApartmentExpense("e24", "14/08/2026", "Rice", 260.00, "5", noDebang),
            ApartmentExpense("e25", "15/08/2026", "maggie", 100.00, "2", noDebang),
            ApartmentExpense("e26", "16/08/2026", "chawal and Allu", 73.00, "5", noDebang),
            ApartmentExpense("e27", "16/08/2026", "chawal and Allu", 50.00, "2", noDebang),
            ApartmentExpense("e28", "17/08/2026", "Atta & allu", 215.00, "1", noDebang),
            ApartmentExpense("e29", "17/08/2026", "Mirchi", 10.00, "3", noDebang),
            ApartmentExpense("e30", "18/08/2026", "Onion + Salt", 40.00, "5", noDebang),
            ApartmentExpense("e31", "18/08/2026", "Onion + Salt", 10.00, "1", noDebang),
            ApartmentExpense("e32", "19/08/2026", "Chawal and Daal", 130.00, "1", noDebang),
            ApartmentExpense("e33", "20/08/2026", "chawal", 37.00, "5", noDebang),
            ApartmentExpense("e34", "20/08/2026", "chawal", 37.00, "3", noDebang),
            ApartmentExpense("e35", "21/08/2026", "chicken", 150.00, "1", aniketAmitRahul),
            ApartmentExpense("e36", "21/08/2026", "atta and sttuf", 110.00, "1", aniketAmitRahul),
            ApartmentExpense("e37", "21/08/2026", "mirchi", 30.00, "2", noDebang),
            ApartmentExpense("e38", "22/08/2026", "Roti & Swai", 205.00, "2", all5),
            ApartmentExpense("e39", "25/08/2026", "rice", 143.00, "5", noRahul),
            ApartmentExpense("e40", "26/08/2026", "oil", 25.00, "1", noRahul),
            ApartmentExpense("e41", "26/08/2026", "daal", 50.00, "2", listOf("1", "4", "5")),
            ApartmentExpense("e42", "27/08/2026", "kachori", 65.00, "4", aniketDebang),
            ApartmentExpense("e43", "29/08/2026", "chawal + egg", 90.00, "4", aniketAmitDebang),
            ApartmentExpense("e44", "29/08/2026", "chicken + onion", 250.00, "4", aniketAmitDebang),
            ApartmentExpense("e45", "01/09/2026", "rice nad things", 235.00, "4", aniketAmitDebang),
            ApartmentExpense("e46", "30/08/2026", "rice and pasta", 60.00, "4", aniketAmitDebang),
            ApartmentExpense("e47", "10/09/2026", "rice and egg", 180.00, "2", noRahul),
            ApartmentExpense("e48", "10/09/2026", "rice and mixture", 60.00, "1", all5)
        )
        _expenses.value = defaultExpenses

        // The exact 36 settlements from Table 4 (all verified and approved by Admin Aniket)
        val defaultSettlements = listOf(
            ApartmentSettlement("s1", "21/07/2026", "3", "1", 37.50, "", SettlementStatus.APPROVED, "1", "21/07/2026"),
            ApartmentSettlement("s2", "21/07/2026", "5", "1", 37.50, "", SettlementStatus.APPROVED, "1", "21/07/2026"),
            ApartmentSettlement("s3", "22/07/2026", "3", "1", 52.00, "", SettlementStatus.APPROVED, "1", "22/07/2026"),
            ApartmentSettlement("s4", "22/07/2026", "5", "1", 52.00, "", SettlementStatus.APPROVED, "1", "22/07/2026"),
            ApartmentSettlement("s5", "22/07/2026", "2", "1", 20.00, "", SettlementStatus.APPROVED, "1", "22/07/2026"),
            ApartmentSettlement("s6", "21/07/2026", "5", "1", 50.00, "", SettlementStatus.APPROVED, "1", "21/07/2026"),
            ApartmentSettlement("s7", "28/07/2026", "5", "4", 77.00, "", SettlementStatus.APPROVED, "1", "28/07/2026"),
            ApartmentSettlement("s8", "31/07/2026", "4", "1", 15.00, "", SettlementStatus.APPROVED, "1", "31/07/2026"),
            ApartmentSettlement("s9", "31/07/2026", "5", "1", 15.00, "", SettlementStatus.APPROVED, "1", "31/07/2026"),
            ApartmentSettlement("s10", "01/08/2026", "3", "1", 40.00, "", SettlementStatus.APPROVED, "1", "01/08/2026"),
            ApartmentSettlement("s11", "02/08/2026", "5", "4", 30.00, "", SettlementStatus.APPROVED, "1", "02/08/2026"),
            ApartmentSettlement("s12", "03/08/2026", "2", "1", 55.00, "", SettlementStatus.APPROVED, "1", "03/08/2026"),
            ApartmentSettlement("s13", "04/08/2026", "5", "4", 70.00, "", SettlementStatus.APPROVED, "1", "04/08/2026"),
            ApartmentSettlement("s14", "05/08/2026", "5", "2", 10.00, "", SettlementStatus.APPROVED, "1", "05/08/2026"),
            ApartmentSettlement("s15", "07/08/2026", "5", "2", 90.00, "", SettlementStatus.APPROVED, "1", "07/08/2026"),
            ApartmentSettlement("s16", "07/08/2026", "3", "2", 10.00, "", SettlementStatus.APPROVED, "1", "07/08/2026"),
            ApartmentSettlement("s17", "31/07/2026", "1", "2", 213.50, "", SettlementStatus.APPROVED, "1", "31/07/2026"),
            ApartmentSettlement("s18", "11/08/2026", "5", "1", 10.00, "", SettlementStatus.APPROVED, "1", "11/08/2026"),
            ApartmentSettlement("s19", "11/08/2026", "5", "4", 10.00, "", SettlementStatus.APPROVED, "1", "11/08/2026"),
            ApartmentSettlement("s20", "12/08/2026", "5", "1", 153.00, "", SettlementStatus.APPROVED, "1", "12/08/2026"),
            ApartmentSettlement("s21", "12/08/2026", "1", "2", 46.00, "", SettlementStatus.APPROVED, "1", "12/08/2026"),
            ApartmentSettlement("s22", "12/08/2026", "3", "1", 300.00, "", SettlementStatus.APPROVED, "1", "12/08/2026"),
            ApartmentSettlement("s23", "12/08/2026", "1", "2", 164.08, "", SettlementStatus.APPROVED, "1", "12/08/2026"),
            ApartmentSettlement("s24", "14/08/2026", "2", "5", 130.00, "", SettlementStatus.APPROVED, "1", "14/08/2026"),
            ApartmentSettlement("s25", "17/08/2026", "3", "1", 30.00, "", SettlementStatus.APPROVED, "1", "17/08/2026"),
            ApartmentSettlement("s26", "21/08/2026", "1", "5", 12.50, "", SettlementStatus.APPROVED, "1", "21/08/2026"),
            ApartmentSettlement("s27", "22/08/2026", "3", "2", 34.00, "", SettlementStatus.APPROVED, "1", "22/08/2026"),
            ApartmentSettlement("s28", "22/08/2026", "5", "2", 34.00, "", SettlementStatus.APPROVED, "1", "22/08/2026"),
            ApartmentSettlement("s29", "22/08/2026", "1", "2", 100.00, "", SettlementStatus.APPROVED, "1", "22/08/2026"),
            ApartmentSettlement("s30", "24/08/2026", "4", "1", 15.00, "", SettlementStatus.APPROVED, "1", "24/08/2026"),
            ApartmentSettlement("s31", "26/08/2026", "4", "1", 55.00, "", SettlementStatus.APPROVED, "1", "26/08/2026"),
            ApartmentSettlement("s32", "27/08/2026", "4", "1", 160.00, "", SettlementStatus.APPROVED, "1", "27/08/2026"),
            ApartmentSettlement("s33", "01/09/2026", "3", "4", 200.00, "", SettlementStatus.APPROVED, "1", "01/09/2026"),
            ApartmentSettlement("s34", "01/09/2026", "2", "4", 98.00, "", SettlementStatus.APPROVED, "1", "01/09/2026"),
            ApartmentSettlement("s35", "10/09/2026", "5", "1", 20.00, "", SettlementStatus.APPROVED, "1", "10/09/2026"),
            ApartmentSettlement("s36", "10/09/2026", "3", "1", 12.00, "", SettlementStatus.APPROVED, "1", "10/09/2026")
        )
        _settlements.value = defaultSettlements

        // Initial default activity notifications
        val now = System.currentTimeMillis()
        val defaultNotifs = listOf(
            ApartmentNotification(
                id = "n1",
                timestamp = now - 1000 * 60 * 15,
                title = "Flat 402 Initialized",
                message = "Apartment Flat 402 ledger synchronized with 5 roommates and 48 purchase records.",
                category = NotificationCategory.GENERAL,
                authorName = "Aniket (Admin)",
                isRead = false
            ),
            ApartmentNotification(
                id = "n2",
                timestamp = now - 1000 * 60 * 10,
                title = "All 36 Past Settlements Approved",
                message = "Admin Aniket verified and approved all historical payments from the apartment ledger.",
                category = NotificationCategory.ADMIN_ACTION,
                authorName = "Aniket (Admin)",
                isRead = false
            ),
            ApartmentNotification(
                id = "n3",
                timestamp = now - 1000 * 60 * 5,
                title = "Recent Purchase Logged",
                message = "Aniket purchased 'rice and mixture' for ₹60.00 (Split among 5 roommates).",
                category = NotificationCategory.PURCHASE,
                authorName = "Aniket",
                isRead = false
            )
        )
        _notifications.value = defaultNotifs
        _envelopeBudgets.value = defaultEnvelopeBudgets()

        saveData()
    }
}
