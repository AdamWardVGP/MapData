package com.award.mapdata.di

import android.content.Context
import com.award.mapdata.BuildConfig
import com.award.mapdata.R

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class MapConfigModule {

    @Provides
    @Named("GIS_API_KEY")
    fun provideGisApiKey(): String {
        return BuildConfig.API_KEY
    }

    @Provides
    @Named("GIS_ENDPOINT_BASE")
    fun provideGisUriBase(@ApplicationContext application: Context): String {
        return application.getString(R.string.esri_base_uri)
    }

    @Provides
    @Named("DOWNLOAD_FILE")
    fun provideOfflineMapFilePath(@ApplicationContext application: Context): File {
        return File(application.externalCacheDir?.path + application.getString(R.string.offline_map_dir))
    }

}