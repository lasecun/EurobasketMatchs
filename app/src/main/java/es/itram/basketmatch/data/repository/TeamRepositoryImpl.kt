package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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

    override fun getAllTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams().map { entities ->
            TeamMapper.toDomainList(entities)
        }
        // TODO: Implementar refresh en background
        // .onStart {
        //     // Intentar sincronizar desde la web si hay conexión
        //     try {
        //         refreshTeamsIfNeeded()
        //     } catch (e: Exception) {
        //         Log.w(TAG, "Error en refresh inicial, continuando con datos locales", e)
        //     }
        // }
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
     * Sincroniza equipos desde la web si es necesario
     */
    private suspend fun refreshTeamsIfNeeded() {
        if (!networkManager.isConnected()) {
            Log.d(TAG, "No hay conexión a internet, usando datos locales")
            return
        }
        
        try {
            Log.d(TAG, "Sincronizando equipos desde la web oficial de EuroLeague...")
            
            val remoteResult = remoteDataSource.getAllTeams()
            
            if (remoteResult.isSuccess) {
                val remoteTeams = remoteResult.getOrNull() ?: emptyList()
                if (remoteTeams.isNotEmpty()) {
                    // Convertir DTOs web a entidades de dominio
                    val domainTeams = TeamWebMapper.toDomainList(remoteTeams)
                    
                    // Convertir a entidades de base de datos y guardar
                    val entities = TeamMapper.fromDomainList(domainTeams)
                    teamDao.insertTeams(entities)
                    
                    Log.d(TAG, "Equipos sincronizados exitosamente: ${domainTeams.size}")
                } else {
                    Log.w(TAG, "No se obtuvieron equipos del scraping")
                }
            } else {
                Log.e(TAG, "Error en sincronización remota", remoteResult.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando equipos desde la web", e)
            // Continuar con datos locales en caso de error
        }
    }
}
