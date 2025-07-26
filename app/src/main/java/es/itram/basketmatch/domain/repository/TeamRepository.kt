package es.itram.basketmatch.domain.repository

import es.itram.basketmatch.domain.entity.Team
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface para operaciones relacionadas con equipos
 */
interface TeamRepository {
    
    /**
     * Obtiene todos los equipos participantes en la EuroLeague 2026
     */
    fun getAllTeams(): Flow<List<Team>>
    
    /**
     * Obtiene un equipo específico por ID
     */
    fun getTeamById(teamId: String): Flow<Team?>
    
    /**
     * Obtiene equipos por país
     */
    fun getTeamsByCountry(country: String): Flow<List<Team>>
    
    /**
     * Obtiene todos los equipos favoritos
     */
    fun getFavoriteTeams(): Flow<List<Team>>
    
    /**
     * Inserta equipos en la base de datos
     */
    suspend fun insertTeams(teams: List<Team>)
    
    /**
     * Actualiza un equipo
     */
    suspend fun updateTeam(team: Team)
    
    /**
     * Elimina todos los equipos
     */
    suspend fun deleteAllTeams()
}
