package com.example.feature.chore.data.dto

data class ChoreDto(
    val id: String = "",
    val apartmentId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "OTHER",
    val priority: String = "NORMAL",
    val assignedTo: String? = null,
    val dueDate: Long = 0L,
    val recurrence: String = "NONE",
    val status: String = "PENDING",
    val points: Int = 1,
    val rotationEnabled: Boolean = false,
    val rotationOrder: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedBy: String? = null,
    val notes: String = "",
    val photoUrl: String? = null
)
