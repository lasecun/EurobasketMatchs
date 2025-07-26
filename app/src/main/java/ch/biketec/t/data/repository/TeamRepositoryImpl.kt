package ch.biketec.t.data.repository

import ch.biketec.t.data.datasource.local.dao.TeamDao
import ch.biketec.t.data.mapper.toDomain
import ch.biketec.t.domain.entity.Team
import ch.biketec.t.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao
) : TeamRepository {

    override suspend fun getAllTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTeamById(teamId: String): Flow<Team?> {
        return teamDao.getTeamById(teamId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun searchTeams(query: String): Flow<List<Team>> {
        return teamDao.searchTeams(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getFavoriteTeams(): Flow<List<Team>> {
        return teamDao.getFavoriteTeams().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToFavorites(teamId: String): Result<Unit> {
        return try {
            teamDao.updateFavoriteStatus(teamId, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromFavorites(teamId: String): Result<Unit> {
        return try {
            teamDao.updateFavoriteStatus(teamId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTeams(): Result<Unit> {
        // TODO: Implementar sincronización con API remota
        return Result.success(Unit)
    }
}
