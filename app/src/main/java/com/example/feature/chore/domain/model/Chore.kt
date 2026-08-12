package com.example.feature.chore.domain.model

enum class ChoreCategory {
    CLEANING, KITCHEN, BATHROOM, LAUNDRY, GARBAGE, MAINTENANCE, SHOPPING, PLANTS, PETS, OTHER;

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

enum class ChorePriority {
    LOW, NORMAL, HIGH, URGENT;

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

enum class ChoreStatus {
    PENDING, IN_PROGRESS, COMPLETED, PENDING_VERIFICATION, VERIFIED, SKIPPED, CANCELLED
}

enum class ChoreRecurrence {
    NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY;

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

data class Chore(
    val id: String = "",
    val apartmentId: String = "",
    val name: String = "",
    val description: String = "",
    val category: ChoreCategory = ChoreCategory.OTHER,
    val priority: ChorePriority = ChorePriority.NORMAL,
    val assignedTo: String? = null,
    val dueDate: Long = 0L,
    val recurrence: ChoreRecurrence = ChoreRecurrence.NONE,
    val status: ChoreStatus = ChoreStatus.PENDING,
    val points: Int = 1,
    val rotationEnabled: Boolean = false,
    val rotationOrder: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedBy: String? = null,
    val notes: String = "",
    val photoUrl: String? = null
) {
    val isOverdue: Boolean
        get() = (status == ChoreStatus.PENDING || status == ChoreStatus.IN_PROGRESS) &&
                dueDate > 0 && dueDate < System.currentTimeMillis()
}

data class FairnessSummary(
    val userPoints: Map<String, Int>,
    val userCompletedCount: Map<String, Int>,
    val userPendingCount: Map<String, Int>,
    val userOverdueCount: Map<String, Int>
)
