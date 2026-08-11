package com.example.feature.shopping.data.dto

data class ShoppingListDto(
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
