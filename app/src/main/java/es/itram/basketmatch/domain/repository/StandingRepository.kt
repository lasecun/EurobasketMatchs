package es.itram.basketmatch.domain.repository

import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.entity.Standing
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface para operaciones relacionadas con clasificaciones
 */
interface StandingRepository {
    
    /**
     * Obtiene todas las clasificaciones
     */
    fun getAllStandings(): Flow<List<Standing>>
    
    /**
     * Obtiene la clasificación por tipo de temporada
     */
    fun getStandingsBySeasonType(seasonType: SeasonType): Flow<List<Standing>>
    
    /**
     * Obtiene la clasificación de un equipo específico
     */
    fun getStandingByTeam(teamId: String): Flow<Standing?>
    
    /**
     * Obtiene los primeros equipos de la clasificación
     */
    fun getTopStandings(maxPosition: Int): Flow<List<Standing>>
    
    /**
     * Inserta clasificaciones en la base de datos
     */
    suspend fun insertStandings(standings: List<Standing>)
    
    /**
     * Actualiza una clasificación
     */
    suspend fun updateStanding(standing: Standing)
    
    /**
     * Elimina todas las clasificaciones
     */
    suspend fun deleteAllStandings()
    
    /**
     * Elimina clasificaciones por tipo de temporada
     */
    suspend fun deleteStandingsBySeasonType(seasonType: SeasonType)
}
