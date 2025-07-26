package ch.biketec.t.di

import android.content.Context
import androidx.room.Room
import ch.biketec.t.data.datasource.local.EuroLeagueDatabase
import ch.biketec.t.data.repository.MatchRepositoryImpl
import ch.biketec.t.data.repository.StandingRepositoryImpl
import ch.biketec.t.data.repository.TeamRepositoryImpl
import ch.biketec.t.domain.repository.MatchRepository
import ch.biketec.t.domain.repository.StandingRepository
import ch.biketec.t.domain.repository.TeamRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar dependencias de base de datos local
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
            context,
            EuroLeagueDatabase::class.java,
            "euroleague_database"
        )
            .fallbackToDestructiveMigration() // Solo para desarrollo inicial
            .build()
    }

    @Provides
    fun provideMatchDao(database: EuroLeagueDatabase) = database.matchDao()

    @Provides
    fun provideTeamDao(database: EuroLeagueDatabase) = database.teamDao()

    @Provides
    fun provideStandingDao(database: EuroLeagueDatabase) = database.standingDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMatchRepository(
        matchRepositoryImpl: MatchRepositoryImpl
    ): MatchRepository

    @Binds
    abstract fun bindTeamRepository(
        teamRepositoryImpl: TeamRepositoryImpl
    ): TeamRepository

    @Binds
    abstract fun bindStandingRepository(
        standingRepositoryImpl: StandingRepositoryImpl
    ): StandingRepository
}
