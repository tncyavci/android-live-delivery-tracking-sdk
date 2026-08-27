package com.tuncayavci.tracking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tuncayavci.tracking.feature.tracking.TrackingScreen
import com.tuncayavci.tracking.location.MapAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
                    )
                }
            }
        }
    }

    private companion object {
        const val DEMO_ORDER_ID = "ORD-2026-89421"
        const val DEMO_ENDPOINT_URL = "wss://api.tracking.example.com/telemetry"
    }
}
