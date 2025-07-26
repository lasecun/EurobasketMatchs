package ch.biketec.t.domain.usecase

import ch.biketec.t.domain.entity.Match
import ch.biketec.t.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obtener todos los partidos de la EuroLeague 2026
 */
class GetAllMatchesUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(): Flow<List<Match>> {
        return matchRepository.getAllMatches()
    }
}
