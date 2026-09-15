package com.example.feature.roommate

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.feature.roommate.data.local.dao.RoommateDao
import com.example.feature.roommate.data.local.entity.RoommateEntity
import com.example.feature.roommate.data.repository.RoommateLocalRepositoryImpl
import com.example.feature.roommate.domain.repository.RoommateLocalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoommateLocalRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var roommateDao: RoommateDao
    private lateinit var repository: RoommateLocalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        roommateDao = database.roommateDao()
        repository = RoommateLocalRepositoryImpl(roommateDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryRoommateViaRepository() = runBlocking {
        val entity = RoommateEntity(
            id = "rm_test_1",
            name = "Aniket",
            email = "aniket@flat.com",
            balanceStatus = "Owed ₹2,471.20",
            balanceAmount = 2471.20,
            apartmentId = "APT402",
            isAdmin = true
        )

        repository.insertRoommate(entity)

        val retrieved = repository.getRoommateById("rm_test_1")
        assertNotNull(retrieved)
        assertEquals("Aniket", retrieved?.name)
        assertEquals("Owed ₹2,471.20", retrieved?.balanceStatus)
        assertEquals(2471.20, retrieved?.balanceAmount ?: 0.0, 0.01)
        assertEquals(true, retrieved?.isAdmin)
    }

    @Test
    fun updateBalanceStatusViaRepository() = runBlocking {
        val entity = RoommateEntity(
            id = "rm_test_2",
            name = "Amit",
            email = "amit@flat.com",
            balanceStatus = "Settled",
            balanceAmount = 0.0,
            apartmentId = "APT402"
        )
        repository.insertRoommate(entity)

        repository.updateBalanceStatus("rm_test_2", "Owes ₹650.00", -650.0)

        val updated = repository.getRoommateById("rm_test_2")
        assertNotNull(updated)
        assertEquals("Owes ₹650.00", updated?.balanceStatus)
        assertEquals(-650.0, updated?.balanceAmount ?: 0.0, 0.01)
    }

    @Test
    fun batchInsertAndGetAllRoommatesViaRepository() = runBlocking {
        val entities = listOf(
            RoommateEntity("1", "Aniket", "aniket@flat.com", "Owed ₹2471.20", 2471.20),
            RoommateEntity("2", "Amit", "amit@flat.com", "Owes ₹650.00", -650.0),
            RoommateEntity("3", "Rahul", "rahul@flat.com", "Owed ₹113.80", 113.80),
            RoommateEntity("4", "Debang", "debang@flat.com", "Owes ₹68.80", -68.80),
            RoommateEntity("5", "Akshay", "akshay@flat.com", "Owes ₹1866.20", -1866.20)
        )

        repository.insertRoommates(entities)

        val allRoommates = repository.getAllRoommates().first()
        assertEquals(5, allRoommates.size)

        val count = repository.getRoommatesCount()
        assertEquals(5, count)
    }

    @Test
    fun deleteRoommateViaRepository() = runBlocking {
        val entity = RoommateEntity(
            id = "rm_to_delete",
            name = "Temp User",
            email = "temp@flat.com"
        )
        repository.insertRoommate(entity)
        assertNotNull(repository.getRoommateById("rm_to_delete"))

        repository.deleteRoommateById("rm_to_delete")
        assertNull(repository.getRoommateById("rm_to_delete"))
    }
}
