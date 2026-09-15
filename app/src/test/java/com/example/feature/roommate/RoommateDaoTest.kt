package com.example.feature.roommate

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.feature.roommate.data.local.dao.RoommateDao
import com.example.feature.roommate.data.local.entity.RoommateEntity
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
class RoommateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var roommateDao: RoommateDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        roommateDao = database.roommateDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryRoommate() = runBlocking {
        val roommate = RoommateEntity(
            id = "rm_1",
            name = "Aniket",
            email = "aniket@apartmentflow.app",
            balanceStatus = "Settled",
            balanceAmount = 0.0,
            apartmentId = "APT402",
            isAdmin = true
        )

        roommateDao.insertRoommate(roommate)

        val retrieved = roommateDao.getRoommateById("rm_1")
        assertNotNull(retrieved)
        assertEquals("Aniket", retrieved?.name)
        assertEquals("aniket@apartmentflow.app", retrieved?.email)
        assertEquals("Settled", retrieved?.balanceStatus)
        assertEquals(true, retrieved?.isAdmin)
    }

    @Test
    fun updateBalanceStatus() = runBlocking {
        val roommate = RoommateEntity(
            id = "rm_2",
            name = "Amit",
            email = "amit@apartmentflow.app",
            balanceStatus = "Settled",
            balanceAmount = 0.0,
            apartmentId = "APT402"
        )
        roommateDao.insertRoommate(roommate)

        roommateDao.updateBalanceStatus("rm_2", "Owes ₹150.00", -150.0)

        val updated = roommateDao.getRoommateById("rm_2")
        assertEquals("Owes ₹150.00", updated?.balanceStatus)
        assertEquals(-150.0, updated?.balanceAmount ?: 0.0, 0.01)
    }

    @Test
    fun insertMultipleAndGetAllRoommates() = runBlocking {
        val list = listOf(
            RoommateEntity("1", "Aniket", "aniket@flat.com", "Owed ₹200.00", 200.0),
            RoommateEntity("2", "Amit", "amit@flat.com", "Owes ₹100.00", -100.0),
            RoommateEntity("3", "Rahul", "rahul@flat.com", "Settled", 0.0)
        )
        roommateDao.insertRoommates(list)

        val all = roommateDao.getAllRoommates().first()
        assertEquals(3, all.size)
    }

    @Test
    fun deleteRoommate() = runBlocking {
        val roommate = RoommateEntity("4", "Debang", "debang@flat.com")
        roommateDao.insertRoommate(roommate)
        assertNotNull(roommateDao.getRoommateById("4"))

        roommateDao.deleteRoommateById("4")
        assertNull(roommateDao.getRoommateById("4"))
    }
}
