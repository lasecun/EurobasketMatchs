package ch.biketec.t.domain.usecase

import ch.biketec.t.domain.entity.Team
import ch.biketec.t.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obtener todos los equipos de la EuroLeague 2026
 */
class GetAllTeamsUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(): Flow<List<Team>> {
        return teamRepository.getAllTeams()
    }
}
