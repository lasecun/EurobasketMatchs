package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.StandingDao
import es.itram.basketmatch.data.mapper.StandingMapper
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.repository.StandingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de clasificación
 */
@Singleton
class StandingRepositoryImpl @Inject constructor(
    private val standingDao: StandingDao
) : StandingRepository {

    override fun getAllStandings(): Flow<List<Standing>> {
        return standingDao.getAllStandings().map { entities ->
            StandingMapper.toDomainList(entities)
        }
    }

    override fun getStandingsBySeasonType(seasonType: SeasonType): Flow<List<Standing>> {
        return standingDao.getStandingsBySeasonType(seasonType).map { entities ->
            StandingMapper.toDomainList(entities)
        }
    }

    override fun getStandingByTeam(teamId: String): Flow<Standing?> {
        return standingDao.getStandingByTeam(teamId).map { entity ->
            entity?.let { StandingMapper.toDomain(it) }
        }
    }

    override fun getTopStandings(maxPosition: Int): Flow<List<Standing>> {
        return standingDao.getTopStandings(maxPosition).map { entities ->
            StandingMapper.toDomainList(entities)
        }
    }

    override suspend fun insertStandings(standings: List<Standing>) {
        val entities = StandingMapper.fromDomainList(standings)
        standingDao.insertStandings(entities)
    }

    override suspend fun updateStanding(standing: Standing) {
        val entity = StandingMapper.fromDomain(standing)
        standingDao.insertStanding(entity)
    }

    override suspend fun deleteAllStandings() {
        standingDao.deleteAllStandings()
    }

    override suspend fun deleteStandingsBySeasonType(seasonType: SeasonType) {
        standingDao.deleteStandingsBySeasonType(seasonType)
    }
}
