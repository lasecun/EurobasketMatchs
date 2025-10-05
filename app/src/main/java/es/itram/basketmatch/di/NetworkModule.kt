package es.itram.basketmatch.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import es.itram.basketmatch.domain.service.DataSyncService
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt para dependencias de red - SOLO API OFICIAL
 *
 * ✅ Eliminado completamente el scraper web
 * ✅ Solo API oficial de EuroLeague
 * ✅ Arquitectura simplificada y limpia
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTeamWebMapper(): TeamWebMapper {
        return TeamWebMapper
    }
    
    @Provides
    @Singleton
    fun provideMatchWebMapper(): MatchWebMapper {
        return MatchWebMapper
    }
    
    @Provides
    @Singleton
    fun provideEuroLeagueRemoteDataSource(
        officialApiDataSource: EuroLeagueOfficialApiDataSource
    ): EuroLeagueRemoteDataSource {
        return EuroLeagueRemoteDataSource(officialApiDataSource)
    }

    @Provides
    @Singleton
    fun provideDataSyncService(
        euroLeagueRemoteDataSource: EuroLeagueRemoteDataSource,
        teamDao: TeamDao,
        matchDao: MatchDao,
        teamMapper: TeamWebMapper,
        matchMapper: MatchWebMapper,
        prefs: SharedPreferences
    ): DataSyncService {
        return DataSyncService(euroLeagueRemoteDataSource, teamDao, matchDao, prefs)
    }

    @Provides
    @Singleton
    fun provideStaticDataGenerator(
        euroLeagueRemoteDataSource: EuroLeagueRemoteDataSource,
        @ApplicationContext context: Context
    ): es.itram.basketmatch.data.generator.StaticDataGenerator {
        return es.itram.basketmatch.data.generator.StaticDataGenerator(euroLeagueRemoteDataSource, context)
    }
}
