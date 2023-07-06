package com.award.mapdata.di

import com.award.mapdata.BuildConfig

//import dagger.Binds
//import dagger.Module
//import javax.inject.Named
//
//@Module
class MapConfigModule {

//    @Binds()
//    @Named("GIS_API_KEY")
    fun provideGisApiKey(): String {
        return BuildConfig.API_KEY
    }
}