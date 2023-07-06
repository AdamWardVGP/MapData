package com.award.mapdata.di

import com.arcgismaps.tasks.offlinemaptask.PreplannedMapArea
import com.award.mapdata.data.EsriMapDataConverter
import com.award.mapdata.data.EsriMapRepository
import com.award.mapdata.data.MapDataSource
import com.award.mapdata.data.MapRepository
import com.award.mapdata.data.EsriNetworkedMapDataSource
import com.award.mapdata.data.MapDataConverter

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
    abstract fun bindEsriMapDataSource(impl: EsriNetworkedMapDataSource): MapDataSource<PreplannedMapArea>

    @Binds
    abstract fun bindEsriConverter(impl: EsriMapDataConverter): MapDataConverter<PreplannedMapArea>

}