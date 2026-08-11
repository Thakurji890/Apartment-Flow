package com.example.feature.shopping.domain.usecase

import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.expense.domain.model.Expense
import com.example.feature.expense.domain.repository.ExpenseRepository
import com.example.feature.shopping.domain.model.ShoppingItemStatus
import com.example.feature.shopping.domain.repository.ShoppingRepository
import java.util.UUID
import javax.inject.Inject

class ConvertPurchaseToExpenseUseCase @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        apartmentId: String,
        listId: String,
        itemId: String,
        actualPrice: Double,
        notes: String = ""
    ): Result<Unit> {
        return try {
            val item = shoppingRepository.getShoppingItem(apartmentId, listId, itemId)
                ?: return Result.failure(Exception("Item not found"))
                
            val currentUser = authRepository.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))

            val expenseId = UUID.randomUUID().toString()

            // Create Expense
            val expense = Expense(
                expenseId = expenseId,
                apartmentId = apartmentId,
                title = "Shopping: ${item.name}",
                amount = actualPrice,
                paidBy = currentUser.uid,
                expenseDate = System.currentTimeMillis(),
                description = notes.ifEmpty { "Purchased from shared shopping list" },
                categoryId = "groceries"
            )
            
            val result = expenseRepository.insertExpense(expense)
            if (result is com.example.core.util.Resource.Error) {
                return Result.failure(Exception(result.message ?: "Failed to create expense"))
            }
            
            // Update Shopping Item
            val updatedItem = item.copy(
                status = ShoppingItemStatus.PURCHASED,
                purchasedPrice = actualPrice,
                purchasedBy = currentUser.uid,
                purchasedAt = System.currentTimeMillis(),
                linkedExpenseId = expenseId,
                updatedAt = System.currentTimeMillis()
            )
            
            shoppingRepository.updateShoppingItem(apartmentId, updatedItem)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
