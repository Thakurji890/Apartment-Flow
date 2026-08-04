package com.example.feature.apartment.domain.model

data class Apartment(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val pinCode: String = "",
    val photoUrl: String? = null,
    val currency: String = "USD",
    val timezone: String = "UTC",
    val monthlyRentDueDate: Int = 1,
    val utilitiesIncluded: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val inviteCode: String = "",
    val isArchived: Boolean = false
)
