package es.itram.basketmatch.domain.service

import android.content.Context
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
    private val context: Context
) {
    
    companion object {
        private const val TAG = "DataSyncService"
        private const val PREFS_NAME = "euroleague_data_prefs"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 horas
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Verifica si es necesario sincronizar datos
     */
    suspend fun isSyncNeeded(): Boolean {
        return withContext(Dispatchers.IO) {
            val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
            if (isFirstLaunch) {
                Log.d(TAG, "🆕 Primera vez que se abre la aplicación - sync necesario")
                return@withContext true
            }
            
            val lastSync = prefs.getLong(KEY_LAST_SYNC, 0)
            val currentTime = System.currentTimeMillis()
            val timeSinceLastSync = currentTime - lastSync
            
            val needsSync = timeSinceLastSync > SYNC_INTERVAL_MS
            
            Log.d(TAG, if (needsSync) {
                "⏰ Han pasado ${timeSinceLastSync / (60 * 60 * 1000)}h desde la última sync - sync necesario"
            } else {
                "✅ Datos actuales - última sync hace ${timeSinceLastSync / (60 * 60 * 1000)}h"
            })
            
            needsSync
        }
    }
    
    /**
     * Sincroniza todos los datos desde la API JSON
     */
    suspend fun syncAllData(): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Iniciando sincronización completa de datos...")
                
                // 1. Obtener equipos desde JSON API
                Log.d(TAG, "🏀 Obteniendo equipos...")
                val teamsFromApi = jsonApiScraper.getTeams()
                
                if (teamsFromApi.isEmpty()) {
                    Log.e(TAG, "❌ No se pudieron obtener equipos")
                    return@withContext Result.failure(Exception("No se pudieron obtener equipos"))
                }
                
                // 2. Obtener partidos desde JSON API
                Log.d(TAG, "⚽ Obteniendo partidos...")
                val matchesFromApi = jsonApiScraper.getMatches("2025-26")
                
                if (matchesFromApi.isEmpty()) {
                    Log.e(TAG, "❌ No se pudieron obtener partidos")
                    return@withContext Result.failure(Exception("No se pudieron obtener partidos"))
                }
                
                // 3. Limpiar base de datos local
                Log.d(TAG, "🗑️ Limpiando datos locales...")
                teamDao.deleteAllTeams()
                matchDao.deleteAllMatches()
                
                // 4. Insertar equipos en base de datos local
                Log.d(TAG, "💾 Guardando ${teamsFromApi.size} equipos...")
                val teamDomains = teamsFromApi.map { teamMapper.toDomain(it) }
                val teamEntities = teamDomains.map { TeamMapper.fromDomain(it) }
                teamDao.insertTeams(teamEntities)
                
                // 5. Insertar partidos en base de datos local
                Log.d(TAG, "💾 Guardando ${matchesFromApi.size} partidos...")
                val matchDomains = matchesFromApi.map { matchMapper.toDomain(it) }
                val matchEntities = matchDomains.map { MatchMapper.fromDomain(it) }
                matchDao.insertMatches(matchEntities)
                
                // 6. Actualizar timestamp de sincronización
                val currentTime = System.currentTimeMillis()
                prefs.edit()
                    .putLong(KEY_LAST_SYNC, currentTime)
                    .putBoolean(KEY_FIRST_LAUNCH, false)
                    .apply()
                
                val result = SyncResult(
                    teamsCount = teamsFromApi.size,
                    matchesCount = matchesFromApi.size,
                    syncTimestamp = currentTime
                )
                
                Log.d(TAG, "✅ Sincronización completada: ${result.teamsCount} equipos, ${result.matchesCount} partidos")
                
                Result.success(result)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error durante la sincronización", e)
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
