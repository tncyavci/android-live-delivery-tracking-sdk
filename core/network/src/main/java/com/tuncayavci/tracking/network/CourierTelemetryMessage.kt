package com.tuncayavci.tracking.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format for the `courier_location_update` WebSocket event, exchanged verbatim in both
 * directions: the courier app publishes it, the customer app's [TelemetryWebSocketClient]
 * receives it, and [com.tuncayavci.tracking.database.TelemetrySyncWorker] replays it over REST
 * when the socket is unavailable. See TECHNICAL_SPECIFICATION.md section 4 for the contract.
 */
@Serializable
data class CourierTelemetryMessage(
    val event: String = EVENT_COURIER_LOCATION_UPDATE,
    @SerialName("order_id") val orderId: String,
    @SerialName("courier_id") val courierId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    @SerialName("speed_kmh") val speedKmh: Float,
    val timestamp: Long,
) {
    companion object {
        const val EVENT_COURIER_LOCATION_UPDATE = "courier_location_update"
    }
}
