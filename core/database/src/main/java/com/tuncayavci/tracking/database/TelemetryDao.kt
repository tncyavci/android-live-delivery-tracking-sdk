package com.tuncayavci.tracking.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TelemetryDao {
    @Insert
    suspend fun insert(entity: CourierLocationEntity): Long

    @Query("SELECT * FROM pending_courier_telemetry WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<CourierLocationEntity>

    @Query("UPDATE pending_courier_telemetry SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM pending_courier_telemetry WHERE isSynced = 1 AND timestamp < :beforeTimestamp")
    suspend fun purgeSyncedOlderThan(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM pending_courier_telemetry WHERE isSynced = 0")
    suspend fun countUnsynced(): Int
}
