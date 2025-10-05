package es.itram.basketmatch.domain.service

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Team
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🏀 Servicio de sincronización simplificado - SOLO API OFICIAL
 *
 * Funcionalidades:
 * ✅ Verificar datos locales al arrancar
 * ✅ Obtener equipos y calendario de API oficial si no existen
 * ✅ Enriquecer partidos con estadísticas en hilo secundario
 * ✅ Eliminado completamente scraper web
 */
@Singleton
class DataSyncService @Inject constructor(
    private val euroLeagueRemoteDataSource: EuroLeagueRemoteDataSource,
    private val teamDao: TeamDao,
    private val matchDao: MatchDao,
    private val prefs: SharedPreferences
) {

    companion object {
        private const val TAG = "DataSyncService"
        private const val PREF_LAST_SYNC = "last_sync_timestamp"
        private const val PREF_TEAMS_SYNCED = "teams_synced"
        private const val PREF_MATCHES_SYNCED = "matches_synced"
        private const val SYNC_INTERVAL_HOURS = 6 // Sincronizar cada 6 horas
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Inicialización al arrancar la aplicación
     * Verifica datos locales y obtiene de API si es necesario
     */
    suspend fun initializeAppData() {
        Log.d(TAG, "🚀 Inicializando datos de la aplicación...")

        try {
            // 1. Verificar y obtener equipos
            initializeTeams()

            // 2. Verificar y obtener calendario
            initializeMatches()

            // 3. Enriquecer partidos en segundo plano
            enrichMatchesInBackground()

            Log.d(TAG, "✅ Inicialización completada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en inicialización: ${e.message}", e)
        }
    }

    /**
     * Verifica equipos en base de datos local y los obtiene de API si no existen
     */
    private suspend fun initializeTeams() {
        Log.d(TAG, "🏀 Verificando equipos locales...")

        val localTeamsCount = teamDao.getTeamCount()
        val teamsAlreadySynced = prefs.getBoolean(PREF_TEAMS_SYNCED, false)

        if (localTeamsCount == 0 || !teamsAlreadySynced || shouldSync()) {
            Log.d(TAG, "📥 Obteniendo equipos desde API oficial...")

            val result = euroLeagueRemoteDataSource.getAllTeams()
            if (result.isSuccess) {
                val teamDtos = result.getOrNull() ?: emptyList()
                if (teamDtos.isNotEmpty()) {
                    // Convertir DTOs web a entidades de dominio y luego a entidades de BD
                    val teamEntities = mutableListOf<TeamEntity>()
                    teamDtos.forEach { teamDto ->
                        val domainTeam = es.itram.basketmatch.data.mapper.TeamWebMapper.toDomain(teamDto)
                        teamEntities.add(convertTeamToEntity(domainTeam))
                    }
                    teamDao.insertAll(teamEntities)

                    // Marcar como sincronizado
                    prefs.edit {
                        putBoolean(PREF_TEAMS_SYNCED, true)
                        putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                    }

                    Log.d(TAG, "✅ ${teamDtos.size} equipos sincronizados correctamente")
                } else {
                    Log.w(TAG, "⚠️ No se obtuvieron equipos de la API")
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo equipos: ${result.exceptionOrNull()?.message}")
            }
        } else {
            Log.d(TAG, "✅ Equipos ya disponibles localmente ($localTeamsCount equipos)")
        }
    }

    /**
     * Verifica partidos en base de datos local y los obtiene de API si no existen
     */
    private suspend fun initializeMatches() {
        Log.d(TAG, "⚽ Verificando partidos locales...")

        val localMatchesCount = matchDao.getMatchCount()
        val matchesAlreadySynced = prefs.getBoolean(PREF_MATCHES_SYNCED, false)

        if (localMatchesCount == 0 || !matchesAlreadySynced || shouldSync()) {
            Log.d(TAG, "📥 Obteniendo calendario desde API oficial...")

            val result = euroLeagueRemoteDataSource.getAllMatches()
            if (result.isSuccess) {
                val matchDtos = result.getOrNull() ?: emptyList()
                if (matchDtos.isNotEmpty()) {
                    // Convertir DTOs web a entidades de dominio y luego a entidades de BD
                    val matchEntities = mutableListOf<MatchEntity>()
                    matchDtos.forEach { matchDto ->
                        val domainMatch = es.itram.basketmatch.data.mapper.MatchWebMapper.toDomain(matchDto)
                        matchEntities.add(convertMatchToEntity(domainMatch))
                    }
                    matchDao.insertAll(matchEntities)

                    // Marcar como sincronizado
                    prefs.edit {
                        putBoolean(PREF_MATCHES_SYNCED, true)
                        putLong(PREF_LAST_SYNC, System.currentTimeMillis())
                    }

                    Log.d(TAG, "✅ ${matchDtos.size} partidos sincronizados correctamente")
                } else {
                    Log.w(TAG, "⚠️ No se obtuvieron partidos de la API")
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo partidos: ${result.exceptionOrNull()?.message}")
            }
        } else {
            Log.d(TAG, "✅ Partidos ya disponibles localmente ($localMatchesCount partidos)")
        }
    }

    /**
     * Enriquece partidos con estadísticas y resultados en hilo secundario
     * Nota: getMatchDetails devuelve MatchWebDto, no Match
     */
    private fun enrichMatchesInBackground() {
        Log.d(TAG, "🔄 Iniciando enriquecimiento de partidos en segundo plano...")

        coroutineScope.launch {
            try {
                // Obtener partidos que necesitan enriquecimiento
                val matchesToEnrich = matchDao.getMatchesWithoutDetails()

                Log.d(TAG, "📊 Enriqueciendo ${matchesToEnrich.size} partidos...")

                matchesToEnrich.forEach { match ->
                    try {
                        // Obtener detalles del partido desde API (devuelve MatchWebDto)
                        val result = euroLeagueRemoteDataSource.getMatchDetails(match.id)

                        if (result.isSuccess) {
                            val matchWebDto = result.getOrNull()
                            if (matchWebDto != null) {
                                // Convertir DTO a entidad de dominio y luego a entidad de BD
                                val enrichedMatch = convertWebDtoToDomain(matchWebDto)
                                val updatedEntity = convertMatchToEntity(enrichedMatch)
                                matchDao.update(updatedEntity)

                                Log.d(TAG, "✅ Partido enriquecido: ${enrichedMatch.homeTeamName} vs ${enrichedMatch.awayTeamName}")
                            }
                        }

                        // Pausa para no sobrecargar la API
                        delay(500)

                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Error enriqueciendo partido ${match.id}: ${e.message}")
                    }
                }

                Log.d(TAG, "✅ Enriquecimiento de partidos completado")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en enriquecimiento de partidos: ${e.message}", e)
            }
        }
    }

    /**
     * Sincronización manual forzada
     */
    suspend fun forceSyncAll() {
        Log.d(TAG, "🔄 Iniciando sincronización manual forzada...")

        // Limpiar flags de sincronización
        prefs.edit {
            putBoolean(PREF_TEAMS_SYNCED, false)
            putBoolean(PREF_MATCHES_SYNCED, false)
        }

        // Ejecutar inicialización completa
        initializeAppData()
    }

    /**
     * Sincronización solo de partidos (para actualizaciones frecuentes)
     */
    suspend fun syncMatchesOnly() {
        Log.d(TAG, "⚽ Sincronizando solo partidos...")

        prefs.edit { putBoolean(PREF_MATCHES_SYNCED, false) }
        initializeMatches()
        enrichMatchesInBackground()
    }

    /**
     * Verifica si es necesario sincronizar basado en tiempo transcurrido
     */
    private fun shouldSync(): Boolean {
        val lastSync = prefs.getLong(PREF_LAST_SYNC, 0)
        val now = System.currentTimeMillis()
        val hoursSinceLastSync = (now - lastSync) / (1000 * 60 * 60)

        return hoursSinceLastSync >= SYNC_INTERVAL_HOURS
    }

    /**
     * Obtiene estado de sincronización para mostrar en UI
     */
    fun getSyncStatus(): SyncStatus {
        val lastSync = prefs.getLong(PREF_LAST_SYNC, 0)
        val teamsCount = teamDao.getTeamCountSync()
        val matchesCount = matchDao.getMatchCountSync()

        return SyncStatus(
            lastSyncTime = if (lastSync > 0) Date(lastSync) else null,
            teamsCount = teamsCount,
            matchesCount = matchesCount,
            isDataAvailable = teamsCount > 0 && matchesCount > 0
        )
    }

    /**
     * Limpia recursos al cerrar la aplicación
     */
    fun cleanup() {
        coroutineScope.cancel()
        Log.d(TAG, "🧹 Recursos de sincronización liberados")
    }

    /**
     * Función para convertir Team de dominio a TeamEntity
     */
    private fun convertTeamToEntity(team: Team): TeamEntity {
        return TeamEntity(
            id = team.id,
            name = team.name,
            shortName = team.shortName,
            code = team.code,
            city = team.city,
            country = team.country,
            founded = team.founded,
            coach = team.coach,
            logoUrl = team.logoUrl,
            isFavorite = team.isFavorite
        )
    }

    /**
     * Función para convertir Match de dominio a MatchEntity
     */
    private fun convertMatchToEntity(match: Match): MatchEntity {
        return MatchEntity(
            id = match.id,
            homeTeamId = match.homeTeamId,
            homeTeamName = match.homeTeamName,
            homeTeamLogo = match.homeTeamLogo,
            awayTeamId = match.awayTeamId,
            awayTeamName = match.awayTeamName,
            awayTeamLogo = match.awayTeamLogo,
            dateTime = match.dateTime,
            venue = match.venue,
            round = match.round,
            status = match.status,
            homeScore = match.homeScore,
            awayScore = match.awayScore,
            seasonType = match.seasonType
        )
    }

    /**
     * Función para convertir MatchWebDto a Match de dominio
     * Reutiliza la lógica del MatchWebMapper
     */
    private fun convertWebDtoToDomain(dto: es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto): Match {
        // Importar el mapper y usarlo
        return es.itram.basketmatch.data.mapper.MatchWebMapper.toDomain(dto)
    }

    /**
     * Extension function para convertir Team de dominio a TeamEntity
     */
    private fun Team.toEntity(): TeamEntity = convertTeamToEntity(this)

    /**
     * Extension function para convertir Match de dominio a MatchEntity
     */
    private fun Match.toEntity(): MatchEntity = convertMatchToEntity(this)
}

/**
 * Estado de sincronización para mostrar en UI
 */
data class SyncStatus(
    val lastSyncTime: Date?,
    val teamsCount: Int,
    val matchesCount: Int,
    val isDataAvailable: Boolean
)
