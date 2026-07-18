package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApartmentRepository private constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var preferenceManager: PreferenceManager? = null
    private var appContext: Context? = null

    // Raw data flows populated by Firestore real-time listeners
    private val _rawRoommates = MutableStateFlow<List<Roommate>>(emptyList())
    private val _rawBills = MutableStateFlow<List<Bill>>(emptyList())
    private val _rawSettlements = MutableStateFlow<List<Settlement>>(emptyList())
    private val _rawNotifications = MutableStateFlow<List<Notification>>(emptyList())

    // Active apartment metadata
    private val _activeApartmentId = MutableStateFlow<String?>(null)
    val activeApartmentId: StateFlow<String?> = _activeApartmentId.asStateFlow()

    private val _activeApartmentName = MutableStateFlow<String?>(null)
    val activeApartmentName: StateFlow<String?> = _activeApartmentName.asStateFlow()

    private val _activeApartmentInviteCode = MutableStateFlow<String?>(null)
    val activeApartmentInviteCode: StateFlow<String?> = _activeApartmentInviteCode.asStateFlow()

    // Loading & Error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Auth flows
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid ?: "guest")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Firestore listener registrations
    private var roommateListener: ListenerRegistration? = null
    private var billListener: ListenerRegistration? = null
    private var settlementListener: ListenerRegistration? = null
    private var apartmentListener: ListenerRegistration? = null
    private var notificationListener: ListenerRegistration? = null

    // Public reactively combined roommate balances with settlements and bills applied
    val roommates: StateFlow<List<Roommate>> = combine(
        _rawRoommates,
        _rawBills,
        _rawSettlements
    ) { rawRms, rawBills, rawSetts ->
        recalculateBalances(rawRms, rawBills, rawSetts)
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val bills: StateFlow<List<Bill>> = _rawBills.asStateFlow()
    val settlements: StateFlow<List<Settlement>> = _rawSettlements.asStateFlow()

    val notifications: StateFlow<List<Notification>> = combine(
        _rawNotifications,
        _currentUserId
    ) { rawNotifs, currentUid ->
        rawNotifs.filter { it.recipientId == currentUid }
            .sortedByDescending { it.timestamp }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    init {
        // Configure Firestore offline persistence
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            Log.e("ApartmentRepository", "Error setting firestore settings", e)
        }

        // Real-time Auth changes
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid ?: "guest"
            _currentUserId.value = uid
            
            // If the user logs out, clear current apartment listeners and local state
            if (firebaseAuth.currentUser == null) {
                clearApartmentState()
            }
        }
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = PreferenceManager.getInstance(context)
        preferenceManager = prefs

        // Listen for the active apartment ID saved in DataStore
        repositoryScope.launch {
            prefs.apartmentId.collect { savedApartmentId ->
                if (savedApartmentId != _activeApartmentId.value) {
                    _activeApartmentId.value = savedApartmentId
                    if (savedApartmentId != null) {
                        startListeningToApartment(savedApartmentId)
                    } else {
                        clearApartmentState()
                    }
                }
            }
        }
    }

    fun getLanguage(): kotlinx.coroutines.flow.Flow<String> {
        return preferenceManager?.language ?: kotlinx.coroutines.flow.flowOf("en")
    }

    fun saveLanguage(lang: String) {
        repositoryScope.launch {
            preferenceManager?.saveLanguage(lang)
        }
    }

    fun getThemeMode(): kotlinx.coroutines.flow.Flow<String> {
        return preferenceManager?.themeMode ?: kotlinx.coroutines.flow.flowOf("system")
    }

    fun saveThemeMode(mode: String) {
        repositoryScope.launch {
            preferenceManager?.saveThemeMode(mode)
        }
    }

    fun getCurrency(): kotlinx.coroutines.flow.Flow<String> {
        return preferenceManager?.currency ?: kotlinx.coroutines.flow.flowOf("USD")
    }

    fun saveCurrency(curr: String) {
        repositoryScope.launch {
            preferenceManager?.saveCurrency(curr)
        }
    }

    private fun startListeningToApartment(apartmentId: String) {
        // Cancel previous listeners
        stopListening()
        _isLoading.value = true

        // Listen to apartment metadata
        apartmentListener = db.collection("apartments").document(apartmentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Failed to load apartment details: ${e.localizedMessage}"
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _activeApartmentName.value = snapshot.getString("name")
                    _activeApartmentInviteCode.value = snapshot.getString("inviteCode")
                }
            }

        // Listen to roommates subcollection
        roommateListener = db.collection("apartments").document(apartmentId)
            .collection("roommates")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "Roommates sync error: ${e.localizedMessage}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.mapNotNull { doc ->
                        try {
                            doc.toObject(Roommate::class.java)
                        } catch (ex: Exception) {
                            Log.e("ApartmentRepository", "Error deserializing Roommate", ex)
                            null
                        }
                    }
                    _rawRoommates.value = list
                }
            }

        // Listen to bills subcollection
        billListener = db.collection("apartments").document(apartmentId)
            .collection("bills")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Bills sync error: ${e.localizedMessage}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.mapNotNull { doc ->
                        try {
                            doc.toObject(Bill::class.java)
                        } catch (ex: Exception) {
                            Log.e("ApartmentRepository", "Error deserializing Bill", ex)
                            null
                        }
                    }
                    _rawBills.value = list
                }
            }

        // Listen to settlements subcollection
        settlementListener = db.collection("apartments").document(apartmentId)
            .collection("settlements")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Settlements sync error: ${e.localizedMessage}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.mapNotNull { doc ->
                        try {
                            doc.toObject(Settlement::class.java)
                        } catch (ex: Exception) {
                            Log.e("ApartmentRepository", "Error deserializing Settlement", ex)
                            null
                        }
                    }
                    _rawSettlements.value = list
                }
            }

        // Listen to notifications subcollection
        notificationListener = db.collection("apartments").document(apartmentId)
            .collection("notifications")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Notifications sync error: ${e.localizedMessage}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.mapNotNull { doc ->
                        try {
                            doc.toObject(Notification::class.java)
                        } catch (ex: Exception) {
                            Log.e("ApartmentRepository", "Error deserializing Notification", ex)
                            null
                        }
                    }
                    _rawNotifications.value = list
                }
            }
    }

    private fun stopListening() {
        roommateListener?.remove()
        billListener?.remove()
        settlementListener?.remove()
        apartmentListener?.remove()
        notificationListener?.remove()

        roommateListener = null
        billListener = null
        settlementListener = null
        apartmentListener = null
        notificationListener = null
    }

    private fun clearApartmentState() {
        stopListening()
        _rawRoommates.value = emptyList()
        _rawBills.value = emptyList()
        _rawSettlements.value = emptyList()
        _rawNotifications.value = emptyList()
        _activeApartmentName.value = null
        _activeApartmentInviteCode.value = null
        _isLoading.value = false
        _error.value = null
    }

    // Dynamic balance calculations that combine Bills and Settlements correctly
    private fun recalculateBalances(
        roommatesList: List<Roommate>,
        billsList: List<Bill>,
        settlementsList: List<Settlement>
    ): List<Roommate> {
        if (roommatesList.isEmpty()) return emptyList()

        val roommateCount = roommatesList.size

        // Step 1: Initialize maps for tracking how much each roommate has actually paid and their share
        val paidMap = roommatesList.associate { it.id to 0.0 }.toMutableMap()
        val shareMap = roommatesList.associate { it.id to 0.0 }.toMutableMap()

        // Step 2: Sum up regular bill shares with exact cent division
        billsList.forEach { bill ->
            val payerId = bill.payerId
            val amount = bill.amount

            // Increment what the payer paid
            paidMap[payerId] = (paidMap[payerId] ?: 0.0) + amount

            // Determine active split participants (default to all if list is empty)
            val participants = if (bill.splitAmongIds.isNotEmpty()) {
                bill.splitAmongIds.filter { id -> roommatesList.any { it.id == id } }
            } else {
                roommatesList.map { it.id }
            }

            if (participants.isNotEmpty()) {
                val totalCents = Math.round(amount * 100.0).toInt()
                val n = participants.size
                val baseCents = totalCents / n
                val leftoverCents = totalCents % n

                participants.forEachIndexed { index, participantId ->
                    val extraCents = if (index < leftoverCents) 1 else 0
                    val share = (baseCents + extraCents) / 100.0
                    shareMap[participantId] = (shareMap[participantId] ?: 0.0) + share
                }
            }
        }

        // Step 3: Combine with bilateral Settlements cleanly
        settlementsList.forEach { settlement ->
            val fromId = settlement.fromId
            val toId = settlement.toId
            val amount = settlement.amount

            // fromId gets credited for settling the debt (they paid this extra)
            paidMap[fromId] = (paidMap[fromId] ?: 0.0) + amount

            // toId owes this extra (they received it, offsetting what they are owed)
            shareMap[toId] = (shareMap[toId] ?: 0.0) + amount
        }

        // Step 4: Compute each roommate's net balance: (Paid) - (Owed Share)
        return roommatesList.map { roommate ->
            val totalPaid = paidMap[roommate.id] ?: 0.0
            val totalShare = shareMap[roommate.id] ?: 0.0
            val balance = totalPaid - totalShare

            roommate.copy(
                totalPaid = totalPaid,
                balance = balance
            )
        }
    }

    // Helper to calculate total spent
    fun getTotalSpent(): Double {
        return _rawBills.value.sumOf { it.amount }
    }

    // Helper to get debts (who owes who)
    fun getDebts(): List<Debt> {
        val currentRoommates = roommates.value
        if (currentRoommates.isEmpty()) return emptyList()

        // Roommates who owe (balance < -0.01)
        val debtors = currentRoommates.filter { it.balance < -0.01 }.map { it.copy() }.toMutableList()
        // Roommates who are owed (balance > 0.01)
        val creditors = currentRoommates.filter { it.balance > 0.01 }.map { it.copy() }.toMutableList()

        val debts = mutableListOf<Debt>()

        var dIdx = 0
        var cIdx = 0

        // Greedy matching of debts
        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtor = debtors[dIdx]
            val creditor = creditors[cIdx]

            val debtAmount = -debtor.balance
            val creditAmount = creditor.balance

            if (debtAmount < 0.01) {
                dIdx++
                continue
            }
            if (creditAmount < 0.01) {
                cIdx++
                continue
            }

            val settleAmount = minOf(debtAmount, creditAmount)
            debts.add(Debt(fromId = debtor.id, toId = creditor.id, amount = settleAmount))

            // Adjust balances
            debtors[dIdx] = debtor.copy(balance = debtor.balance + settleAmount)
            creditors[cIdx] = creditor.copy(balance = creditor.balance - settleAmount)
        }

        return debts
    }

    // CREATE Flow
    fun createApartment(apartmentName: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "No signed-in user found")
        val generatedId = db.collection("apartments").document().id
        val inviteCode = generateInviteCode()

        val apartmentDoc = hashMapOf(
            "name" to apartmentName,
            "inviteCode" to inviteCode,
            "memberIds" to listOf(currentUid),
            "createdAt" to System.currentTimeMillis()
        )

        _isLoading.value = true
        db.collection("apartments").document(generatedId)
            .set(apartmentDoc)
            .addOnSuccessListener {
                // Now create the roommate profile for the creator under the roommate's subcollection
                val user = auth.currentUser
                val displayName = user?.displayName ?: "Owner User"
                val initials = displayName.split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .joinToString("")
                    .take(2)

                val colors = listOf(0xFFD1E4FF, 0xFFFFDBCF, 0xFFD2E5D5, 0xFFE6DEFF, 0xFFF5E0FF, 0xFFFFF1C5)
                val textColors = listOf(0xFF001D36, 0xFF350B00, 0xFF00210E, 0xFF1D1633, 0xFF2A004B, 0xFF2F1D00)
                val randomIndex = currentUid.hashCode().coerceAtLeast(0) % colors.size

                val firstRoommate = Roommate(
                    id = currentUid,
                    name = "$displayName (You)",
                    initials = if (initials.isNotEmpty()) initials else "RM",
                    avatarBgColor = colors[randomIndex],
                    avatarTextColor = textColors[randomIndex],
                    totalPaid = 0.0,
                    balance = 0.0
                )

                db.collection("apartments").document(generatedId)
                    .collection("roommates").document(currentUid)
                    .set(firstRoommate)
                    .addOnSuccessListener {
                        repositoryScope.launch {
                            preferenceManager?.saveApartmentId(generatedId)
                            _activeApartmentId.value = generatedId
                            _isLoading.value = false
                            onResult(true, null)
                        }
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        onResult(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onResult(false, e.localizedMessage)
            }
    }

    // JOIN-BY-CODE Flow
    fun joinApartment(enteredInviteCode: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "No signed-in user found")
        val codeToSearch = enteredInviteCode.uppercase().trim()

        if (codeToSearch.length != 6) {
            return onResult(false, "Invite code must be exactly 6 characters")
        }

        _isLoading.value = true
        db.collection("apartments")
            .whereEqualTo("inviteCode", codeToSearch)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    _isLoading.value = false
                    onResult(false, "Invalid invite code. Apartment not found.")
                    return@addOnSuccessListener
                }

                val doc = querySnapshot.documents.first()
                val apartmentId = doc.id
                @Suppress("UNCHECKED_CAST")
                val memberIds = doc.get("memberIds") as? List<String> ?: emptyList()

                if (currentUid in memberIds) {
                    // Already a member, just select it
                    repositoryScope.launch {
                        preferenceManager?.saveApartmentId(apartmentId)
                        _activeApartmentId.value = apartmentId
                        _isLoading.value = false
                        onResult(true, null)
                    }
                    return@addOnSuccessListener
                }

                // Add uid to memberIds
                val updatedMembers = memberIds + currentUid
                db.collection("apartments").document(apartmentId)
                    .update("memberIds", updatedMembers)
                    .addOnSuccessListener {
                        // Notify existing members
                        val newMemberName = auth.currentUser?.displayName ?: "A new member"
                        memberIds.forEach { existingMemberId ->
                            val docRef = db.collection("apartments").document(apartmentId)
                                .collection("notifications").document()
                            val newNotification = Notification(
                                id = docRef.id,
                                recipientId = existingMemberId,
                                title = "New Roommate",
                                message = "$newMemberName joined the apartment!",
                                timestamp = System.currentTimeMillis(),
                                read = false
                            )
                            docRef.set(newNotification)
                        }

                        // Create their Roommate profile under subcollection
                        val user = auth.currentUser
                        val displayName = user?.displayName ?: "New Roommate"
                        val initials = displayName.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                            .take(2)

                        val colors = listOf(0xFFD1E4FF, 0xFFFFDBCF, 0xFFD2E5D5, 0xFFE6DEFF, 0xFFF5E0FF, 0xFFFFF1C5)
                        val textColors = listOf(0xFF001D36, 0xFF350B00, 0xFF00210E, 0xFF1D1633, 0xFF2A004B, 0xFF2F1D00)
                        val randomIndex = currentUid.hashCode().coerceAtLeast(0) % colors.size

                        val newRoommate = Roommate(
                            id = currentUid,
                            name = displayName,
                            initials = if (initials.isNotEmpty()) initials else "RM",
                            avatarBgColor = colors[randomIndex],
                            avatarTextColor = textColors[randomIndex],
                            totalPaid = 0.0,
                            balance = 0.0
                        )

                        db.collection("apartments").document(apartmentId)
                            .collection("roommates").document(currentUid)
                            .set(newRoommate)
                            .addOnSuccessListener {
                                repositoryScope.launch {
                                    preferenceManager?.saveApartmentId(apartmentId)
                                    _activeApartmentId.value = apartmentId
                                    _isLoading.value = false
                                    onResult(true, null)
                                }
                            }
                            .addOnFailureListener { e ->
                                _isLoading.value = false
                                onResult(false, e.localizedMessage)
                            }
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        onResult(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onResult(false, e.localizedMessage)
            }
    }

    // LEAVE Flow
    fun leaveApartment() {
        repositoryScope.launch {
            preferenceManager?.saveApartmentId(null)
            clearApartmentState()
            _activeApartmentId.value = null
        }
    }

    // BILL READ/WRITE Flow
    fun addBill(title: String, amount: Double, category: BillCategory, payerId: String, description: String = "", date: String? = null, splitAmongIds: List<String> = emptyList()) {
        val aptId = _activeApartmentId.value ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = date ?: sdf.format(Date())
        val generatedBillId = db.collection("apartments").document(aptId).collection("bills").document().id

        val newBill = Bill(
            id = generatedBillId,
            title = title,
            amount = amount,
            category = category,
            date = dateString,
            payerId = payerId,
            description = description,
            splitAmongIds = splitAmongIds
        )

        db.collection("apartments").document(aptId)
            .collection("bills").document(generatedBillId)
            .set(newBill)
            .addOnSuccessListener {
                repositoryScope.launch {
                    val payerName = _rawRoommates.value.find { it.id == payerId }?.name ?: "Someone"
                    val currentCurrency = preferenceManager?.currency?.firstOrNull() ?: "USD"
                    val amountStr = com.example.util.CurrencyFormatter.format(amount, currentCurrency)
                    
                    _rawRoommates.value.forEach { roommate ->
                        if (roommate.id != payerId) {
                            val localizedTitle = appContext?.getString(R.string.notification_bill_added_title) ?: "New Bill Added"
                            val localizedMsg = appContext?.getString(R.string.notification_bill_added_msg, payerName, title, amountStr)
                                ?: "$payerName added a bill for $title ($amountStr)"
                            addNotification(
                                recipientId = roommate.id,
                                title = localizedTitle,
                                message = localizedMsg
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to add bill: ${e.localizedMessage}"
            }
    }

    fun deleteBill(billId: String) {
        val aptId = _activeApartmentId.value ?: return
        db.collection("apartments").document(aptId)
            .collection("bills").document(billId)
            .delete()
            .addOnFailureListener { e ->
                _error.value = "Failed to delete bill: ${e.localizedMessage}"
            }
    }

    // Add Roommate manually
    fun addGuest(name: String) {
        val aptId = _activeApartmentId.value ?: return
        val generatedRoommateId = "guest_" + name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis().toString().takeLast(4)
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").take(2)
        val colors = listOf(0xFFD1E4FF, 0xFFFFDBCF, 0xFFD2E5D5, 0xFFE6DEFF, 0xFFF5E0FF, 0xFFFFF1C5)
        val textColors = listOf(0xFF001D36, 0xFF350B00, 0xFF00210E, 0xFF1D1633, 0xFF2A004B, 0xFF2F1D00)
        val randomIndex = name.hashCode().coerceAtLeast(0) % colors.size
        val newRoommate = Roommate(
            id = generatedRoommateId,
            name = name,
            initials = if (initials.isNotEmpty()) initials else "GU",
            isGuest = true,
            avatarBgColor = colors[randomIndex],
            avatarTextColor = textColors[randomIndex],
            totalPaid = 0.0,
            balance = 0.0
        )
        db.collection("apartments").document(aptId)
            .collection("roommates").document(generatedRoommateId)
            .set(newRoommate)
    }

    fun addRoommate(name: String) {
        val aptId = _activeApartmentId.value ?: return
        val generatedRoommateId = name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis().toString().takeLast(4)
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .take(2)

        val colors = listOf(0xFFD1E4FF, 0xFFFFDBCF, 0xFFD2E5D5, 0xFFE6DEFF, 0xFFF5E0FF, 0xFFFFF1C5)
        val textColors = listOf(0xFF001D36, 0xFF350B00, 0xFF00210E, 0xFF1D1633, 0xFF2A004B, 0xFF2F1D00)
        val randomIndex = name.hashCode().coerceAtLeast(0) % colors.size

        val newRoommate = Roommate(
            id = generatedRoommateId,
            name = name,
            initials = if (initials.isNotEmpty()) initials else "RM",
            avatarBgColor = colors[randomIndex],
            avatarTextColor = textColors[randomIndex],
            totalPaid = 0.0,
            balance = 0.0
        )

        db.collection("apartments").document(aptId)
            .collection("roommates").document(generatedRoommateId)
            .set(newRoommate)
            .addOnFailureListener { e ->
                _error.value = "Failed to add roommate: ${e.localizedMessage}"
            }
    }

    fun removeRoommate(roommateId: String) {
        val aptId = _activeApartmentId.value ?: return
        db.collection("apartments").document(aptId)
            .collection("roommates").document(roommateId)
            .delete()
            .addOnFailureListener { e ->
                _error.value = "Failed to remove roommate: ${e.localizedMessage}"
            }
    }

    // Proper Settlement Integration
    fun performSettlement(fromId: String, toId: String, amount: Double) {
        val aptId = _activeApartmentId.value ?: return
        val generatedSettlementId = db.collection("apartments").document(aptId).collection("settlements").document().id

        val newSettlement = Settlement(
            id = generatedSettlementId,
            fromId = fromId,
            toId = toId,
            amount = amount,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )

        db.collection("apartments").document(aptId)
            .collection("settlements").document(generatedSettlementId)
            .set(newSettlement)
            .addOnSuccessListener {
                repositoryScope.launch {
                    val payerName = _rawRoommates.value.find { it.id == fromId }?.name ?: "Someone"
                    val currentCurrency = preferenceManager?.currency?.firstOrNull() ?: "USD"
                    val amountStr = com.example.util.CurrencyFormatter.format(amount, currentCurrency)
                    val localizedTitle = appContext?.getString(R.string.notification_settlement_received_title) ?: "Settlement Received"
                    val localizedMsg = appContext?.getString(R.string.notification_settlement_received_msg, payerName, amountStr)
                        ?: "$payerName recorded a settlement of $amountStr to you"
                    addNotification(
                        recipientId = toId,
                        title = localizedTitle,
                        message = localizedMsg
                    )
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to save settlement: ${e.localizedMessage}"
            }
    }

    fun addNotification(recipientId: String, title: String, message: String) {
        val aptId = _activeApartmentId.value ?: return
        val docRef = db.collection("apartments").document(aptId)
            .collection("notifications").document()
        val id = docRef.id
        val newNotification = Notification(
            id = id,
            recipientId = recipientId,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            read = false
        )

        // TODO: Future push-notification phase (Cloud Function + FCM token storage)
        // This function is structured to act as a single trigger point where FCM/push payload can be dispatched
        // to the recipient's registered device tokens without needing to modify screen logic.

        docRef.set(newNotification)
            .addOnFailureListener { e ->
                Log.e("ApartmentRepository", "Failed to write notification: ${e.localizedMessage}")
            }
    }

    fun markNotificationsAsRead() {
        val aptId = _activeApartmentId.value ?: return
        val currentUid = _currentUserId.value
        val unreadNotifs = _rawNotifications.value.filter { it.recipientId == currentUid && !it.read }
        if (unreadNotifs.isEmpty()) return

        val batch = db.batch()
        unreadNotifs.forEach { notif ->
            val docRef = db.collection("apartments").document(aptId)
                .collection("notifications").document(notif.id)
            batch.update(docRef, "read", true)
        }
        batch.commit().addOnFailureListener { e ->
            Log.e("ApartmentRepository", "Failed to mark notifications read: ${e.localizedMessage}")
        }
    }

    fun updateProfile(newDisplayName: String, newUpiId: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return onComplete(false, "No user signed in")
        val uid = user.uid
        val aptId = _activeApartmentId.value

        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
            displayName = newDisplayName
        }
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (aptId != null) {
                        db.collection("apartments").document(aptId)
                            .collection("roommates").document(uid)
                            .update("name", newDisplayName, "upiId", newUpiId)
                            .addOnSuccessListener {
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                onComplete(true, "Auth updated, but Firestore failed: ${e.localizedMessage}")
                            }
                    } else {
                        onComplete(true, null)
                    }
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Failed to update auth profile")
                }
            }
    }

    fun updateApartmentName(newName: String, onComplete: (Boolean, String?) -> Unit) {
        val id = _activeApartmentId.value ?: return onComplete(false, "No active apartment")
        db.collection("apartments").document(id)
            .update("name", newName)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    private fun generateInviteCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // uppercase, no 0/O, 1/I
        return (1..6).map { chars.random() }.joinToString("")
    }

    companion object {
        @Volatile
        private var instance: ApartmentRepository? = null
        
        fun getInstance(): ApartmentRepository {
            return instance ?: synchronized(this) {
                instance ?: ApartmentRepository().also { instance = it }
            }
        }
    }
}
