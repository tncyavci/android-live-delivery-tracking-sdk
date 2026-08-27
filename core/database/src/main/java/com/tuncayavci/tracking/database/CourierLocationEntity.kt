package com.tuncayavci.tracking.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A courier location ping that could not be delivered live over the WebSocket (tunnel/cellular
 * dead zone) and is buffered here until [TelemetrySyncWorker] can flush it via REST.
 */
@Entity(tableName = "pending_courier_telemetry")
data class CourierLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val courierId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val speedKmh: Float,
    val timestamp: Long,
    val isSynced: Boolean = false,
)
