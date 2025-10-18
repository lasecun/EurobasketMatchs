package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
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
 * Repositorio de equipos - Temporada 2025-2026
 */
@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource,
    private val networkManager: NetworkManager
) : TeamRepository {

    companion object {
        private const val TAG = "TeamRepositoryImpl"
    }

    // Scope para operaciones de background que no deben bloquear el UI
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllTeams(): Flow<List<Team>> {
        Log.d(TAG, "📱 Obteniendo equipos temporada 2025-2026...")

        return teamDao.getAllTeams().map { entities ->
            Log.d(TAG, "✅ Equipos en BD local: ${entities.size}")
            TeamMapper.toDomainList(entities)
        }.onStart {
            if (networkManager.isConnected()) {
                backgroundScope.launch {
                    try {
                        val localTeamCount = teamDao.getTeamCount()

                        if (localTeamCount == 0) {
                            Log.d(TAG, "⚠️ Cache vacío, descargando equipos de E2026...")
                            syncTeamsFromApi()
                        } else {
                            Log.d(TAG, "✅ Cache disponible ($localTeamCount equipos)")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error en sincronización: ${e.message}")
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

    override fun getTeamByCode(teamCode: String): Flow<Team?> {
        return teamDao.getTeamByCode(teamCode).map { entity ->
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

    override suspend fun updateFavoriteStatus(teamId: String, isFavorite: Boolean) {
        teamDao.updateFavoriteStatus(teamId, isFavorite)
    }

    override suspend fun updateFavoriteStatusByCode(teamCode: String, isFavorite: Boolean) {
        teamDao.updateFavoriteStatusByCode(teamCode, isFavorite)
    }

    override suspend fun insertTeams(teams: List<Team>) {
        val entities = TeamMapper.fromDomainList(teams)
        teamDao.insertAll(entities)
    }

    override suspend fun updateTeam(team: Team) {
        val entity = TeamMapper.fromDomain(team)
        teamDao.updateTeam(entity)
    }

    override suspend fun deleteAllTeams() {
        teamDao.deleteAllTeams()
    }

    /**
     * Sincroniza equipos de la temporada 2025-2026 desde la API oficial
     */
    private suspend fun syncTeamsFromApi() {
        if (!networkManager.isConnected()) {
            Log.w(TAG, "Sin conexión a internet")
            return
        }

        try {
            Log.d(TAG, "🌐 Descargando equipos de temporada E2026 desde API...")

            val result = officialApiDataSource.getAllTeams()

            if (result.isSuccess) {
                val remoteTeams = result.getOrNull() ?: emptyList()
                if (remoteTeams.isNotEmpty()) {
                    Log.d(TAG, "✅ Equipos descargados: ${remoteTeams.size}")

                    val domainTeams = TeamWebMapper.toDomainList(remoteTeams)
                    val entities = TeamMapper.fromDomainList(domainTeams)
                    teamDao.insertAll(entities)

                    Log.d(TAG, "💾 Equipos guardados en BD local")
                } else {
                    Log.w(TAG, "⚠️ API devolvió lista vacía")
                }
            } else {
                Log.e(TAG, "❌ Error en API: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sincronizando: ${e.message}", e)
        }
    }
}
