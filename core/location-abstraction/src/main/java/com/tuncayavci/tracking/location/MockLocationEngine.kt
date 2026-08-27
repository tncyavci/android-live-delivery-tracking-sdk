package com.tuncayavci.tracking.location

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Emits simulated [CourierLocation] ticks along a route, standing in for the real WebSocket
 * telemetry stream so the tracking screen's marker animation and MVI state transitions can be
 * exercised on an emulator or in a demo without a physical device or a live backend.
 */
object MockLocationEngine {
    fun simulateRoute(
        points: List<LatLngPoint>,
        tickIntervalMs: Long = 2_000,
        speedKmh: Float = 25f,
    ): Flow<CourierLocation> =
        flow {
            require(points.size >= 2) { "A route needs at least two points to simulate movement" }

            var timestamp = System.currentTimeMillis()
            for (index in 0 until points.size - 1) {
                val from = points[index]
                val to = points[index + 1]
                emit(
                    CourierLocation(
                        latitude = from.latitude,
                        longitude = from.longitude,
                        bearing = bearingBetween(from, to),
                        speedKmh = speedKmh,
                        timestamp = timestamp,
                    ),
                )
                delay(tickIntervalMs)
                timestamp += tickIntervalMs
            }

            val last = points.last()
            emit(
                CourierLocation(
                    latitude = last.latitude,
                    longitude = last.longitude,
                    bearing = bearingBetween(points[points.size - 2], last),
                    speedKmh = 0f,
                    timestamp = timestamp,
                ),
            )
        }

    private fun bearingBetween(
        from: LatLngPoint,
        to: LatLngPoint,
    ): Float {
        val deltaLat = to.latitude - from.latitude
        val deltaLng = to.longitude - from.longitude
        val radians = kotlin.math.atan2(deltaLng, deltaLat)
        val degrees = Math.toDegrees(radians).toFloat()
        return (degrees + 360) % 360
    }
}
