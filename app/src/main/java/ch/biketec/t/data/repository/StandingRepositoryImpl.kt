package ch.biketec.t.data.repository

import ch.biketec.t.data.datasource.local.dao.StandingDao
import ch.biketec.t.data.datasource.local.dao.TeamDao
import ch.biketec.t.data.mapper.toDomain
import ch.biketec.t.domain.entity.SeasonType
import ch.biketec.t.domain.entity.Standing
import ch.biketec.t.domain.repository.StandingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StandingRepositoryImpl @Inject constructor(
    private val standingDao: StandingDao,
    private val teamDao: TeamDao
) : StandingRepository {

    override suspend fun getCurrentStandings(): Flow<List<Standing>> {
        return combine(
            standingDao.getCurrentStandings(),
            teamDao.getAllTeams()
        ) { standings, teams ->
            val teamMap = teams.associateBy { it.id }
            standings.mapNotNull { standingEntity ->
                val team = teamMap[standingEntity.teamId]?.toDomain()
                if (team != null) {
                    standingEntity.toDomain(team)
                } else null
            }.sortedBy { it.position }
        }
    }

    override suspend fun getStandingsBySeasonType(seasonType: SeasonType): Flow<List<Standing>> {
        return combine(
            standingDao.getStandingsBySeasonType(seasonType),
            teamDao.getAllTeams()
        ) { standings, teams ->
            val teamMap = teams.associateBy { it.id }
            standings.mapNotNull { standingEntity ->
                val team = teamMap[standingEntity.teamId]?.toDomain()
                if (team != null) {
                    standingEntity.toDomain(team)
                } else null
            }.sortedBy { it.position }
        }
    }

    override suspend fun getTeamStanding(teamId: String): Flow<Standing?> {
        return combine(
            standingDao.getTeamStanding(teamId),
            teamDao.getTeamById(teamId)
        ) { standingEntity, teamEntity ->
            if (standingEntity != null && teamEntity != null) {
                standingEntity.toDomain(teamEntity.toDomain())
            } else null
        }
    }

    override suspend fun syncStandings(): Result<Unit> {
        // TODO: Implementar sincronización con API remota
        return Result.success(Unit)
    }
}
