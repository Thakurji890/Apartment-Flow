package com.example.data

import androidx.compose.ui.graphics.Color

data class Roommate(
    val id: String = "",
    val name: String = "",
    val initials: String = "",
    val isGuest: Boolean = false,
    val avatarBgColor: Long = 0L, // hex value representation for serialization/reconstruction
    val avatarTextColor: Long = 0L,
    val totalPaid: Double = 0.0,
    val balance: Double = 0.0 // positive means they are owed money, negative means they owe money
)

enum class BillCategory(val displayName: String, val bgColor: Long, val textColor: Long) {
    GROCERIES("Groceries", 0xFFE6DEFF, 0xFF1D1633),
    UTILITIES("Utilities", 0xFFD2E5D5, 0xFF00210E),
    RENT("Rent", 0xFFFFDBCF, 0xFF350B00),
    OTHERS("Others", 0xFFD1E4FF, 0xFF001D36)
}

data class Bill(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: BillCategory = BillCategory.GROCERIES,
    val date: String = "",
    val payerId: String = "", // Roommate ID who paid
    val description: String = "",
    val splitAmongIds: List<String> = emptyList()
)

data class Settlement(
    val id: String = "",
    val fromId: String = "",
    val toId: String = "",
    val amount: Double = 0.0,
    val date: String = ""
)

data class Debt(
    val fromId: String = "", // Who owes
    val toId: String = "",   // Who is owed
    val amount: Double = 0.0
)

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
)
