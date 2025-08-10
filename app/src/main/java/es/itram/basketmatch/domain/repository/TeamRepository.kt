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
     * Obtiene un equipo específico por código (MAD, FCB, etc.)
     */
    fun getTeamByCode(teamCode: String): Flow<Team?>
    
    /**
     * Obtiene equipos por país
     */
    fun getTeamsByCountry(country: String): Flow<List<Team>>
    
    /**
     * Obtiene todos los equipos favoritos
     */
    fun getFavoriteTeams(): Flow<List<Team>>
    
    /**
     * Actualiza el estado de favorito de un equipo por ID
     */
    suspend fun updateFavoriteStatus(teamId: String, isFavorite: Boolean)

    /**
     * Actualiza el estado de favorito de un equipo por código
     */
    suspend fun updateFavoriteStatusByCode(teamCode: String, isFavorite: Boolean)

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
