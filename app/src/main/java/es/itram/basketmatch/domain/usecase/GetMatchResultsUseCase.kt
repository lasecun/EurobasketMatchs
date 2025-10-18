package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 🏀 Caso de uso para obtener resultados de partidos de Euroliga
 *
 * Filtra partidos que ya han terminado y tienen resultados disponibles
 */
class GetMatchResultsUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {

    /**
     * Obtiene todos los partidos que ya han terminado con sus resultados
     */
    operator fun invoke(): Flow<List<Match>> {
        return matchRepository.getMatchesByStatus(MatchStatus.FINISHED)
            .map { matches ->
                // Ordenar por fecha descendente para mostrar los más recientes primero
                matches.sortedByDescending { it.dateTime }
            }
    }

    /**
     * Obtiene resultados de partidos para un equipo específico
     */
    fun getResultsForTeam(teamId: String): Flow<List<Match>> {
        return matchRepository.getMatchesByTeam(teamId)
            .map { matches ->
                matches.filter { it.status == MatchStatus.FINISHED }
                    .sortedByDescending { it.dateTime }
            }
    }

    /**
     * Obtiene los últimos N resultados de partidos
     */
    fun getLatestResults(limit: Int = 10): Flow<List<Match>> {
        return invoke().map { matches ->
            matches.take(limit)
        }
    }
}
