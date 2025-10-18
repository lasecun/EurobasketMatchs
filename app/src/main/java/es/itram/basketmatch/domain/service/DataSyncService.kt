package es.itram.basketmatch.domain.service

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.data.mapper.TeamMapper
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio de sincronización - Temporada 2025-2026 (E2025)
 *
 * Funcionalidades:
 * ✅ Descargar equipos de E2025 (temporada 2025-2026) si no existen localmente
 * ✅ Descargar las 38 jornadas de E2025 si no existen localmente
 * ✅ Solo API oficial, sin web scraping
 */
@Singleton
class DataSyncService @Inject constructor(
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource,
    private val teamDao: TeamDao,
    private val matchDao: MatchDao,
    private val prefs: SharedPreferences
) {

    companion object {
        private const val TAG = "DataSyncService"
        private const val PREF_LAST_SYNC = "last_sync_timestamp"
        private const val PREF_TEAMS_SYNCED = "teams_synced"
        private const val PREF_MATCHES_SYNCED = "matches_synced"
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Inicializa los datos de la app al arrancar
     */
    suspend fun initializeAppData() {
        Log.d(TAG, "🚀 Inicializando datos temporada 2025-2026 (E2025)...")

        try {
            initializeTeams()
            initializeMatches()

            Log.d(TAG, "✅ Inicialización completada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en inicialización: ${e.message}", e)
        }
    }

    /**
     * Descarga equipos si no existen localmente
     */
    private suspend fun initializeTeams() {
        val localTeamsCount = teamDao.getTeamCount()

        if (localTeamsCount == 0) {
            Log.d(TAG, "📥 Descargando equipos de E2025...")

            val result = officialApiDataSource.getAllTeams()
            if (result.isSuccess) {
                val teamDtos = result.getOrNull() ?: emptyList()
                if (teamDtos.isNotEmpty()) {
                    val domainTeams = TeamWebMapper.toDomainList(teamDtos)
                    val entities = TeamMapper.fromDomainList(domainTeams)
                    teamDao.insertAll(entities)

                    prefs.edit {
                        putBoolean(PREF_TEAMS_SYNCED, true)
                        putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                    }

                    Log.d(TAG, "✅ ${teamDtos.size} equipos sincronizados")
                }
            }
        } else {
            Log.d(TAG, "✅ Equipos ya disponibles: $localTeamsCount")
        }
    }

    /**
     * Descarga partidos (38 jornadas) y ACTUALIZA siempre con datos frescos de la API
     */
    private suspend fun initializeMatches() {
        Log.d(TAG, "📥 Actualizando partidos de E2025 desde API...")

        val result = officialApiDataSource.getAllMatches()
        if (result.isSuccess) {
            val matchDtos = result.getOrNull() ?: emptyList()
            if (matchDtos.isNotEmpty()) {
                val domainMatches = MatchWebMapper.toDomainList(matchDtos)
                val entities = MatchMapper.fromDomainList(domainMatches)

                // Usar REPLACE para actualizar automáticamente los partidos existentes
                // Esto actualiza logos, estados y marcadores sin perder otros datos
                matchDao.insertMatches(entities)

                prefs.edit {
                    putBoolean(PREF_MATCHES_SYNCED, true)
                    putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                }

                Log.d(TAG, "✅ ${matchDtos.size} partidos actualizados (logos, estados y marcadores)")
            }
        } else {
            Log.w(TAG, "⚠️ No se pudieron obtener partidos de la API")

            // Si falla la API, verificar si hay datos locales
            val localMatchesCount = matchDao.getMatchCount()
            if (localMatchesCount > 0) {
                Log.d(TAG, "ℹ️ Usando ${localMatchesCount} partidos en caché")
            }
        }
    }

    /**
     * Fuerza una sincronización completa
     */
    suspend fun forceSync() {
        Log.d(TAG, "🔄 Forzando sincronización...")

        prefs.edit {
            putBoolean(PREF_TEAMS_SYNCED, false)
            putBoolean(PREF_MATCHES_SYNCED, false)
        }

        initializeAppData()
    }
}
