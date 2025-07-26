package ch.biketec.t.domain.repository

import ch.biketec.t.domain.entity.Standing
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface para operaciones relacionadas con clasificaciones
 */
interface StandingRepository {
    
    /**
     * Obtiene la clasificación actual de la EuroLeague 2026
     */
    suspend fun getCurrentStandings(): Flow<List<Standing>>
    
    /**
     * Obtiene la clasificación de un equipo específico
     */
    suspend fun getTeamStanding(teamId: String): Flow<Standing?>
    
    /**
     * Sincroniza clasificaciones desde la fuente remota
     */
    suspend fun syncStandings(): Result<Unit>
}
