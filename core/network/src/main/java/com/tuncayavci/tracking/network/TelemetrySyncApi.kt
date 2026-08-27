package com.tuncayavci.tracking.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * REST fallback for [CourierTelemetryMessage] batches that couldn't be delivered over the live
 * WebSocket (tunnel/cellular dead zones). Used by
 * [com.tuncayavci.tracking.database.TelemetrySyncWorker] once connectivity is restored.
 */
interface TelemetrySyncApi {
    @POST("v1/telemetry/sync")
    suspend fun syncTelemetryBatch(
        @Body request: TelemetrySyncRequest,
    ): TelemetrySyncResponse
}

@kotlinx.serialization.Serializable
data class TelemetrySyncRequest(
    val events: List<CourierTelemetryMessage>,
)

@kotlinx.serialization.Serializable
data class TelemetrySyncResponse(
    val accepted: Int,
)
