package ch.biketec.t.domain.repository

import ch.biketec.t.domain.entity.Team
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface para operaciones relacionadas con equipos
 */
interface TeamRepository {
    
    /**
     * Obtiene todos los equipos participantes en la EuroLeague 2026
     */
    suspend fun getAllTeams(): Flow<List<Team>>
    
    /**
     * Obtiene un equipo específico por ID
     */
    suspend fun getTeamById(teamId: String): Flow<Team?>
    
    /**
     * Busca equipos por nombre o ciudad
     */
    suspend fun searchTeams(query: String): Flow<List<Team>>
    
    /**
     * Obtiene todos los equipos favoritos
     */
    suspend fun getFavoriteTeams(): Flow<List<Team>>
    
    /**
     * Añade un equipo a favoritos
     */
    suspend fun addToFavorites(teamId: String): Result<Unit>
    
    /**
     * Quita un equipo de favoritos
     */
    suspend fun removeFromFavorites(teamId: String): Result<Unit>
    
    /**
     * Sincroniza equipos desde la fuente remota
     */
    suspend fun syncTeams(): Result<Unit>
}
