package com.tuncayavci.tracking.feature.tracking

import com.tuncayavci.tracking.location.CourierLocation
import com.tuncayavci.tracking.location.LatLngPoint

/** MVI state for the live courier tracking screen. */
sealed interface TrackingUiState {
    data object Idle : TrackingUiState

    data object Connecting : TrackingUiState

    data class Tracking(
        val courierLocation: CourierLocation,
        val routePoints: List<LatLngPoint>,
    ) : TrackingUiState

    data class Error(val message: String) : TrackingUiState
}

/** User/host-app intents the tracking screen can act on. */
sealed interface TrackingIntent {
    data class StartTracking(
        val orderId: String,
        val endpointUrl: String,
        val routePoints: List<LatLngPoint> = emptyList(),
    ) : TrackingIntent

    data object StopTracking : TrackingIntent

    data object Retry : TrackingIntent
}

/** One-shot effects the screen should react to but not retain in state. */
sealed interface TrackingEffect {
    data class ShowError(val message: String) : TrackingEffect
}
