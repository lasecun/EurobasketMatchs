package es.itram.basketmatch.data.generator

import android.content.Context
import android.util.Log
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.datasource.local.assets.StaticMatch
import es.itram.basketmatch.data.datasource.local.assets.StaticMatchesData
import es.itram.basketmatch.data.datasource.local.assets.StaticTeam
import es.itram.basketmatch.data.datasource.local.assets.StaticTeamsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generador de datos estáticos - Temporada 2025-2026
 *
 * ✅ Solo API oficial de EuroLeague (E2026)
 * ✅ Sin web scraping
 * ✅ Datos oficiales y confiables
 */
@Singleton
class StaticDataGenerator @Inject constructor(
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "StaticDataGenerator"
        private const val STATIC_DATA_DIR = "static_data"
        private const val TEAMS_FILE = "teams_2025_26.json"
        private const val MATCHES_FILE = "matches_calendar_2025_26.json"

        private val json = Json { prettyPrint = true }
    }

    /**
     * Genera todos los datos estáticos desde la API oficial
     */
    suspend fun generateAllStaticData(): Result<GenerationResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏗️ Generando datos estáticos temporada 2025-2026...")

            // 1. Obtener equipos desde API oficial
            val teamsResult = officialApiDataSource.getAllTeams()
            if (!teamsResult.isSuccess) {
                Log.e(TAG, "❌ Error obteniendo equipos: ${teamsResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(teamsResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }

            // 2. Obtener partidos desde API oficial
            val matchesResult = officialApiDataSource.getAllMatches()
            if (!matchesResult.isSuccess) {
                Log.e(TAG, "❌ Error obteniendo partidos: ${matchesResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(matchesResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }

            val teams = teamsResult.getOrNull() ?: emptyList()
            val matches = matchesResult.getOrNull() ?: emptyList()

            Log.d(TAG, "✅ Datos obtenidos - Equipos: ${teams.size}, Partidos: ${matches.size}")

            // 3. Convertir a formato estático
            val staticTeams = teams.map { team ->
                StaticTeam(
                    id = team.id,
                    name = team.name,
                    shortName = team.shortCode,
                    logoUrl = team.logoUrl ?: "",
                    country = team.country ?: "",
                    city = "",
                    venue = "", // No disponible en la entidad Team
                    code = team.shortCode
                )
            }
            
            val staticMatches = matches.map { match ->
                StaticMatch(
                    id = match.id,
                    round = Integer.parseInt(match.round ?: "", 10),
                    homeTeamCode = match.homeTeamId, // Usando homeTeamId como código
                    awayTeamCode = match.awayTeamId, // Usando awayTeamId como código
                    venue = match.venue ?: "",
                    season = "2025-26",
                    status = match.status.name,
                    dateTime = match.date,
                    homeScore = match.homeScore,
                    awayScore = match.awayScore
                )
            }
            
            // 4. Guardar archivos JSON
            val teamsData = StaticTeamsData(
                version = "1.0",
                lastUpdated = java.time.LocalDateTime.now().toString(),
                teams = staticTeams
            )

            val matchesData = StaticMatchesData(
                version = "1.0",
                lastUpdated = java.time.LocalDateTime.now().toString(),
                season = "2025-26",
                totalRounds = 34,
                description = "EuroLeague 2025-26 season calendar generated from API",
                note = "Generated from https://feeds.incrowdsports.com/provider/euroleague-feeds/v2",
                matches = staticMatches
            )

            saveStaticFile(TEAMS_FILE, json.encodeToString(teamsData))
            saveStaticFile(MATCHES_FILE, json.encodeToString(matchesData))

            val result = GenerationResult(
                teamsGenerated = staticTeams.size,
                matchesGenerated = staticMatches.size,
                timestamp = System.currentTimeMillis()
            )
            
            Log.d(TAG, "✅ [GENERATOR] Generación completada: ${result.teamsGenerated} equipos, ${result.matchesGenerated} partidos")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GENERATOR] Error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Genera solo equipos estáticos desde API
     */
    private suspend fun generateTeamsData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏗️ [GENERATOR] Generando solo equipos desde API...")
            
            val teamsResult = officialApiDataSource.getAllTeams()
            if (!teamsResult.isSuccess) {
                Log.e(TAG, "❌ Error obteniendo equipos: ${teamsResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(teamsResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }

            val teams = teamsResult.getOrNull() ?: emptyList()
            val staticTeams = teams.map { team ->
                StaticTeam(
                    id = team.id,
                    name = team.name,
                    shortName = team.shortCode,
                    logoUrl = team.logoUrl ?: "",
                    country = team.country ?: "",
                    city = "",
                    venue = "", // No disponible en la entidad Team
                    code = team.shortCode
                )
            }
            
            saveTeamsToAssets(staticTeams)
            Log.d(TAG, "🏗️ [GENERATOR] ✅ Equipos generados: ${staticTeams.size}")
            Result.success(staticTeams.size)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GENERATOR] Error generando equipos", e)
            Result.failure(e)
        }
    }
    
    /**
     * Genera solo partidos estáticos desde API
     */
    private suspend fun generateMatchesData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏗️ [GENERATOR] Generando solo partidos desde API...")
            
            val matchesResult = officialApiDataSource.getAllMatches()
            if (!matchesResult.isSuccess) {
                Log.e(TAG, "❌ Error obteniendo partidos: ${matchesResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(matchesResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }
            
            val matches = matchesResult.getOrNull() ?: emptyList()
            val staticMatches = matches.map { match ->
                StaticMatch(
                    id = match.id,
                    round = match.round ?.toIntOrNull() ?: 0,
                    homeTeamCode = match.homeTeamId,
                    awayTeamCode = match.awayTeamId,
                    venue = match.venue ?: "",
                    season = "2025-26",
                    status = match.status.name,
                    dateTime = match.date,
                    homeScore = match.homeScore,
                    awayScore = match.awayScore
                )
            }
            
            saveMatchesToAssets(staticMatches)
            Log.d(TAG, "🏗️ [GENERATOR] ✅ Partidos generados: ${staticMatches.size}")
            Result.success(staticMatches.size)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GENERATOR] Error generando partidos", e)
            Result.failure(e)
        }
    }

    /**
     * Guarda un archivo estático en el directorio interno
     */
    private fun saveStaticFile(filename: String, content: String) {
        val internalDir = File(context.filesDir, STATIC_DATA_DIR)
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }

        val file = File(internalDir, filename)
        file.writeText(content)

        Log.d(TAG, "💾 [GENERATOR] Archivo guardado: ${file.absolutePath}")
    }

    /**
     * Guarda equipos en archivo JSON interno de la aplicación
     */
    private fun saveTeamsToAssets(teams: List<StaticTeam>) {
        val internalDir = File(context.filesDir, STATIC_DATA_DIR)
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        
        // Crear la estructura wrapper completa
        val teamsData = StaticTeamsData(
            version = "1.0",
            lastUpdated = java.time.LocalDateTime.now().toString(),
            teams = teams
        )
        
        val teamsFile = File(internalDir, TEAMS_FILE)
        val jsonContent = Json { prettyPrint = true }.encodeToString(teamsData)
        teamsFile.writeText(jsonContent)
        
        Log.d(TAG, "💾 [GENERATOR] Equipos guardados en: ${teamsFile.absolutePath}")
    }
    
    /**
     * Guarda partidos en archivo JSON interno de la aplicación
     */
    private fun saveMatchesToAssets(matches: List<StaticMatch>) {
        val internalDir = File(context.filesDir, STATIC_DATA_DIR)
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        
        // Crear la estructura wrapper completa
        val matchesData = StaticMatchesData(
            version = "1.0",
            lastUpdated = java.time.LocalDateTime.now().toString(),
            season = "2025-26",
            totalRounds = 34,
            description = "EuroLeague 2025-26 season calendar generated from API",
            note = "Generated from https://feeds.incrowdsports.com/provider/euroleague-feeds/v2",
            matches = matches
        )
        
        val matchesFile = File(internalDir, MATCHES_FILE)
        val jsonContent = Json { prettyPrint = true }.encodeToString(matchesData)
        matchesFile.writeText(jsonContent)
        
        Log.d(TAG, "💾 [GENERATOR] Partidos guardados en: ${matchesFile.absolutePath}")
    }
    
    /**
     * Verifica si existen datos estáticos (tanto en assets como en archivos internos)
     */
    fun hasStaticData(): Boolean {
        return try {
            // Primero verificar si existen en assets (datos originales)
            val assetsManager = context.assets
            val teamsExistsInAssets = assetsManager.list(STATIC_DATA_DIR)?.contains(TEAMS_FILE) == true
            val matchesExistsInAssets = assetsManager.list(STATIC_DATA_DIR)?.contains(MATCHES_FILE) == true
            
            if (teamsExistsInAssets && matchesExistsInAssets) {
                return true
            }
            
            // Verificar si existen en archivos internos (generados)
            val internalDir = File(context.filesDir, STATIC_DATA_DIR)
            val teamsFile = File(internalDir, TEAMS_FILE)
            val matchesFile = File(internalDir, MATCHES_FILE)
            
            teamsFile.exists() && matchesFile.exists()
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtiene información sobre los datos estáticos existentes
     */
    suspend fun getStaticDataInfo(): StaticDataInfo = withContext(Dispatchers.IO) {
        try {
            if (!hasStaticData()) {
                return@withContext StaticDataInfo(
                    hasData = false,
                    teamsCount = 0,
                    matchesCount = 0,
                    lastGenerated = 0L
                )
            }
            
            // TODO: Implementar lectura de metadatos de generación
            StaticDataInfo(
                hasData = true,
                teamsCount = 18, // Placeholder
                matchesCount = 306, // Placeholder (18 equipos * 34 jornadas / 2)
                lastGenerated = System.currentTimeMillis()
            )
            
        } catch (_: Exception) {
            StaticDataInfo(false, 0, 0, 0L)
        }
    }
}

/**
 * Resultado de la generación de datos estáticos
 */
data class GenerationResult(
    val teamsGenerated: Int,
    val matchesGenerated: Int,
    val timestamp: Long
)

/**
 * Información sobre datos estáticos existentes
 */
data class StaticDataInfo(
    val hasData: Boolean,
    val teamsCount: Int,
    val matchesCount: Int,
    val lastGenerated: Long
)
