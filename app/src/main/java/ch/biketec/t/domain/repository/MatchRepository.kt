package ch.biketec.t.domain.repository

import ch.biketec.t.domain.entity.Match
import ch.biketec.t.domain.entity.MatchStatus
import ch.biketec.t.domain.entity.SeasonType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface para operaciones relacionadas con partidos
 */
interface MatchRepository {
    
    /**
     * Obtiene todos los partidos de la temporada 2026
     */
    suspend fun getAllMatches(): Flow<List<Match>>
    
    /**
     * Obtiene partidos por fecha específica
     */
    suspend fun getMatchesByDate(date: LocalDate): Flow<List<Match>>
    
    /**
     * Obtiene partidos de un equipo específico
     */
    suspend fun getMatchesByTeam(teamId: String): Flow<List<Match>>
    
    /**
     * Obtiene partidos por estado
     */
    suspend fun getMatchesByStatus(status: MatchStatus): Flow<List<Match>>
    
    /**
     * Obtiene partidos por tipo de temporada
     */
    suspend fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<Match>>
    
    /**
     * Obtiene un partido específico por ID
     */
    suspend fun getMatchById(matchId: String): Flow<Match?>
    
    /**
     * Sincroniza partidos desde la fuente remota
     */
    suspend fun syncMatches(): Result<Unit>
}
