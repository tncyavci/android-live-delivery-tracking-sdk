package com.tuncayavci.tracking.location

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.HuaweiApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Strategy + Factory: picks the [MapAdapter] implementation that actually works on the running
 * device, so the rest of the SDK never has to branch on "which map provider am I using".
 */
@Module
@InstallIn(SingletonComponent::class)
object MapAdapterModule {
    @Provides
    @Singleton
    fun provideMapAdapter(
        @ApplicationContext context: Context,
        gmsAdapter: Provider<GmsMapAdapter>,
        hmsAdapter: Provider<HmsMapAdapter>,
        yandexAdapter: Provider<YandexMapAdapter>,
    ): MapAdapter {
        val isGmsAvailable =
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        return when {
            isGmsAvailable -> gmsAdapter.get()
            isHuaweiMobileServicesAvailable(context) -> hmsAdapter.get()
            else -> yandexAdapter.get()
        }
    }

    private fun isHuaweiMobileServicesAvailable(context: Context): Boolean {
        val result = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context)
        return result == com.huawei.hms.api.ConnectionResult.SUCCESS
    }
}
