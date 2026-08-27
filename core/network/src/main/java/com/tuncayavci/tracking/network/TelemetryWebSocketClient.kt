package com.tuncayavci.tracking.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin coroutine wrapper around an OkHttp WebSocket carrying `courier_location_update` events.
 *
 * - [observeTelemetry] is used by the customer-facing tracking screen to receive live updates.
 * - [send] is used by the courier app to publish its own position; a `false` result signals the
 *   caller (typically [com.tuncayavci.tracking.database.CourierLocationReporter]) to fall back to
 *   the offline-first Room buffer instead of losing the ping.
 */
@Singleton
class TelemetryWebSocketClient
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
        private val json: Json,
    ) {
        private var webSocket: WebSocket? = null

        fun observeTelemetry(
            orderId: String,
            endpointUrl: String,
        ): Flow<CourierTelemetryMessage> =
            callbackFlow {
                val request = Request.Builder().url(endpointUrl).build()
                val socket =
                    okHttpClient.newWebSocket(
                        request,
                        object : WebSocketListener() {
                            override fun onOpen(
                                webSocket: WebSocket,
                                response: Response,
                            ) {
                                webSocket.send(json.encodeToString(SubscribeMessage.serializer(), SubscribeMessage(orderId)))
                            }

                            override fun onMessage(
                                webSocket: WebSocket,
                                text: String,
                            ) {
                                runCatching { json.decodeFromString(CourierTelemetryMessage.serializer(), text) }
                                    .onSuccess { trySend(it) }
                            }

                            override fun onFailure(
                                webSocket: WebSocket,
                                t: Throwable,
                                response: Response?,
                            ) {
                                close(t)
                            }

                            override fun onClosed(
                                webSocket: WebSocket,
                                code: Int,
                                reason: String,
                            ) {
                                close()
                            }
                        },
                    )
                webSocket = socket

                awaitClose {
                    socket.close(NORMAL_CLOSURE_CODE, "tracking screen closed")
                    webSocket = null
                }
            }

        /** Attempts an immediate send over the live socket. Returns false if there is none open. */
        fun send(message: CourierTelemetryMessage): Boolean {
            val socket = webSocket ?: return false
            return socket.send(json.encodeToString(CourierTelemetryMessage.serializer(), message))
        }

        suspend fun reconnectBackoff(attempt: Int) {
            val delayMs = (INITIAL_BACKOFF_MS * (1 shl attempt.coerceAtMost(MAX_BACKOFF_SHIFT))).coerceAtMost(MAX_BACKOFF_MS)
            delay(delayMs)
        }

        @kotlinx.serialization.Serializable
        private data class SubscribeMessage(
            @kotlinx.serialization.SerialName("order_id") val orderId: String,
            val event: String = "subscribe",
        )

        private companion object {
            const val NORMAL_CLOSURE_CODE = 1000
            const val INITIAL_BACKOFF_MS = 500L
            const val MAX_BACKOFF_MS = 16_000L
            const val MAX_BACKOFF_SHIFT = 5
        }
    }
