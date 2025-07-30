package es.itram.basketmatch.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource

import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import es.itram.basketmatch.domain.service.DataSyncService
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt para dependencias de red y scraping
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
    fun provideEuroLeagueJsonApiScraper(): EuroLeagueJsonApiScraper {
        return EuroLeagueJsonApiScraper()
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
        jsonApiScraper: EuroLeagueJsonApiScraper
    ): EuroLeagueRemoteDataSource {
        return EuroLeagueRemoteDataSource(jsonApiScraper)
    }
    
    @Provides
    @Singleton
    fun provideDataSyncService(
        jsonApiScraper: EuroLeagueJsonApiScraper,
        teamDao: TeamDao,
        matchDao: MatchDao,
        teamMapper: TeamWebMapper,
        matchMapper: MatchWebMapper,
        @ApplicationContext context: Context
    ): DataSyncService {
        return DataSyncService(jsonApiScraper, teamDao, matchDao, teamMapper, matchMapper, context)
    }
}
