package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.repository.StandingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 📊 Use Case para obtener la clasificación de la EuroLeague
 *
 * Recupera los datos de clasificación ordenados por posición,
 * permitiendo filtrar por tipo de temporada.
 */
class GetStandingsUseCase @Inject constructor(
    private val standingRepository: StandingRepository
) {
    /**
     * Obtiene todas las clasificaciones
     */
    operator fun invoke(): Flow<List<Standing>> {
        return standingRepository.getAllStandings()
    }

    /**
     * Obtiene la clasificación por tipo de temporada
     */
    operator fun invoke(seasonType: SeasonType): Flow<List<Standing>> {
        return standingRepository.getStandingsBySeasonType(seasonType)
    }

    /**
     * Obtiene el top N de equipos en la clasificación
     */
    fun getTopStandings(maxPosition: Int): Flow<List<Standing>> {
        return standingRepository.getTopStandings(maxPosition)
    }
}

