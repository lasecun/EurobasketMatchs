package es.itram.basketmatch.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.local.assets.StaticDataManager
import es.itram.basketmatch.data.sync.SmartSyncManager
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para datos estáticos y sincronización inteligente
 */
@Module
@InstallIn(SingletonComponent::class)
object StaticDataModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }
    }

    @Provides
    @Singleton
    fun provideStaticDataManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
        json: Json
    ): StaticDataManager {
        return StaticDataManager(context, json)
    }

    @Provides
    @Singleton
    fun provideSmartSyncManager(
        staticDataManager: StaticDataManager,
        teamRepository: es.itram.basketmatch.domain.repository.TeamRepository,
        matchRepository: es.itram.basketmatch.domain.repository.MatchRepository,
        analyticsManager: es.itram.basketmatch.analytics.AnalyticsManager
    ): SmartSyncManager {
        return SmartSyncManager(
            staticDataManager,
            teamRepository,
            matchRepository,
            analyticsManager
        )
    }
}
