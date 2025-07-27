package es.itram.basketmatch.domain.repository

import es.itram.basketmatch.domain.model.TeamRoster

/**
 * Repositorio para datos de roster de equipos
 */
interface TeamRosterRepository {
    
    /**
     * Obtiene el roster completo de un equipo por su código TLA
     */
    suspend fun getTeamRoster(teamTla: String, season: String = "2025-26"): Result<TeamRoster>
    
    /**
     * Obtiene el roster de un equipo desde caché local si está disponible
     */
    suspend fun getCachedTeamRoster(teamTla: String): TeamRoster?
    
    /**
     * Fuerza la actualización del roster desde la API remota
     */
    suspend fun refreshTeamRoster(teamTla: String, season: String = "2025-26"): Result<TeamRoster>
}
