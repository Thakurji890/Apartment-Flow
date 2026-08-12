package com.example.feature.chore.domain.usecase

import com.example.feature.chore.domain.model.Chore
import com.example.feature.chore.domain.model.ChoreRecurrence
import com.example.feature.chore.domain.model.ChoreStatus
import com.example.feature.chore.domain.model.FairnessSummary
import com.example.feature.chore.domain.repository.ChoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class GetChoresUseCase @Inject constructor(
    private val repository: ChoreRepository
) {
    operator fun invoke(apartmentId: String): Flow<List<Chore>> {
        return repository.getChores(apartmentId)
    }
}

class SaveChoreUseCase @Inject constructor(
    private val repository: ChoreRepository
) {
    suspend operator fun invoke(chore: Chore) {
        val choreToSave = if (chore.id.isBlank()) {
            chore.copy(id = UUID.randomUUID().toString())
        } else {
            chore
        }
        repository.saveChore(choreToSave)
    }
}

class DeleteChoreUseCase @Inject constructor(
    private val repository: ChoreRepository
) {
    suspend operator fun invoke(apartmentId: String, choreId: String) {
        repository.deleteChore(apartmentId, choreId)
    }
}

class CompleteChoreUseCase @Inject constructor(
    private val repository: ChoreRepository
) {
    suspend operator fun invoke(
        chore: Chore,
        completedByUserId: String,
        notes: String = "",
        photoUrl: String? = null
    ) {
        val currentTime = System.currentTimeMillis()
        
        // 1. Mark current chore as completed
        val completedChore = chore.copy(
            status = ChoreStatus.COMPLETED,
            completedAt = currentTime,
            completedBy = completedByUserId,
            notes = notes.ifBlank { chore.notes },
            photoUrl = photoUrl ?: chore.photoUrl
        )
        repository.saveChore(completedChore)
        
        // 2. Generate next occurrence if recurring
        if (chore.recurrence != ChoreRecurrence.NONE && chore.dueDate > 0) {
            val nextDueDate = calculateNextDueDate(chore.dueDate, chore.recurrence)
            var nextAssignedTo = chore.assignedTo
            
            // Handle Rotation
            if (chore.rotationEnabled && chore.rotationOrder.isNotEmpty() && nextAssignedTo != null) {
                val currentIndex = chore.rotationOrder.indexOf(nextAssignedTo)
                if (currentIndex != -1) {
                    val nextIndex = (currentIndex + 1) % chore.rotationOrder.size
                    nextAssignedTo = chore.rotationOrder[nextIndex]
                }
            }
            
            val nextChore = chore.copy(
                id = UUID.randomUUID().toString(),
                status = ChoreStatus.PENDING,
                dueDate = nextDueDate,
                assignedTo = nextAssignedTo,
                createdAt = currentTime,
                completedAt = null,
                completedBy = null,
                notes = "",
                photoUrl = null
            )
            repository.saveChore(nextChore)
        }
    }
    
    private fun calculateNextDueDate(currentDue: Long, recurrence: ChoreRecurrence): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDue }
        when (recurrence) {
            ChoreRecurrence.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            ChoreRecurrence.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            ChoreRecurrence.BIWEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            ChoreRecurrence.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            ChoreRecurrence.NONE -> {}
        }
        return calendar.timeInMillis
    }
}

class GetFairnessSummaryUseCase @Inject constructor(
    private val repository: ChoreRepository
) {
    operator fun invoke(apartmentId: String): Flow<FairnessSummary> {
        return repository.getChores(apartmentId).map { chores ->
            val userPoints = mutableMapOf<String, Int>()
            val userCompletedCount = mutableMapOf<String, Int>()
            val userPendingCount = mutableMapOf<String, Int>()
            val userOverdueCount = mutableMapOf<String, Int>()

            chores.forEach { chore ->
                val assignee = chore.assignedTo
                if (chore.status == ChoreStatus.COMPLETED && chore.completedBy != null) {
                    val completedBy = chore.completedBy
                    userPoints[completedBy] = (userPoints[completedBy] ?: 0) + chore.points
                    userCompletedCount[completedBy] = (userCompletedCount[completedBy] ?: 0) + 1
                } else if (assignee != null) {
                    if (chore.isOverdue) {
                        userOverdueCount[assignee] = (userOverdueCount[assignee] ?: 0) + 1
                    } else if (chore.status == ChoreStatus.PENDING || chore.status == ChoreStatus.IN_PROGRESS) {
                        userPendingCount[assignee] = (userPendingCount[assignee] ?: 0) + 1
                    }
                }
            }

            FairnessSummary(
                userPoints = userPoints,
                userCompletedCount = userCompletedCount,
                userPendingCount = userPendingCount,
                userOverdueCount = userOverdueCount
            )
        }
    }
}
