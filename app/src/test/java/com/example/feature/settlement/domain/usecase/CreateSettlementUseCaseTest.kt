package com.example.feature.settlement.domain.usecase

import com.example.core.util.Resource
import com.example.feature.settlement.domain.model.Settlement
import com.example.feature.settlement.domain.repository.SettlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.feature.settlement.domain.model.SettlementReceipt

class CreateSettlementUseCaseTest {

    private lateinit var useCase: CreateSettlementUseCase
    private lateinit var fakeRepository: FakeSettlementRepository

    @Before
    fun setup() {
        fakeRepository = FakeSettlementRepository()
        useCase = CreateSettlementUseCase(fakeRepository)
    }

    @Test
    fun `Create settlement with zero amount returns error`() = runBlocking {
        val settlement = Settlement(
            debtorId = "user1",
            creditorId = "user2",
            amount = 0.0
        )
        val result = useCase(settlement)
        assertTrue(result is Resource.Error)
    }

    @Test
    fun `Create settlement with negative amount returns error`() = runBlocking {
        val settlement = Settlement(
            debtorId = "user1",
            creditorId = "user2",
            amount = -100.0
        )
        val result = useCase(settlement)
        assertTrue(result is Resource.Error)
    }

    @Test
    fun `Create settlement same debtor and creditor returns error`() = runBlocking {
        val settlement = Settlement(
            debtorId = "user1",
            creditorId = "user1",
            amount = 100.0
        )
        val result = useCase(settlement)
        assertTrue(result is Resource.Error)
    }

    @Test
    fun `Create settlement valid returns success`() = runBlocking {
        val settlement = Settlement(
            debtorId = "user1",
            creditorId = "user2",
            amount = 100.0
        )
        val result = useCase(settlement)
        assertTrue(result is Resource.Error == false)
        assertTrue(result is Resource.Success)
        assertTrue(fakeRepository.settlements.isNotEmpty())
    }
}

class FakeSettlementRepository : SettlementRepository {
    val settlements = mutableListOf<Settlement>()

    override fun getSettlements(apartmentId: String): Flow<Resource<List<Settlement>>> {
        return flowOf(Resource.Success(settlements))
    }

    override fun getSettlement(settlementId: String): Flow<Resource<Settlement>> {
        return flowOf(Resource.Success(settlements.first { it.settlementId == settlementId }))
    }

    override fun getSettlementsByUser(apartmentId: String, userId: String): Flow<Resource<List<Settlement>>> {
        return flowOf(Resource.Success(settlements.filter { it.debtorId == userId || it.creditorId == userId }))
    }

    override suspend fun createSettlement(settlement: Settlement): Resource<Unit> {
        settlements.add(settlement)
        return Resource.Success(Unit)
    }

    override suspend fun updateSettlement(settlement: Settlement): Resource<Unit> {
        val idx = settlements.indexOfFirst { it.settlementId == settlement.settlementId }
        if (idx != -1) {
            settlements[idx] = settlement
            return Resource.Success(Unit)
        }
        return Resource.Error("Not found")
    }

    override suspend fun confirmSettlement(settlementId: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun rejectSettlement(settlementId: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun deleteSettlement(settlementId: String): Resource<Unit> {
        settlements.removeIf { it.settlementId == settlementId }
        return Resource.Success(Unit)
    }

    override suspend fun uploadReceipt(receipt: SettlementReceipt, fileBytes: ByteArray): Resource<String> {
        return Resource.Success("url")
    }
}
