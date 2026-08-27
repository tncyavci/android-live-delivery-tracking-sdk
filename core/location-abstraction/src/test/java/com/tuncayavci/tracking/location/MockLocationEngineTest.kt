package com.tuncayavci.tracking.location

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockLocationEngineTest {
    private val route =
        listOf(
            LatLngPoint(41.0, 29.0),
            LatLngPoint(41.1, 29.0),
            LatLngPoint(41.1, 29.1),
        )

    @Test
    fun `emits one tick per route point`() =
        runTest {
            MockLocationEngine.simulateRoute(route, tickIntervalMs = 0).test {
                assertEquals(41.0, awaitItem().latitude, DELTA)
                assertEquals(41.1, awaitItem().latitude, DELTA)
                val last = awaitItem()
                assertEquals(41.1, last.latitude, DELTA)
                assertEquals(29.1, last.longitude, DELTA)
                awaitComplete()
            }
        }

    @Test
    fun `the final tick reports zero speed since the courier has arrived`() =
        runTest {
            MockLocationEngine.simulateRoute(route, tickIntervalMs = 0).test {
                skipItems(2)
                assertEquals(0f, awaitItem().speedKmh)
                awaitComplete()
            }
        }

    @Test
    fun `rejects routes with fewer than two points`() =
        runTest {
            MockLocationEngine.simulateRoute(listOf(LatLngPoint(0.0, 0.0))).test {
                assertTrue(awaitError() is IllegalArgumentException)
            }
        }

    private companion object {
        const val DELTA = 0.0001
    }
}
