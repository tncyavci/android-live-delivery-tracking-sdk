package com.tuncayavci.tracking.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuncayavci.tracking.location.LatLngPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel
    @Inject
    constructor(
        private val repository: TrackingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<TrackingUiState>(TrackingUiState.Idle)
        val uiState: StateFlow<TrackingUiState> = _uiState

        private val _effects = Channel<TrackingEffect>(Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        private var trackingJob: Job? = null
        private var lastIntent: TrackingIntent.StartTracking? = null
        private var currentRoutePoints: List<LatLngPoint> = emptyList()

        fun onIntent(intent: TrackingIntent) {
            when (intent) {
                is TrackingIntent.StartTracking -> startTracking(intent)
                TrackingIntent.StopTracking -> stopTracking()
                TrackingIntent.Retry -> lastIntent?.let { startTracking(it) }
            }
        }

        private fun startTracking(intent: TrackingIntent.StartTracking) {
            lastIntent = intent
            currentRoutePoints = intent.routePoints
            trackingJob?.cancel()
            _uiState.value = TrackingUiState.Connecting

            val exceptionHandler =
                CoroutineExceptionHandler { _, throwable ->
                    onTrackingFailure(throwable)
                }

            trackingJob =
                viewModelScope.launch(exceptionHandler) {
                    repository.observeCourierLocation(intent.orderId, intent.endpointUrl)
                        .catch { onTrackingFailure(it) }
                        .collect { location ->
                            _uiState.value =
                                TrackingUiState.Tracking(
                                    courierLocation = location,
                                    routePoints = currentRoutePoints,
                                )
                        }
                }
        }

        private fun stopTracking() {
            trackingJob?.cancel()
            trackingJob = null
            _uiState.value = TrackingUiState.Idle
        }

        private fun onTrackingFailure(throwable: Throwable) {
            val message = throwable.message ?: "Courier telemetry stream disconnected"
            _uiState.value = TrackingUiState.Error(message)
            _effects.trySend(TrackingEffect.ShowError(message))
        }

        override fun onCleared() {
            trackingJob?.cancel()
            super.onCleared()
        }
    }
