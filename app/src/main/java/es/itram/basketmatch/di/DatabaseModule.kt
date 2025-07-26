package es.itram.basketmatch.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.local.EuroLeagueDatabase
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.StandingDao
import javax.inject.Singleton

/**
 * Módulo de Hilt para la base de datos
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEuroLeagueDatabase(
        @ApplicationContext context: Context
    ): EuroLeagueDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            EuroLeagueDatabase::class.java,
            EuroLeagueDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideTeamDao(database: EuroLeagueDatabase): TeamDao {
        return database.teamDao()
    }

    @Provides
    fun provideMatchDao(database: EuroLeagueDatabase): MatchDao {
        return database.matchDao()
    }

    @Provides
    fun provideStandingDao(database: EuroLeagueDatabase): StandingDao {
        return database.standingDao()
    }
}
