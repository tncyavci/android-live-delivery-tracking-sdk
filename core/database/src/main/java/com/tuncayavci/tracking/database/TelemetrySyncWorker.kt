package com.tuncayavci.tracking.database

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tuncayavci.tracking.network.TelemetrySyncApi
import com.tuncayavci.tracking.network.TelemetrySyncRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Flushes any [CourierLocationEntity] rows buffered while offline to the REST sync endpoint,
 * scheduled by the host app as a one-time or periodic `WorkRequest` with a network constraint.
 */
@HiltWorker
class TelemetrySyncWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParams: WorkerParameters,
        private val telemetryDao: TelemetryDao,
        private val telemetrySyncApi: TelemetrySyncApi,
    ) : CoroutineWorker(context, workerParams) {
        override suspend fun doWork(): Result {
            val unsynced = telemetryDao.getUnsynced()
            if (unsynced.isEmpty()) return Result.success()

            return runCatching {
                telemetrySyncApi.syncTelemetryBatch(
                    TelemetrySyncRequest(events = unsynced.map { it.toMessage() }),
                )
            }.fold(
                onSuccess = {
                    telemetryDao.markSynced(unsynced.map { it.id })
                    Result.success()
                },
                onFailure = { Result.retry() },
            )
        }

        companion object {
            const val WORK_NAME = "telemetry_sync_worker"
        }
    }
