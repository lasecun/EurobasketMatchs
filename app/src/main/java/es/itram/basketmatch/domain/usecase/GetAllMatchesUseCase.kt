package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los partidos
 */
class GetAllMatchesUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    operator fun invoke(): Flow<List<Match>> {
        return matchRepository.getAllMatches()
    }
}
