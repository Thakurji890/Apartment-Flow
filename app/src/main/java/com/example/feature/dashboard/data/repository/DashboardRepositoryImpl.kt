package com.example.feature.dashboard.data.repository

import com.example.core.util.Resource
import com.example.feature.dashboard.domain.repository.DashboardRepository
import com.example.feature.dashboard.domain.repository.DashboardSearchResult
import com.example.feature.expense.domain.model.Expense
import com.example.feature.settlement.domain.model.Settlement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DashboardRepository {

    override fun getRecentExpenses(apartmentId: String, limit: Int): Flow<Resource<List<Expense>>> = callbackFlow {
        val listener = firestore.collection("expenses")
            .whereEqualTo("apartmentId", apartmentId)
            .whereEqualTo("deleted", false)
            .orderBy("expenseDate", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load recent expenses"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                    trySend(Resource.Success(expenses))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getExpensesForMonth(
        apartmentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Resource<List<Expense>>> = callbackFlow {
        val listener = firestore.collection("expenses")
            .whereEqualTo("apartmentId", apartmentId)
            .whereEqualTo("deleted", false)
            .whereGreaterThanOrEqualTo("expenseDate", startTimestamp)
            .whereLessThanOrEqualTo("expenseDate", endTimestamp)
            .orderBy("expenseDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load expenses for month"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                    trySend(Resource.Success(expenses))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getSettlementsForMonth(
        apartmentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<Resource<List<Settlement>>> = callbackFlow {
        val listener = firestore.collection("settlements")
            .whereEqualTo("apartmentId", apartmentId)
            .whereGreaterThanOrEqualTo("paymentDate", startTimestamp)
            .whereLessThanOrEqualTo("paymentDate", endTimestamp)
            .orderBy("paymentDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load settlements for month"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val settlements = snapshot.documents.mapNotNull { it.toObject(Settlement::class.java) }
                    trySend(Resource.Success(settlements))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun searchDashboard(apartmentId: String, query: String): Flow<Resource<DashboardSearchResult>> = callbackFlow {
        if (query.isBlank()) {
            trySend(Resource.Success(DashboardSearchResult()))
            close()
            return@callbackFlow
        }
        
        // Firestore doesn't have native full-text search. 
        // For simple dashboard search, we can fetch recent active items and filter locally, 
        // or just use basic equality if needed. Here we fetch the last 100 expenses and filter locally 
        // to avoid huge scans and expensive third-party integrations like Algolia for a simple app.
        
        val listener = firestore.collection("expenses")
            .whereEqualTo("apartmentId", apartmentId)
            .whereEqualTo("deleted", false)
            .orderBy("expenseDate", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allExpenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                    val q = query.lowercase()
                    val filteredExpenses = allExpenses.filter { 
                        it.title.lowercase().contains(q) || 
                        it.description.lowercase().contains(q) ||
                        it.paidBy.lowercase().contains(q)
                    }
                    trySend(Resource.Success(DashboardSearchResult(expenses = filteredExpenses)))
                }
            }
        awaitClose { listener.remove() }
    }
}
