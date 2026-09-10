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
                    list.add(
                        ApartmentExpense(
                            id = obj.getString("id"),
                            date = obj.getString("date"),
                            item = obj.getString("item"),
                            amount = obj.getDouble("amount"),
                            paidByRoommateId = obj.getString("paidBy"),
                            sharedByRoommateIds = sharedList,
                            notes = obj.optString("notes", "")
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
                    list.add(
                        ApartmentSettlement(
                            id = obj.getString("id"),
                            date = obj.getString("date"),
                            fromRoommateId = obj.getString("from"),
                            toRoommateId = obj.getString("to"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", "")
                        )
                    )
                }
                _settlements.value = list
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
            setArray.put(obj)
        }
        editor.putString("settlements", setArray.toString())
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
        saveData()
    }

    fun updateRoommate(roommate: ApartmentRoommate) {
        _roommates.value = _roommates.value.map { if (it.id == roommate.id) roommate else it }
        saveData()
    }

    fun deleteRoommate(roommateId: String) {
        if (_roommates.value.size <= 1) return // Keep at least one
        _roommates.value = _roommates.value.filter { it.id != roommateId }
        if (_activeRoommateId.value == roommateId) {
            _activeRoommateId.value = _roommates.value.firstOrNull()?.id ?: ""
        }
        saveData()
    }

    // --- Expenses CRUD ---
    fun addExpense(
        date: String,
        item: String,
        amount: Double,
        paidByRoommateId: String,
        sharedByRoommateIds: List<String>,
        notes: String = ""
    ) {
        val expense = ApartmentExpense(
            id = UUID.randomUUID().toString().take(8),
            date = date,
            item = item,
            amount = amount,
            paidByRoommateId = paidByRoommateId,
            sharedByRoommateIds = sharedByRoommateIds,
            notes = notes
        )
        // Add to the front so newest appears first
        _expenses.value = listOf(expense) + _expenses.value
        saveData()
    }

    fun updateExpense(expense: ApartmentExpense) {
        _expenses.value = _expenses.value.map { if (it.id == expense.id) expense else it }
        saveData()
    }

    fun deleteExpense(expenseId: String) {
        _expenses.value = _expenses.value.filter { it.id != expenseId }
        saveData()
    }

    // --- Settlements CRUD ---
    fun addSettlement(
        date: String,
        fromRoommateId: String,
        toRoommateId: String,
        amount: Double,
        note: String = ""
    ) {
        val settlement = ApartmentSettlement(
            id = UUID.randomUUID().toString().take(8),
            date = date,
            fromRoommateId = fromRoommateId,
            toRoommateId = toRoommateId,
            amount = amount,
            note = note
        )
        _settlements.value = listOf(settlement) + _settlements.value
        saveData()
    }

    fun deleteSettlement(settlementId: String) {
        _settlements.value = _settlements.value.filter { it.id != settlementId }
        saveData()
    }

    // --- Balance Calculations (Matches Spreadsheet Table 1) ---
    fun getBalanceSummaries(): List<RoommateBalanceSummary> {
        val rms = _roommates.value
        val exps = _expenses.value
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

            // Paid to Others (Settlements where from == rm.id)
            val paidToOthers = sets
                .filter { it.fromRoommateId == rm.id }
                .sumOf { it.amount }

            // Received from Others (Settlements where to == rm.id)
            val receivedFromOthers = sets
                .filter { it.toRoommateId == rm.id }
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
        val debtors = summaries.filter { it.netBalance < -0.01 }
            .map { it.roommate to abs(it.netBalance) }
            .toMutableList()
        val creditors = summaries.filter { it.netBalance > 0.01 }
            .map { it.roommate to it.netBalance }
            .toMutableList()

        val transfers = mutableListOf<DebtTransfer>()
        var dIndex = 0
        var cIndex = 0

        while (dIndex < debtors.size && cIndex < creditors.size) {
            val (debtor, debtAmount) = debtors[dIndex]
            val (creditor, creditAmount) = creditors[cIndex]

            val settleAmount = minOf(debtAmount, creditAmount)
            val roundedSettle = (settleAmount * 100.0).roundToInt() / 100.0

            if (roundedSettle > 0.0) {
                transfers.add(DebtTransfer(fromRoommate = debtor, toRoommate = creditor, amount = roundedSettle))
            }

            val remainingDebt = debtAmount - settleAmount
            val remainingCredit = creditAmount - settleAmount

            if (remainingDebt < 0.01) {
                dIndex++
            } else {
                debtors[dIndex] = debtor to remainingDebt
            }

            if (remainingCredit < 0.01) {
                cIndex++
            } else {
                creditors[cIndex] = creditor to remainingCredit
            }
        }

        return transfers
    }

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

        // The exact 36 settlements from Table 4
        val defaultSettlements = listOf(
            ApartmentSettlement("s1", "21/07/2026", "3", "1", 37.50, ""),
            ApartmentSettlement("s2", "21/07/2026", "5", "1", 37.50, ""),
            ApartmentSettlement("s3", "22/07/2026", "3", "1", 52.00, ""),
            ApartmentSettlement("s4", "22/07/2026", "5", "1", 52.00, ""),
            ApartmentSettlement("s5", "22/07/2026", "2", "1", 20.00, ""),
            ApartmentSettlement("s6", "21/07/2026", "5", "1", 50.00, ""),
            ApartmentSettlement("s7", "28/07/2026", "5", "4", 77.00, ""),
            ApartmentSettlement("s8", "31/07/2026", "4", "1", 15.00, ""),
            ApartmentSettlement("s9", "31/07/2026", "5", "1", 15.00, ""),
            ApartmentSettlement("s10", "01/08/2026", "3", "1", 40.00, ""),
            ApartmentSettlement("s11", "02/08/2026", "5", "4", 30.00, ""),
            ApartmentSettlement("s12", "03/08/2026", "2", "1", 55.00, ""),
            ApartmentSettlement("s13", "04/08/2026", "5", "4", 70.00, ""),
            ApartmentSettlement("s14", "05/08/2026", "5", "2", 10.00, ""),
            ApartmentSettlement("s15", "07/08/2026", "5", "2", 90.00, ""),
            ApartmentSettlement("s16", "07/08/2026", "3", "2", 10.00, ""),
            ApartmentSettlement("s17", "31/07/2026", "1", "2", 213.50, ""),
            ApartmentSettlement("s18", "11/08/2026", "5", "1", 10.00, ""),
            ApartmentSettlement("s19", "11/08/2026", "5", "4", 10.00, ""),
            ApartmentSettlement("s20", "12/08/2026", "5", "1", 153.00, ""),
            ApartmentSettlement("s21", "12/08/2026", "1", "2", 46.00, ""),
            ApartmentSettlement("s22", "12/08/2026", "3", "1", 300.00, ""),
            ApartmentSettlement("s23", "12/08/2026", "1", "2", 164.08, ""),
            ApartmentSettlement("s24", "14/08/2026", "2", "5", 130.00, ""),
            ApartmentSettlement("s25", "17/08/2026", "3", "1", 30.00, ""),
            ApartmentSettlement("s26", "21/08/2026", "1", "5", 12.50, ""),
            ApartmentSettlement("s27", "22/08/2026", "3", "2", 34.00, ""),
            ApartmentSettlement("s28", "22/08/2026", "5", "2", 34.00, ""),
            ApartmentSettlement("s29", "22/08/2026", "1", "2", 100.00, ""),
            ApartmentSettlement("s30", "24/08/2026", "4", "1", 15.00, ""),
            ApartmentSettlement("s31", "26/08/2026", "4", "1", 55.00, ""),
            ApartmentSettlement("s32", "27/08/2026", "4", "1", 160.00, ""),
            ApartmentSettlement("s33", "01/09/2026", "3", "4", 200.00, ""),
            ApartmentSettlement("s34", "01/09/2026", "2", "4", 98.00, ""),
            ApartmentSettlement("s35", "10/09/2026", "5", "1", 20.00, ""),
            ApartmentSettlement("s36", "10/09/2026", "3", "1", 12.00, "")
        )
        _settlements.value = defaultSettlements

        saveData()
    }
}
