package com.example.feature.shopping.data.mapper

import com.example.feature.shopping.data.dto.ShoppingItemDto
import com.example.feature.shopping.data.dto.ShoppingListDto
import com.example.feature.shopping.domain.model.ShoppingItem
import com.example.feature.shopping.domain.model.ShoppingItemPriority
import com.example.feature.shopping.domain.model.ShoppingItemStatus
import com.example.feature.shopping.domain.model.ShoppingList

fun ShoppingListDto.toDomain(): ShoppingList = ShoppingList(
    id = id,
    apartmentId = apartmentId,
    name = name,
    description = description,
    icon = icon,
    color = color,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    itemCount = itemCount,
    purchasedCount = purchasedCount
)

fun ShoppingList.toDto(): ShoppingListDto = ShoppingListDto(
    id = id,
    apartmentId = apartmentId,
    name = name,
    description = description,
    icon = icon,
    color = color,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    itemCount = itemCount,
    purchasedCount = purchasedCount
)

fun ShoppingItemDto.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    notes = notes,
    priority = runCatching { ShoppingItemPriority.valueOf(priority) }.getOrDefault(ShoppingItemPriority.NORMAL),
    addedBy = addedBy,
    assignedTo = assignedTo,
    estimatedPrice = estimatedPrice,
    purchasedPrice = purchasedPrice,
    status = runCatching { ShoppingItemStatus.valueOf(status) }.getOrDefault(ShoppingItemStatus.NEEDED),
    linkedExpenseId = linkedExpenseId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    purchasedAt = purchasedAt,
    purchasedBy = purchasedBy
)

fun ShoppingItem.toDto(): ShoppingItemDto = ShoppingItemDto(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    notes = notes,
    priority = priority.name,
    addedBy = addedBy,
    assignedTo = assignedTo,
    estimatedPrice = estimatedPrice,
    purchasedPrice = purchasedPrice,
    status = status.name,
    linkedExpenseId = linkedExpenseId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    purchasedAt = purchasedAt,
    purchasedBy = purchasedBy
)
