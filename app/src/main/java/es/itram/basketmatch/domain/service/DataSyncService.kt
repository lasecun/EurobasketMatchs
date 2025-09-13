package es.itram.basketmatch.domain.service

import android.content.SharedPreferences
import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio centralizado para gestionar la sincronización de datos de EuroLeague
 */
@Singleton
class DataSyncService @Inject constructor(
    private val jsonApiScraper: EuroLeagueJsonApiScraper,
    private val teamDao: TeamDao,
    private val matchDao: MatchDao,
    private val teamMapper: TeamWebMapper,
    private val matchMapper: MatchWebMapper,
    private val prefs: SharedPreferences
) {
    
    companion object {
        private const val TAG = "DataSyncService"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_DATA_POPULATED = "data_populated"
        private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 horas
    }
    
    // Estado del progreso de sincronización
    private val _syncProgress = MutableStateFlow(SyncProgress())
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()
    
    data class SyncProgress(
        val isLoading: Boolean = false,
        val currentRound: Int = 0,
        val totalRounds: Int = 38,
        val message: String = ""
    )
    
    /**
     * Verifica si es necesario sincronizar datos
     */
    suspend fun isSyncNeeded(): Boolean {
        return withContext(Dispatchers.IO) {
            val isDataPopulated = prefs.getBoolean(KEY_DATA_POPULATED, false)
            val matchCount = matchDao.getMatchCount()
            
            Log.d(TAG, "🔍 Verificando necesidad de sync:")
            Log.d(TAG, "   - DATA_POPULATED flag: $isDataPopulated")
            Log.d(TAG, "   - Matches en BD: $matchCount")
            
            if (!isDataPopulated || matchCount == 0) {
                Log.d(TAG, "🆕 Base de datos vacía - sync necesario")
                return@withContext true
            }
            
            val lastSync = prefs.getLong(KEY_LAST_SYNC, 0)
            val currentTime = System.currentTimeMillis()
            val timeSinceLastSync = currentTime - lastSync
            val hoursSinceLastSync = timeSinceLastSync / (1000 * 60 * 60)
            
            Log.d(TAG, "   - Última sync: ${if (lastSync == 0L) "nunca" else "$hoursSinceLastSync horas atrás"}")
            
            if (timeSinceLastSync > SYNC_INTERVAL_MS) {
                Log.d(TAG, "⏰ Hace más de 24h desde última sync - sync necesario")
                return@withContext true
            }
            
            Log.d(TAG, "✅ Datos locales disponibles y actualizados")
            false
        }
    }
    
    /**
     * Sincroniza todos los datos desde la API JSON con progreso
     */
    suspend fun syncAllData(): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Iniciando sincronización completa de datos...")
                
                _syncProgress.value = SyncProgress(
                    isLoading = true,
                    currentRound = 0,
                    totalRounds = 38,
                    message = "Preparando sincronización..."
                )
                
                // 1. Obtener equipos desde JSON API
                Log.d(TAG, "🏀 Obteniendo equipos...")
                _syncProgress.value = _syncProgress.value.copy(
                    message = "Obteniendo información de equipos..."
                )
                
                val teamsFromApi = jsonApiScraper.getTeams()
                
                if (teamsFromApi.isEmpty()) {
                    _syncProgress.value = SyncProgress()
                    Log.e(TAG, "❌ No se pudieron obtener equipos")
                    return@withContext Result.failure(Exception("No se pudieron obtener equipos"))
                }
                
                // 2. Obtener partidos desde JSON API con progreso
                Log.d(TAG, "⚽ Obteniendo partidos...")
                val matchesFromApi = jsonApiScraper.getMatchesWithProgress { current, total ->
                    _syncProgress.value = SyncProgress(
                        isLoading = true,
                        currentRound = current,
                        totalRounds = total,
                        message = "Importando jornada $current de $total..."
                    )
                }
                
                if (matchesFromApi.isEmpty()) {
                    _syncProgress.value = SyncProgress()
                    Log.e(TAG, "❌ No se pudieron obtener partidos")
                    return@withContext Result.failure(Exception("No se pudieron obtener partidos"))
                }
                
                // 3. Limpiar base de datos local
                Log.d(TAG, "🗑️ Limpiando datos locales...")
                _syncProgress.value = _syncProgress.value.copy(
                    message = "Preparando base de datos..."
                )
                teamDao.deleteAllTeams()
                matchDao.deleteAllMatches()
                
                // 4. Insertar equipos en base de datos local
                Log.d(TAG, "💾 Guardando ${teamsFromApi.size} equipos...")
                _syncProgress.value = _syncProgress.value.copy(
                    message = "Guardando equipos..."
                )
                val teamDomains = teamsFromApi.map { teamMapper.toDomain(it) }
                val teamEntities = teamDomains.map { TeamMapper.fromDomain(it) }
                teamDao.insertTeams(teamEntities)
                
                // 5. Insertar partidos en base de datos local
                Log.d(TAG, "💾 Guardando ${matchesFromApi.size} partidos...")
                _syncProgress.value = _syncProgress.value.copy(
                    message = "Guardando partidos en base de datos..."
                )
                val matchDomains = matchesFromApi.map { matchMapper.toDomain(it) }
                val matchEntities = matchDomains.map { MatchMapper.fromDomain(it) }
                matchDao.insertMatches(matchEntities)
                
                // 6. Actualizar timestamp de sincronización
                val currentTime = System.currentTimeMillis()
                prefs.edit()
                    .putLong(KEY_LAST_SYNC, currentTime)
                    .putBoolean(KEY_FIRST_LAUNCH, false)
                    .putBoolean(KEY_DATA_POPULATED, true)
                    .apply()
                
                _syncProgress.value = SyncProgress() // Reset loading state
                
                val result = SyncResult(
                    teamsCount = teamsFromApi.size,
                    matchesCount = matchesFromApi.size,
                    syncTimestamp = currentTime
                )
                
                Log.d(TAG, "✅ Sincronización completada: ${result.teamsCount} equipos, ${result.matchesCount} partidos")
                
                Result.success(result)
                
            } catch (e: Exception) {
                _syncProgress.value = SyncProgress() // Reset loading state
                Log.e(TAG, "❌ Error durante la sincronización", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Refresca los datos de una jornada específica
     */
    suspend fun refreshRound(round: Int): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Refrescando jornada $round...")
                
                val matchesFromApi = jsonApiScraper.getMatchesForRoundRefresh(round, "2025-26")
                
                if (matchesFromApi.isEmpty()) {
                    Log.w(TAG, "⚠️ No se obtuvieron partidos para la jornada $round")
                    return@withContext Result.failure(Exception("No se obtuvieron partidos para la jornada $round"))
                }
                
                // Actualizar solo los partidos de esta jornada
                val matchDomains = matchesFromApi.map { matchMapper.toDomain(it) }
                val matchEntities = matchDomains.map { MatchMapper.fromDomain(it) }
                matchDao.insertMatches(matchEntities) // REPLACE strategy actualiza automáticamente
                
                Log.d(TAG, "✅ Jornada $round refrescada: ${matchesFromApi.size} partidos actualizados")
                
                Result.success(matchesFromApi.size)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error refrescando jornada $round", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Fuerza la sincronización independientemente del tiempo transcurrido
     */
    suspend fun forceSyncData(): Result<SyncResult> {
        Log.d(TAG, "🔄 Sincronización forzada solicitada")
        return syncAllData()
    }
    
    /**
     * Obtiene información sobre la última sincronización
     */
    fun getLastSyncInfo(): SyncInfo {
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        
        return SyncInfo(
            lastSyncTimestamp = lastSync,
            isFirstLaunch = isFirstLaunch,
            timeSinceLastSync = if (lastSync > 0) System.currentTimeMillis() - lastSync else 0
        )
    }
}

/**
 * Resultado de una operación de sincronización
 */
data class SyncResult(
    val teamsCount: Int,
    val matchesCount: Int,
    val syncTimestamp: Long
)

/**
 * Información sobre el estado de sincronización
 */
data class SyncInfo(
    val lastSyncTimestamp: Long,
    val isFirstLaunch: Boolean,
    val timeSinceLastSync: Long
)
