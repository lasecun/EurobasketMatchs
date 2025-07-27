package es.itram.basketmatch.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import javax.inject.Singleton

/**
 * Módulo Hilt para dependencias de red y scraping
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideEuroLeagueWebScraper(): EuroLeagueWebScraper {
        return EuroLeagueWebScraper()
    }
    
    @Provides
    @Singleton
    fun provideEuroLeagueJsonApiScraper(): EuroLeagueJsonApiScraper {
        return EuroLeagueJsonApiScraper()
    }
    
    @Provides
    @Singleton
    fun provideEuroLeagueRemoteDataSource(
        jsonApiScraper: EuroLeagueJsonApiScraper,
        webScraper: EuroLeagueWebScraper
    ): EuroLeagueRemoteDataSource {
        return EuroLeagueRemoteDataSource(jsonApiScraper, webScraper)
    }
}
