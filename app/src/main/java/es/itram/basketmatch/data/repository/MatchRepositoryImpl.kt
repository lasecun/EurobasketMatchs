package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de partidos
 */
@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao
) : MatchRepository {

    override fun getAllMatches(): Flow<List<Match>> {
        return matchDao.getAllMatches().map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByDate(date: LocalDateTime): Flow<List<Match>> {
        return matchDao.getMatchesByDate(date).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByTeam(teamId: String): Flow<List<Match>> {
        return matchDao.getMatchesByTeam(teamId).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByStatus(status: MatchStatus): Flow<List<Match>> {
        return matchDao.getMatchesByStatus(status).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<Match>> {
        return matchDao.getMatchesBySeasonType(seasonType).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchById(matchId: String): Flow<Match?> {
        return matchDao.getMatchById(matchId).map { entity ->
            entity?.let { MatchMapper.toDomain(it) }
        }
    }

    override suspend fun insertMatches(matches: List<Match>) {
        val entities = MatchMapper.fromDomainList(matches)
        matchDao.insertMatches(entities)
    }

    override suspend fun updateMatch(match: Match) {
        val entity = MatchMapper.fromDomain(match)
        matchDao.insertMatch(entity)
    }

    override suspend fun deleteAllMatches() {
        matchDao.deleteAllMatches()
    }
}
