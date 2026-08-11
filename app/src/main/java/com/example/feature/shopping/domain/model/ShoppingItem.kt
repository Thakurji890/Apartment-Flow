package com.example.feature.shopping.domain.model

enum class ShoppingItemStatus {
    NEEDED, ASSIGNED, IN_CART, PURCHASED, CANCELLED
}

enum class ShoppingItemPriority {
    LOW, NORMAL, HIGH, URGENT
}

data class ShoppingItem(
    val id: String = "",
    val listId: String = "",
    val name: String = "",
    val quantity: Double? = null,
    val unit: String = "",
    val category: String = "Other",
    val notes: String = "",
    val priority: ShoppingItemPriority = ShoppingItemPriority.NORMAL,
    val addedBy: String = "",
    val assignedTo: String? = null,
    val estimatedPrice: Double? = null,
    val purchasedPrice: Double? = null,
    val status: ShoppingItemStatus = ShoppingItemStatus.NEEDED,
    val linkedExpenseId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val purchasedAt: Long? = null,
    val purchasedBy: String? = null
)
