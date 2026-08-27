package com.tuncayavci.tracking.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CourierTelemetryMessageTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes the courier_location_update payload contract`() {
        val payload =
            """
            {
              "event": "courier_location_update",
              "order_id": "ORD-2026-89421",
              "courier_id": "CR-502",
              "latitude": 41.008238,
              "longitude": 28.978359,
              "bearing": 142.5,
              "speed_kmh": 28.4,
              "timestamp": 1787784000000
            }
            """.trimIndent()

        val message = json.decodeFromString(CourierTelemetryMessage.serializer(), payload)

        assertEquals("courier_location_update", message.event)
        assertEquals("ORD-2026-89421", message.orderId)
        assertEquals("CR-502", message.courierId)
        assertEquals(41.008238, message.latitude, 0.000001)
        assertEquals(28.978359, message.longitude, 0.000001)
        assertEquals(142.5f, message.bearing)
        assertEquals(28.4f, message.speedKmh)
        assertEquals(1787784000000L, message.timestamp)
    }

    @Test
    fun `round-trips through encode and decode`() {
        val original =
            CourierTelemetryMessage(
                orderId = "ORD-1",
                courierId = "CR-1",
                latitude = 10.0,
                longitude = 20.0,
                bearing = 90f,
                speedKmh = 15f,
                timestamp = 1_000L,
            )

        val encoded = json.encodeToString(CourierTelemetryMessage.serializer(), original)
        val decoded = json.decodeFromString(CourierTelemetryMessage.serializer(), encoded)

        assertEquals(original, decoded)
    }
}
