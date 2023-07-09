package com.award.mapdata

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.award.mapdata.data.base.MapRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Monitors app lifecycle for cancellation of outstanding download jobs
 */
class AppScopeDownloadMonitor @Inject constructor(
    val mapRepository: MapRepository
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            mapRepository.cancelRunningDownloads()
        }
    }

}