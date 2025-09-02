package es.itram.basketmatch.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.TeamRepository

/**
 * Entry Point para acceder a los repositorios desde widgets 
 * (que no pueden usar @AndroidEntryPoint)
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun matchRepository(): MatchRepository
    fun teamRepository(): TeamRepository
}
