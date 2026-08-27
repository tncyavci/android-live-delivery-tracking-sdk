package com.tuncayavci.tracking.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TelemetryDaoTest {
    private lateinit var database: TrackingDatabase
    private lateinit var dao: TelemetryDao

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), TrackingDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.telemetryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `inserted rows default to unsynced and are returned by getUnsynced`() =
        runTest {
            dao.insert(entity(id = 0, timestamp = 1_000))

            val unsynced = dao.getUnsynced()

            assertEquals(1, unsynced.size)
            assertTrue(unsynced.single().isSynced.not())
        }

    @Test
    fun `markSynced removes rows from the unsynced set`() =
        runTest {
            val id = dao.insert(entity(id = 0, timestamp = 1_000))

            dao.markSynced(listOf(id))

            assertEquals(0, dao.getUnsynced().size)
            assertEquals(0, dao.countUnsynced())
        }

    @Test
    fun `getUnsynced orders oldest ping first`() =
        runTest {
            dao.insert(entity(id = 0, timestamp = 3_000))
            dao.insert(entity(id = 0, timestamp = 1_000))
            dao.insert(entity(id = 0, timestamp = 2_000))

            val unsynced = dao.getUnsynced()

            assertEquals(listOf(1_000L, 2_000L, 3_000L), unsynced.map { it.timestamp })
        }

    private fun entity(
        id: Long,
        timestamp: Long,
    ) = CourierLocationEntity(
        id = id,
        orderId = "ORD-1",
        courierId = "CR-1",
        latitude = 41.0,
        longitude = 29.0,
        bearing = 90f,
        speedKmh = 20f,
        timestamp = timestamp,
    )
}
