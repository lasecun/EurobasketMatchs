package es.itram.basketmatch.domain.repository

import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface para operaciones relacionadas con partidos
 */
interface MatchRepository {
    
    /**
     * Obtiene todos los partidos de la EuroLeague 2026
     */
    fun getAllMatches(): Flow<List<Match>>
    
    /**
     * Obtiene partidos de una fecha específica
     */
    fun getMatchesByDate(date: LocalDateTime): Flow<List<Match>>
    
    /**
     * Obtiene partidos de un equipo específico
     */
    fun getMatchesByTeam(teamId: String): Flow<List<Match>>
    
    /**
     * Obtiene partidos por estado
     */
    fun getMatchesByStatus(status: MatchStatus): Flow<List<Match>>
    
    /**
     * Obtiene partidos por tipo de temporada
     */
    fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<Match>>
    
    /**
     * Obtiene un partido específico por ID
     */
    fun getMatchById(matchId: String): Flow<Match?>
    
    /**
     * Inserta partidos en la base de datos
     */
    suspend fun insertMatches(matches: List<Match>)
    
    /**
     * Actualiza un partido
     */
    suspend fun updateMatch(match: Match)
    
    /**
     * Elimina todos los partidos
     */
    suspend fun deleteAllMatches()
}
