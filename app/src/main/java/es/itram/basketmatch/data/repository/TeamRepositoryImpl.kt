package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de equipos
 */
@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao
) : TeamRepository {

    override fun getAllTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams().map { entities ->
            TeamMapper.toDomainList(entities)
        }
    }

    override fun getTeamById(teamId: String): Flow<Team?> {
        return teamDao.getTeamById(teamId).map { entity ->
            entity?.let { TeamMapper.toDomain(it) }
        }
    }

    override fun getTeamsByCountry(country: String): Flow<List<Team>> {
        return teamDao.getTeamsByCountry(country).map { entities ->
            TeamMapper.toDomainList(entities)
        }
    }

    override fun getFavoriteTeams(): Flow<List<Team>> {
        return teamDao.getFavoriteTeams().map { entities ->
            TeamMapper.toDomainList(entities)
        }
    }

    override suspend fun insertTeams(teams: List<Team>) {
        val entities = TeamMapper.fromDomainList(teams)
        teamDao.insertTeams(entities)
    }

    override suspend fun updateTeam(team: Team) {
        val entity = TeamMapper.fromDomain(team)
        teamDao.insertTeam(entity)
    }

    override suspend fun deleteAllTeams() {
        teamDao.deleteAllTeams()
    }
}
