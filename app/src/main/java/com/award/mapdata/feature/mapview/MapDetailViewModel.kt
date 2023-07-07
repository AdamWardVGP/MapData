package com.award.mapdata.feature.mapview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MapDetailViewModel @Inject constructor(
    @Named("GIS_ENDPOINT_BASE") public val esriUrlBase: String)
    : ViewModel()