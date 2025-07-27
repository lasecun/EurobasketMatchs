package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.repository.TeamRosterRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener el roster de un equipo
 */
class GetTeamRosterUseCase @Inject constructor(
    private val teamRosterRepository: TeamRosterRepository
) {
    
    /**
     * Obtiene el roster completo de un equipo por su código TLA
     */
    suspend operator fun invoke(teamTla: String, season: String = "2025-26"): Result<TeamRoster> {
        return teamRosterRepository.getTeamRoster(teamTla, season)
    }
    
    /**
     * Fuerza la actualización del roster desde la API
     */
    suspend fun refresh(teamTla: String, season: String = "2025-26"): Result<TeamRoster> {
        return teamRosterRepository.refreshTeamRoster(teamTla, season)
    }
}
