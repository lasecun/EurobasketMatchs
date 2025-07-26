package ch.biketec.t.data.repository

import ch.biketec.t.data.datasource.local.dao.MatchDao
import ch.biketec.t.data.datasource.local.dao.TeamDao
import ch.biketec.t.data.mapper.toDomain
import ch.biketec.t.data.mapper.toEntity
import ch.biketec.t.domain.entity.Match
import ch.biketec.t.domain.entity.MatchStatus
import ch.biketec.t.domain.entity.SeasonType
import ch.biketec.t.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val teamDao: TeamDao
) : MatchRepository {

    override suspend fun getAllMatches(): Flow<List<Match>> {
        return combine(
            matchDao.getAllMatches(),
            teamDao.getAllTeams()
        ) { matches, teams ->
            val teamMap = teams.associateBy { it.id }
            matches.mapNotNull { matchEntity ->
                val homeTeam = teamMap[matchEntity.homeTeamId]?.toDomain()
                val awayTeam = teamMap[matchEntity.awayTeamId]?.toDomain()
                if (homeTeam != null && awayTeam != null) {
                    matchEntity.toDomain(homeTeam, awayTeam)
                } else null
            }
        }
    }

    override suspend fun getMatchesByDate(date: LocalDate): Flow<List<Match>> {
        return combine(
            matchDao.getMatchesByDate(date.atStartOfDay()),
            teamDao.getAllTeams()
        ) { matches, teams ->
            val teamMap = teams.associateBy { it.id }
            matches.mapNotNull { matchEntity ->
                val homeTeam = teamMap[matchEntity.homeTeamId]?.toDomain()
                val awayTeam = teamMap[matchEntity.awayTeamId]?.toDomain()
                if (homeTeam != null && awayTeam != null) {
                    matchEntity.toDomain(homeTeam, awayTeam)
                } else null
            }.filter { it.dateTime.toLocalDate() == date }
        }
    }

    override suspend fun getMatchesByTeam(teamId: String): Flow<List<Match>> {
        return combine(
            matchDao.getMatchesByTeam(teamId),
            teamDao.getAllTeams()
        ) { matches, teams ->
            val teamMap = teams.associateBy { it.id }
            matches.mapNotNull { matchEntity ->
                val homeTeam = teamMap[matchEntity.homeTeamId]?.toDomain()
                val awayTeam = teamMap[matchEntity.awayTeamId]?.toDomain()
                if (homeTeam != null && awayTeam != null) {
                    matchEntity.toDomain(homeTeam, awayTeam)
                } else null
            }
        }
    }

    override suspend fun getMatchesByStatus(status: MatchStatus): Flow<List<Match>> {
        return combine(
            matchDao.getMatchesByStatus(status),
            teamDao.getAllTeams()
        ) { matches, teams ->
            val teamMap = teams.associateBy { it.id }
            matches.mapNotNull { matchEntity ->
                val homeTeam = teamMap[matchEntity.homeTeamId]?.toDomain()
                val awayTeam = teamMap[matchEntity.awayTeamId]?.toDomain()
                if (homeTeam != null && awayTeam != null) {
                    matchEntity.toDomain(homeTeam, awayTeam)
                } else null
            }
        }
    }

    override suspend fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<Match>> {
        return combine(
            matchDao.getMatchesBySeasonType(seasonType),
            teamDao.getAllTeams()
        ) { matches, teams ->
            val teamMap = teams.associateBy { it.id }
            matches.mapNotNull { matchEntity ->
                val homeTeam = teamMap[matchEntity.homeTeamId]?.toDomain()
                val awayTeam = teamMap[matchEntity.awayTeamId]?.toDomain()
                if (homeTeam != null && awayTeam != null) {
                    matchEntity.toDomain(homeTeam, awayTeam)
                } else null
            }
        }
    }

    override suspend fun getMatchById(matchId: String): Flow<Match?> {
        return combine(
            matchDao.getMatchById(matchId),
            teamDao.getAllTeams()
        ) { matchEntity, teams ->
            if (matchEntity != null) {
                val teamMap = teams.associateBy { it.id }
                val homeTeam = teamMap[matchEntity.homeTeamId]?.toDomain()
                val awayTeam = teamMap[matchEntity.awayTeamId]?.toDomain()
                if (homeTeam != null && awayTeam != null) {
                    matchEntity.toDomain(homeTeam, awayTeam)
                } else null
            } else null
        }
    }

    override suspend fun syncMatches(): Result<Unit> {
        // TODO: Implementar sincronización con API remota
        return Result.success(Unit)
    }
}
