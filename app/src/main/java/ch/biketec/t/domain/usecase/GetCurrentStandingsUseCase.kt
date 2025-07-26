package ch.biketec.t.domain.usecase

import ch.biketec.t.domain.entity.Standing
import ch.biketec.t.domain.repository.StandingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obtener la clasificación actual de la EuroLeague 2026
 */
class GetCurrentStandingsUseCase @Inject constructor(
    private val standingRepository: StandingRepository
) {
    suspend operator fun invoke(): Flow<List<Standing>> {
        return standingRepository.getCurrentStandings()
    }
}
