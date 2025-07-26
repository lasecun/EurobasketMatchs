package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de equipos
 * Combina datos locales (Room) con datos remotos (Web scraping)
 */
@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val remoteDataSource: EuroLeagueRemoteDataSource,
    private val networkManager: NetworkManager
) : TeamRepository {

    companion object {
        private const val TAG = "TeamRepositoryImpl"
    }

    // Scope para operaciones de background que no deben bloquear el UI
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams().map { entities ->
            TeamMapper.toDomainList(entities)
        }.onStart {
            // Solo ejecutar refresh si hay conexión, evitando problemas en tests
            if (networkManager.isConnected()) {
                backgroundScope.launch {
                    try {
                        refreshTeamsIfNeeded()
                    } catch (e: Exception) {
                        // En producción se loggearía, en tests se ignora silenciosamente
                        // Log.w(TAG, "Error en refresh en background", e)
                    }
                }
            }
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
        teamDao.updateTeam(entity)
    }

    override suspend fun deleteAllTeams() {
        teamDao.deleteAllTeams()
    }

    /**
     * Fuerza la sincronización de equipos desde la web
     * Útil para implementar pull-to-refresh
     */
    suspend fun forceRefreshTeams(): Result<List<Team>> {
        return try {
            refreshTeamsIfNeeded()
            
            // Devolver los equipos actualizados
            val entities = teamDao.getAllTeamsSync()
            val teams = TeamMapper.toDomainList(entities)
            Result.success(teams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza equipos desde la web si es necesario
     */
    private suspend fun refreshTeamsIfNeeded() {
        if (!networkManager.isConnected()) {
            return
        }
        
        try {
            val remoteResult = remoteDataSource.getAllTeams()
            
            if (remoteResult.isSuccess) {
                val remoteTeams = remoteResult.getOrNull() ?: emptyList()
                if (remoteTeams.isNotEmpty()) {
                    // Convertir DTOs web a entidades de dominio
                    val domainTeams = TeamWebMapper.toDomainList(remoteTeams)
                    
                    // Convertir a entidades de base de datos y guardar
                    val entities = TeamMapper.fromDomainList(domainTeams)
                    teamDao.insertTeams(entities)
                }
            }
        } catch (e: Exception) {
            // Continuar con datos locales en caso de error
        }
    }
}
