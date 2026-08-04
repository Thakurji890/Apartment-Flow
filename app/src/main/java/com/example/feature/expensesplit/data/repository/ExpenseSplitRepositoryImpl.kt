package com.example.feature.expensesplit.data.repository

import com.example.core.util.Resource
import com.example.feature.expensesplit.domain.model.Debt
import com.example.feature.expensesplit.domain.model.MemberBalance
import com.example.feature.expensesplit.domain.model.Split
import com.example.feature.expensesplit.domain.repository.ExpenseSplitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ExpenseSplitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseSplitRepository {

    override fun getSplitsForExpense(expenseId: String): Flow<Resource<List<Split>>> = callbackFlow {
        val listener = firestore.collection("expenseSplits")
            .whereEqualTo("expenseId", expenseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // map logic
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getMemberBalances(apartmentId: String): Flow<Resource<List<MemberBalance>>> = callbackFlow {
        val listener = firestore.collection("memberBalances")
            .whereEqualTo("apartmentId", apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // map logic
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getDebtsForApartment(apartmentId: String): Flow<Resource<List<Debt>>> = callbackFlow {
        val listener = firestore.collection("debts")
            .whereEqualTo("apartmentId", apartmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // map logic
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveExpenseWithSplits(
        expenseId: String,
        apartmentId: String,
        splits: List<Split>,
        debts: List<Debt>
    ): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // Implementation for updating balances and debts
                // Here we would fetch current balances, compute new ones based on the old splits vs new splits
                // But for simplicity, we assume we just write them for now or calculate on the fly
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun deleteExpenseSplits(expenseId: String, apartmentId: String): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // Reverse debts and balances
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun syncBalances(apartmentId: String): Resource<Unit> {
        return Resource.Success(Unit)
    }
}
