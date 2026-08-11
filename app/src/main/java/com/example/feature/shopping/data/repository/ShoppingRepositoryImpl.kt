package com.example.feature.shopping.data.repository

import com.example.feature.shopping.data.dto.ShoppingItemDto
import com.example.feature.shopping.data.dto.ShoppingListDto
import com.example.feature.shopping.data.mapper.toDomain
import com.example.feature.shopping.data.mapper.toDto
import com.example.feature.shopping.domain.model.ShoppingItem
import com.example.feature.shopping.domain.model.ShoppingList
import com.example.feature.shopping.domain.repository.ShoppingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ShoppingRepository {

    private fun getShoppingListsRef(apartmentId: String) =
        firestore.collection("apartments").document(apartmentId).collection("shoppingLists")

    private fun getShoppingItemsRef(apartmentId: String, listId: String) =
        getShoppingListsRef(apartmentId).document(listId).collection("items")

    override fun getShoppingLists(apartmentId: String): Flow<List<ShoppingList>> = callbackFlow {
        val listener = getShoppingListsRef(apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lists = snapshot?.documents?.mapNotNull {
                    it.toObject(ShoppingListDto::class.java)?.copy(id = it.id)?.toDomain()
                } ?: emptyList()
                trySend(lists)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getShoppingList(apartmentId: String, listId: String): ShoppingList? {
        val snapshot = getShoppingListsRef(apartmentId).document(listId).get().await()
        return snapshot.toObject(ShoppingListDto::class.java)?.copy(id = snapshot.id)?.toDomain()
    }

    override suspend fun createShoppingList(shoppingList: ShoppingList): String {
        val ref = getShoppingListsRef(shoppingList.apartmentId).document()
        val dto = shoppingList.toDto().copy(id = ref.id)
        ref.set(dto).await()
        return ref.id
    }

    override suspend fun updateShoppingList(shoppingList: ShoppingList) {
        val dto = shoppingList.toDto()
        getShoppingListsRef(shoppingList.apartmentId).document(shoppingList.id).set(dto).await()
    }

    override suspend fun deleteShoppingList(apartmentId: String, listId: String) {
        getShoppingListsRef(apartmentId).document(listId).delete().await()
    }

    override fun getShoppingItems(apartmentId: String, listId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val listener = getShoppingItemsRef(apartmentId, listId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull {
                    it.toObject(ShoppingItemDto::class.java)?.copy(id = it.id)?.toDomain()
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getShoppingItem(apartmentId: String, listId: String, itemId: String): ShoppingItem? {
        val snapshot = getShoppingItemsRef(apartmentId, listId).document(itemId).get().await()
        return snapshot.toObject(ShoppingItemDto::class.java)?.copy(id = snapshot.id)?.toDomain()
    }

    override suspend fun addShoppingItem(apartmentId: String, item: ShoppingItem): String {
        val ref = getShoppingItemsRef(apartmentId, item.listId).document()
        val dto = item.toDto().copy(id = ref.id)
        ref.set(dto).await()
        return ref.id
    }

    override suspend fun updateShoppingItem(apartmentId: String, item: ShoppingItem) {
        val dto = item.toDto()
        getShoppingItemsRef(apartmentId, item.listId).document(item.id).set(dto).await()
    }

    override suspend fun deleteShoppingItem(apartmentId: String, listId: String, itemId: String) {
        getShoppingItemsRef(apartmentId, listId).document(itemId).delete().await()
    }
}
