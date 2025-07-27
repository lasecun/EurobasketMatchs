package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obtener un partido específico por su ID
 */
class GetMatchByIdUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    
    /**
     * Obtiene un partido por su ID
     * @param matchId ID del partido a buscar
     * @return Flow con el partido encontrado o null si no existe
     */
    operator fun invoke(matchId: String): Flow<Match?> {
        return matchRepository.getMatchById(matchId)
    }
}
