package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.data.repository.MatchRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 🏀 Caso de uso para obtener resultados de partidos por fecha
 *
 * Permite filtrar resultados de partidos de Euroliga por fecha específica
 * ACTUALIZADO: Ahora usa la API oficial v3 para obtener resultados reales
 */
class GetMatchResultsByDateUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {

    /**
     * Obtiene resultados de partidos para una fecha específica usando la API oficial v3
     * Esta función obtiene resultados reales con marcadores finales del 30 de septiembre de 2025
     */
    operator fun invoke(date: LocalDateTime): Flow<List<Match>> {
        // Usar la nueva funcionalidad de API v3 si está disponible
        return if (matchRepository is MatchRepositoryImpl) {
            matchRepository.getMatchResultsByDateFromApi(date)
                .map { matches ->
                    matches.filter { it.status == MatchStatus.FINISHED }
                        .sortedBy { it.dateTime }
                }
        } else {
            // Fallback a datos locales
            matchRepository.getMatchesByDate(date)
                .map { matches ->
                    matches.filter { it.status == MatchStatus.FINISHED }
                        .sortedBy { it.dateTime }
                }
        }
    }
}
