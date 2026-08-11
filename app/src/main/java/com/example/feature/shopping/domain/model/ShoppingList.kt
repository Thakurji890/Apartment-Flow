package com.example.feature.shopping.domain.model

data class ShoppingList(
    val id: String = "",
    val apartmentId: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "ShoppingCart",
    val color: String = "Blue",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0,
    val purchasedCount: Int = 0
)
