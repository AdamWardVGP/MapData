package com.award.mapdata

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidApp
class MapDataApplication : Application() {

    @Inject
    @Named("GIS_API_KEY")
    lateinit var gisKey: String

    @Inject
    lateinit var downloadMonitor: AppScopeDownloadMonitor
    override fun onCreate() {
        super.onCreate()

        ArcGISEnvironment.apiKey = ApiKey.create(gisKey)

        ProcessLifecycleOwner.get().lifecycle.addObserver(downloadMonitor)

    }
}