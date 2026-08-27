package com.tuncayavci.tracking.network.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives push-triggered lifecycle events for an active delivery (e.g. "Courier nearby",
 * "Order status updated") that should surface even when the tracking screen isn't in the
 * foreground consuming the WebSocket stream directly.
 *
 * Consuming apps register a [CourierNotificationHandler] implementation (typically via Hilt) to
 * turn these payloads into their own notification/bottom-sheet UI, keeping this SDK UI-agnostic.
 */
class CourierFcmService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val event = message.data[KEY_EVENT] ?: return
        val orderId = message.data[KEY_ORDER_ID] ?: return

        val lifecycleEvent =
            when (event) {
                EVENT_COURIER_NEARBY -> CourierLifecycleEvent.CourierNearby(orderId)
                EVENT_ORDER_STATUS_UPDATED -> {
                    val status = message.data[KEY_STATUS] ?: return
                    CourierLifecycleEvent.OrderStatusUpdated(orderId, status)
                }
                else -> return
            }

        notificationHandler?.onCourierLifecycleEvent(lifecycleEvent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        notificationHandler?.onFcmTokenRefreshed(token)
    }

    companion object {
        private const val KEY_EVENT = "event"
        private const val KEY_ORDER_ID = "order_id"
        private const val KEY_STATUS = "status"
        private const val EVENT_COURIER_NEARBY = "courier_nearby"
        private const val EVENT_ORDER_STATUS_UPDATED = "order_status_updated"

        /**
         * Set by the host app at startup. A plain nullable var (rather than Hilt field injection)
         * because [FirebaseMessagingService] instances are created by the OS, not the DI graph.
         */
        var notificationHandler: CourierNotificationHandler? = null
    }
}

sealed interface CourierLifecycleEvent {
    val orderId: String

    data class CourierNearby(override val orderId: String) : CourierLifecycleEvent

    data class OrderStatusUpdated(override val orderId: String, val status: String) : CourierLifecycleEvent
}

interface CourierNotificationHandler {
    fun onCourierLifecycleEvent(event: CourierLifecycleEvent)

    fun onFcmTokenRefreshed(token: String)
}
