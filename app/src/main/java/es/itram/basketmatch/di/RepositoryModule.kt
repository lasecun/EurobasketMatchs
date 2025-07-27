package es.itram.basketmatch.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.repository.TeamRepositoryImpl
import es.itram.basketmatch.data.repository.MatchRepositoryImpl
import es.itram.basketmatch.data.repository.StandingRepositoryImpl
import es.itram.basketmatch.data.repository.TeamRosterRepositoryImpl
import es.itram.basketmatch.domain.repository.TeamRepository
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.StandingRepository
import es.itram.basketmatch.domain.repository.TeamRosterRepository
import javax.inject.Singleton

/**
 * Módulo de Hilt para los repositorios
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTeamRepository(
        teamRepositoryImpl: TeamRepositoryImpl
    ): TeamRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(
        matchRepositoryImpl: MatchRepositoryImpl
    ): MatchRepository

    @Binds
    @Singleton
    abstract fun bindStandingRepository(
        standingRepositoryImpl: StandingRepositoryImpl
    ): StandingRepository

    @Binds
    @Singleton
    abstract fun bindTeamRosterRepository(
        teamRosterRepositoryImpl: TeamRosterRepositoryImpl
    ): TeamRosterRepository
}
