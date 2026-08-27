package com.tuncayavci.tracking.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "tracking_sdk.db"

    @Provides
    @Singleton
    fun provideTrackingDatabase(
        @ApplicationContext context: Context,
    ): TrackingDatabase = Room.databaseBuilder(context, TrackingDatabase::class.java, DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideTelemetryDao(database: TrackingDatabase): TelemetryDao = database.telemetryDao()
}
