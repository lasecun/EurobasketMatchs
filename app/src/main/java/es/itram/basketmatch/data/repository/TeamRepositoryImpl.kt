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
        Log.d(TAG, "📱 [LOCAL] Iniciando obtención de equipos desde cache local...")
        return teamDao.getAllTeams().map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Equipos obtenidos desde BD local: ${entities.size}")
            TeamMapper.toDomainList(entities)
        }.onStart {
            // Solo ejecutar refresh si la BD local está vacía y hay conexión
            if (networkManager.isConnected()) {
                backgroundScope.launch {
                    try {
                        val localTeamCount = teamDao.getTeamCount()
                        Log.d(TAG, "📱 [LOCAL] Verificando cache: $localTeamCount equipos en BD local")
                        
                        if (localTeamCount == 0) {
                            Log.d(TAG, "📱 [LOCAL] ⚠️ Cache vacío, iniciando descarga desde API...")
                            refreshTeamsIfNeeded()
                        } else {
                            Log.d(TAG, "📱 [LOCAL] ✅ Cache disponible, usando datos locales")
                        }
                    } catch (e: Exception) {
                        // En producción se loggearía, en tests se ignora silenciosamente
                        // Log.w(TAG, "Error en refresh en background", e)
                    }
                }
            }
        }
    }

    override fun getTeamById(teamId: String): Flow<Team?> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo equipo específico desde BD local: $teamId")
        return teamDao.getTeamById(teamId).map { entity ->
            if (entity != null) {
                Log.d(TAG, "📱 [LOCAL] ✅ Equipo encontrado: ${entity.name}")
            } else {
                Log.d(TAG, "📱 [LOCAL] ⚠️ Equipo no encontrado: $teamId")
            }
            entity?.let { TeamMapper.toDomain(it) }
        }
    }

    override fun getTeamsByCountry(country: String): Flow<List<Team>> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo equipos por país desde BD local: $country")
        return teamDao.getTeamsByCountry(country).map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Equipos encontrados para $country: ${entities.size}")
            TeamMapper.toDomainList(entities)
        }
    }

    override fun getFavoriteTeams(): Flow<List<Team>> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo equipos favoritos desde BD local...")
        return teamDao.getFavoriteTeams().map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Equipos favoritos obtenidos: ${entities.size}")
            TeamMapper.toDomainList(entities)
        }
    }

    override suspend fun insertTeams(teams: List<Team>) {
        Log.d(TAG, "💾 [SAVE] Guardando ${teams.size} equipos en BD local...")
        val entities = TeamMapper.fromDomainList(teams)
        teamDao.insertTeams(entities)
        Log.d(TAG, "💾 [SAVE] ✅ Equipos guardados en BD local")
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
     * Reemplaza completamente todos los datos con datos reales frescos
     * Útil para migrar de datos mockeados a datos reales
     */
    suspend fun replaceAllWithRealData(): Result<List<Team>> {
        return try {
            Log.d(TAG, "🔄 Reemplazando todos los datos con datos reales...")
            
            // 1. Obtener datos reales
            val remoteResult = remoteDataSource.getAllTeams()
            
            if (remoteResult.isSuccess) {
                val remoteTeams = remoteResult.getOrNull() ?: emptyList()
                
                if (remoteTeams.isNotEmpty()) {
                    Log.d(TAG, "📊 Obtenidos ${remoteTeams.size} equipos reales de la web")
                    
                    // 2. Borrar todos los datos existentes
                    teamDao.deleteAllTeams()
                    Log.d(TAG, "🗑️ Datos anteriores eliminados")
                    
                    // 3. Convertir y guardar datos reales
                    val domainTeams = TeamWebMapper.toDomainList(remoteTeams)
                    val entities = TeamMapper.fromDomainList(domainTeams)
                    teamDao.insertTeams(entities)
                    
                    Log.d(TAG, "✅ ${domainTeams.size} equipos reales guardados")
                    
                    Result.success(domainTeams)
                } else {
                    Log.w(TAG, "⚠️ No se obtuvieron equipos reales")
                    Result.failure(Exception("No se obtuvieron equipos reales"))
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo datos remotos")
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en replaceAllWithRealData: ${e.message}", e)
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
