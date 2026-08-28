package com.tuncayavci.tracking.feature.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tuncayavci.tracking.feature.tracking.components.CourierMapView
import com.tuncayavci.tracking.location.LatLngPoint
import com.tuncayavci.tracking.location.MapAdapter

@Composable
fun TrackingScreen(
    orderId: String,
    endpointUrl: String,
    mapAdapter: MapAdapter,
    routePoints: List<LatLngPoint> = emptyList(),
    onShowError: (String) -> Unit = {},
    viewModel: TrackingViewModel = hiltViewModel(),
    startIntent: TrackingIntent = TrackingIntent.StartTracking(orderId, endpointUrl, routePoints),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(startIntent) {
        viewModel.onIntent(startIntent)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrackingEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    TrackingScreenContent(uiState = uiState, mapAdapter = mapAdapter, onRetry = { viewModel.onIntent(TrackingIntent.Retry) })
}

@Composable
internal fun TrackingScreenContent(
    uiState: TrackingUiState,
    mapAdapter: MapAdapter,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CourierMapView(mapAdapter = mapAdapter, modifier = Modifier.fillMaxSize())

        when (uiState) {
            TrackingUiState.Idle, TrackingUiState.Connecting -> ConnectingOverlay()
            is TrackingUiState.Tracking -> Unit // map already reflects the latest courier location
            is TrackingUiState.Error -> ErrorOverlay(message = uiState.message, onRetry = onRetry)
        }
    }
}

@Composable
private fun ConnectingOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SPACING)) {
            CircularProgressIndicator()
            Text(text = "Connecting to courier…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorOverlay(
    message: String,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SPACING),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

private val SPACING = 12.dp
