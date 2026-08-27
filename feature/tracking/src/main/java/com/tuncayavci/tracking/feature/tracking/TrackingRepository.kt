package com.tuncayavci.tracking.feature.tracking

import com.tuncayavci.tracking.location.CourierLocation
import com.tuncayavci.tracking.network.CourierTelemetryMessage
import com.tuncayavci.tracking.network.TelemetryWebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Maps the wire-level [CourierTelemetryMessage] stream onto the domain-level [CourierLocation]
 * type the map-rendering layer (core:location-abstraction) already understands.
 */
class TrackingRepository
    @Inject
    constructor(
        private val webSocketClient: TelemetryWebSocketClient,
    ) {
        fun observeCourierLocation(
            orderId: String,
            endpointUrl: String,
        ): Flow<CourierLocation> = webSocketClient.observeTelemetry(orderId, endpointUrl).map { it.toDomain() }
    }

private fun CourierTelemetryMessage.toDomain(): CourierLocation =
    CourierLocation(
        latitude = latitude,
        longitude = longitude,
        bearing = bearing,
        speedKmh = speedKmh,
        timestamp = timestamp,
    )
