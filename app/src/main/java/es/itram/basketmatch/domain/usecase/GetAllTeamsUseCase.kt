package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los equipos
 */
class GetAllTeamsUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    operator fun invoke(): Flow<List<Team>> {
        return teamRepository.getAllTeams()
    }
}
