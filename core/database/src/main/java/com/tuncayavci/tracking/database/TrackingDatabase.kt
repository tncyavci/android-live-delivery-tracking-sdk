package com.tuncayavci.tracking.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CourierLocationEntity::class], version = 1, exportSchema = true)
abstract class TrackingDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
}
