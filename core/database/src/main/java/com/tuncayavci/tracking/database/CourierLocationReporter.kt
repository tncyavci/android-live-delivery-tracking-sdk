package com.tuncayavci.tracking.database

import com.tuncayavci.tracking.network.CourierTelemetryMessage
import com.tuncayavci.tracking.network.TelemetryWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first entry point for the courier app to report its own position.
 *
 * Tries the live WebSocket first; on failure (or when nothing is connected, e.g. mid-tunnel) the
 * ping is buffered into Room instead of being dropped, and [TelemetrySyncWorker] flushes it via
 * REST once connectivity returns. This is what makes the SDK "zero data loss".
 */
@Singleton
class CourierLocationReporter
    @Inject
    constructor(
        private val webSocketClient: TelemetryWebSocketClient,
        private val telemetryDao: TelemetryDao,
    ) {
        suspend fun reportLocation(message: CourierTelemetryMessage) {
            val delivered = runCatching { webSocketClient.send(message) }.getOrDefault(false)
            if (!delivered) {
                telemetryDao.insert(message.toEntity())
            }
        }
    }

internal fun CourierTelemetryMessage.toEntity(): CourierLocationEntity =
    CourierLocationEntity(
        orderId = orderId,
        courierId = courierId,
        latitude = latitude,
        longitude = longitude,
        bearing = bearing,
        speedKmh = speedKmh,
        timestamp = timestamp,
    )

internal fun CourierLocationEntity.toMessage(): CourierTelemetryMessage =
    CourierTelemetryMessage(
        orderId = orderId,
        courierId = courierId,
        latitude = latitude,
        longitude = longitude,
        bearing = bearing,
        speedKmh = speedKmh,
        timestamp = timestamp,
    )
