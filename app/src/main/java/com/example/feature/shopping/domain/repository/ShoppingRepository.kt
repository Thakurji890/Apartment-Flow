package com.example.feature.shopping.domain.repository

import com.example.feature.shopping.domain.model.ShoppingItem
import com.example.feature.shopping.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun getShoppingLists(apartmentId: String): Flow<List<ShoppingList>>
    suspend fun getShoppingList(apartmentId: String, listId: String): ShoppingList?
    suspend fun createShoppingList(shoppingList: ShoppingList): String
    suspend fun updateShoppingList(shoppingList: ShoppingList)
    suspend fun deleteShoppingList(apartmentId: String, listId: String)
    
    fun getShoppingItems(apartmentId: String, listId: String): Flow<List<ShoppingItem>>
    suspend fun getShoppingItem(apartmentId: String, listId: String, itemId: String): ShoppingItem?
    suspend fun addShoppingItem(apartmentId: String, item: ShoppingItem): String
    suspend fun updateShoppingItem(apartmentId: String, item: ShoppingItem)
    suspend fun deleteShoppingItem(apartmentId: String, listId: String, itemId: String)
}
