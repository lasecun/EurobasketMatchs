package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.repository.StandingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener la clasificación actual
 */
class GetCurrentStandingsUseCase @Inject constructor(
    private val standingRepository: StandingRepository
) {
    operator fun invoke(): Flow<List<Standing>> {
        return standingRepository.getAllStandings()
    }
}
