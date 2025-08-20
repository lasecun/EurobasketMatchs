package es.itram.basketmatch.data.datasource.local.assets

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para datos estáticos pre-cargados en la aplicación
 * 
 * ARQUITECTURA DE DATOS ESTÁTICOS:
 * - Equipos, calendario de partidos, venues → ESTÁTICOS (rara vez cambian)
 * - Resultados, estadísticas, standings → DINÁMICOS (se sincronizan)
 * 
 * Beneficios:
 * - ⚡ Rendimiento: Sin llamadas de red constantes
 * - 🔋 Batería: Menos sincronización
 * - 📱 Offline: Datos siempre disponibles
 * - 🎯 Control: Actualizaciones manuales
 */
@Singleton
class StaticDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    
    companion object {
        private const val TAG = "StaticDataManager"
        private const val ASSETS_PATH = "static_data"
        private const val TEAMS_FILE = "teams_2025_26.json"
        private const val MATCHES_FILE = "matches_calendar_2025_26.json"
        private const val VERSION_FILE = "data_version.json"
    }
    
    /**
     * Carga los equipos estáticos desde assets o archivos internos
     */
    suspend fun loadStaticTeams(): Result<StaticTeamsData> = withContext(Dispatchers.IO) {
        try {
            // Primero intentar cargar desde archivos internos (generados)
            val internalFile = File(context.filesDir, "$ASSETS_PATH/$TEAMS_FILE")
            val jsonString = if (internalFile.exists()) {
                Log.d(TAG, "📱 Loading teams from internal generated file")
                internalFile.readText()
            } else {
                Log.d(TAG, "📦 Loading teams from assets")
                readAssetFile(TEAMS_FILE)
            }
            
            val teamsData = json.decodeFromString<StaticTeamsData>(jsonString)
            Log.d(TAG, "✅ Loaded ${teamsData.teams.size} static teams")
            Result.success(teamsData)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading static teams", e)
            Result.failure(e)
        }
    }
    
    /**
     * Carga el calendario de partidos estático desde assets o archivos internos
     */
    suspend fun loadStaticMatches(): Result<StaticMatchesData> = withContext(Dispatchers.IO) {
        try {
            // Primero intentar cargar desde archivos internos (generados)
            val internalFile = File(context.filesDir, "$ASSETS_PATH/$MATCHES_FILE")
            val jsonString = if (internalFile.exists()) {
                Log.d(TAG, "📱 Loading matches from internal generated file")
                internalFile.readText()
            } else {
                Log.d(TAG, "📦 Loading matches from assets")
                readAssetFile(MATCHES_FILE)
            }
            
            val matchesData = json.decodeFromString<StaticMatchesData>(jsonString)
            Log.d(TAG, "✅ Loaded ${matchesData.matches.size} static matches")
            Result.success(matchesData)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading static matches", e)
            Result.failure(e)
        }
    }
    
    /**
     * Carga la información de versión de datos estáticos
     */
    suspend fun loadDataVersion(): Result<DataVersionInfo> = withContext(Dispatchers.IO) {
        try {
            val jsonString = readAssetFile(VERSION_FILE)
            val versionData = json.decodeFromString<DataVersionInfo>(jsonString)
            Log.d(TAG, "✅ Loaded data version info: ${versionData.version}")
            Result.success(versionData)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading data version", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verifica si los datos estáticos están actualizados
     */
    suspend fun areStaticDataUpdated(): Boolean {
        return try {
            val versionInfo = loadDataVersion().getOrNull()
            versionInfo?.syncConfig?.manualSyncOnly == true
        } catch (e: Exception) {
            Log.w(TAG, "Could not verify static data version", e)
            false
        }
    }
    
    /**
     * Lee un archivo desde assets
     */
    private fun readAssetFile(fileName: String): String {
        return try {
            context.assets.open("$ASSETS_PATH/$fileName").use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading asset file: $fileName", e)
            throw e
        }
    }
}

/**
 * Modelos de datos para JSON estático
 */
@Serializable
data class StaticTeamsData(
    val version: String,
    val lastUpdated: String,
    val teams: List<StaticTeam>
)

@Serializable
data class StaticTeam(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String,
    val primaryColor: String = "#000000",
    val secondaryColor: String = "#FFFFFF",
    val country: String = "",
    val city: String = "",
    val venue: String = "",
    val website: String = "",
    val president: String = "",
    val phone: String = "",
    val address: String = "",
    val twitterAccount: String = "",
    val ticketsUrl: String = "",
    // Campos legacy mantenidos para compatibilidad
    val code: String = id,
    val founded: Int = 0,
    val coach: String = ""
)

@Serializable
data class StaticMatchesData(
    val version: String,
    val lastUpdated: String,
    val season: String,
    val totalRounds: Int,
    val description: String,
    val note: String,
    val matches: List<StaticMatch>
)

@Serializable
data class StaticMatch(
    val id: String,
    val round: Int,
    val homeTeamCode: String,
    val awayTeamCode: String,
    val venue: String,
    val season: String,
    val status: String,
    val dateTime: String,
    val homeScore: Int?,
    val awayScore: Int?
)

@Serializable
data class DataVersionInfo(
    val version: String,
    val lastUpdated: String,
    val description: String,
    val staticDataVersions: StaticDataVersions,
    val lastStaticDataUpdate: String,
    val dynamicDataVersions: DynamicDataVersions,
    val syncConfig: SyncConfig
)

@Serializable
data class StaticDataVersions(
    val teams: String,
    val matches_calendar: String,
    val venues: String
)

@Serializable
data class DynamicDataVersions(
    val match_results: String,
    val standings: String,
    val player_stats: String
)

@Serializable
data class SyncConfig(
    val enableAutoSync: Boolean,
    val autoSyncInterval: Int,
    val manualSyncOnly: Boolean,
    val syncOnlyDynamicData: Boolean
)
