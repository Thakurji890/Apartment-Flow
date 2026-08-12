package com.example.feature.chore.data.mapper

import com.example.feature.chore.data.dto.ChoreDto
import com.example.feature.chore.domain.model.Chore
import com.example.feature.chore.domain.model.ChoreCategory
import com.example.feature.chore.domain.model.ChorePriority
import com.example.feature.chore.domain.model.ChoreRecurrence
import com.example.feature.chore.domain.model.ChoreStatus

fun ChoreDto.toDomain(): Chore {
    return Chore(
        id = id,
        apartmentId = apartmentId,
        name = name,
        description = description,
        category = runCatching { ChoreCategory.valueOf(category) }.getOrDefault(ChoreCategory.OTHER),
        priority = runCatching { ChorePriority.valueOf(priority) }.getOrDefault(ChorePriority.NORMAL),
        assignedTo = assignedTo,
        dueDate = dueDate,
        recurrence = runCatching { ChoreRecurrence.valueOf(recurrence) }.getOrDefault(ChoreRecurrence.NONE),
        status = runCatching { ChoreStatus.valueOf(status) }.getOrDefault(ChoreStatus.PENDING),
        points = points,
        rotationEnabled = rotationEnabled,
        rotationOrder = rotationOrder,
        createdBy = createdBy,
        createdAt = createdAt,
        completedAt = completedAt,
        completedBy = completedBy,
        notes = notes,
        photoUrl = photoUrl
    )
}

fun Chore.toDto(): ChoreDto {
    return ChoreDto(
        id = id,
        apartmentId = apartmentId,
        name = name,
        description = description,
        category = category.name,
        priority = priority.name,
        assignedTo = assignedTo,
        dueDate = dueDate,
        recurrence = recurrence.name,
        status = status.name,
        points = points,
        rotationEnabled = rotationEnabled,
        rotationOrder = rotationOrder,
        createdBy = createdBy,
        createdAt = createdAt,
        completedAt = completedAt,
        completedBy = completedBy,
        notes = notes,
        photoUrl = photoUrl
    )
}
