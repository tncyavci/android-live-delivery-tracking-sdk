package com.tuncayavci.tracking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tuncayavci.tracking.feature.tracking.TrackingIntent
import com.tuncayavci.tracking.feature.tracking.TrackingScreen
import com.tuncayavci.tracking.location.LatLngPoint
import com.tuncayavci.tracking.location.MapAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This demo app has no live backend to point `endpointUrl` at, so it drives the tracking screen
 * with [TrackingIntent.StartDemo] over a simulated Istanbul courier route instead of the real
 * [TrackingIntent.StartTracking] WebSocket path a production host app would use.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mapAdapter: MapAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackingScreen(
                        orderId = DEMO_ORDER_ID,
                        endpointUrl = DEMO_ENDPOINT_URL,
                        mapAdapter = mapAdapter,
                        startIntent = TrackingIntent.StartDemo(DEMO_ROUTE, tickIntervalMs = DEMO_TICK_INTERVAL_MS),
                    )
                }
            }
        }
    }

    private companion object {
        const val DEMO_ORDER_ID = "ORD-2026-89421"
        const val DEMO_ENDPOINT_URL = "wss://api.tracking.example.com/telemetry"
        const val DEMO_TICK_INTERVAL_MS = 2_500L

        // A short courier route through Taksim/Beyoglu, Istanbul.
        val DEMO_ROUTE =
            listOf(
                LatLngPoint(41.036946, 28.985046),
                LatLngPoint(41.034432, 28.984671),
                LatLngPoint(41.032101, 28.982951),
                LatLngPoint(41.029828, 28.979869),
                LatLngPoint(41.028661, 28.977434),
                LatLngPoint(41.028072, 28.974513),
            )
    }
}
