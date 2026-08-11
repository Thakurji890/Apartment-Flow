package com.example.feature.shopping.data.dto

data class ShoppingItemDto(
    val id: String = "",
    val listId: String = "",
    val name: String = "",
    val quantity: Double? = null,
    val unit: String = "",
    val category: String = "Other",
    val notes: String = "",
    val priority: String = "NORMAL",
    val addedBy: String = "",
    val assignedTo: String? = null,
    val estimatedPrice: Double? = null,
    val purchasedPrice: Double? = null,
    val status: String = "NEEDED",
    val linkedExpenseId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val purchasedAt: Long? = null,
    val purchasedBy: String? = null
)
