package com.tuncayavci.tracking.feature.tracking

import app.cash.turbine.test
import com.tuncayavci.tracking.location.CourierLocation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TrackingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: TrackingRepository = mockk()

    private fun viewModel() = TrackingViewModel(repository)

    @Test
    fun `starts in Idle state before any intent is dispatched`() =
        runTest {
            val viewModel = viewModel()

            assertEquals(TrackingUiState.Idle, viewModel.uiState.value)
        }

    @Test
    fun `StartTracking moves through Connecting then Tracking on the first courier update`() =
        runTest {
            val location = courierLocation()
            every { repository.observeCourierLocation("ORD-1", ENDPOINT) } returns flow { emit(location) }
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(TrackingUiState.Idle, awaitItem())

                viewModel.onIntent(TrackingIntent.StartTracking("ORD-1", ENDPOINT))

                assertEquals(TrackingUiState.Connecting, awaitItem())
                val tracking = awaitItem()
                assertTrue(tracking is TrackingUiState.Tracking)
                assertEquals(location, (tracking as TrackingUiState.Tracking).courierLocation)
            }
        }

    @Test
    fun `a stream failure moves the state to Error and emits a ShowError effect`() =
        runTest {
            every { repository.observeCourierLocation("ORD-1", ENDPOINT) } returns flow { throw IllegalStateException("socket closed") }
            val viewModel = viewModel()

            viewModel.effects.test {
                viewModel.onIntent(TrackingIntent.StartTracking("ORD-1", ENDPOINT))

                val effect = awaitItem()
                assertTrue(effect is TrackingEffect.ShowError)
                assertEquals("socket closed", (effect as TrackingEffect.ShowError).message)
            }
            assertTrue(viewModel.uiState.value is TrackingUiState.Error)
        }

    @Test
    fun `StopTracking resets state back to Idle`() =
        runTest {
            every { repository.observeCourierLocation("ORD-1", ENDPOINT) } returns flow { emit(courierLocation()) }
            val viewModel = viewModel()

            viewModel.onIntent(TrackingIntent.StartTracking("ORD-1", ENDPOINT))
            viewModel.onIntent(TrackingIntent.StopTracking)

            assertEquals(TrackingUiState.Idle, viewModel.uiState.value)
        }

    private fun courierLocation() =
        CourierLocation(
            latitude = 41.008238,
            longitude = 28.978359,
            bearing = 142.5f,
            speedKmh = 28.4f,
            timestamp = 1_787_784_000_000L,
        )

    private companion object {
        const val ENDPOINT = "wss://api.tracking.example.com/telemetry"
    }
}
