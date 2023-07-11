package com.award.mapdata.di

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.esri.EsriPortalItemConverter
import com.award.mapdata.data.esri.EsriMapRepository
import com.award.mapdata.data.base.MapDataSource
import com.award.mapdata.data.base.MapRepository
import com.award.mapdata.data.base.DownloadableMapAreaSource
import com.award.mapdata.data.esri.EsriNetworkedMapDataSource
import com.award.mapdata.data.base.MapDataConverter
import com.award.mapdata.data.esri.EsriNetworkedMapAreaSource

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MapDataProviderModule {

    @Binds
    abstract fun bindDependency(impl: EsriMapRepository): MapRepository

    @Binds
    abstract fun bindEsriMapDataSource(impl: EsriNetworkedMapDataSource): MapDataSource<PortalItem, ArcGISMap>

    @Binds
    abstract fun bindEsriConverter(impl: EsriPortalItemConverter): MapDataConverter<PortalItem>

    @Binds
    abstract fun bindEsriMapAreaSource(impl: EsriNetworkedMapAreaSource): DownloadableMapAreaSource<PreplannedMapArea>
}