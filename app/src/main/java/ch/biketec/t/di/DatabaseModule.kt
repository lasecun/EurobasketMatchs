package ch.biketec.t.di

import android.content.Context
import androidx.room.Room
import ch.biketec.t.data.datasource.local.EuroLeagueDatabase
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
