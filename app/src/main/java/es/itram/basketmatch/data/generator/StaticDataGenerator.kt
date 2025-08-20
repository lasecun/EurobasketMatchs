package es.itram.basketmatch.data.generator

import android.content.Context
import android.util.Log
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.data.datasource.local.assets.StaticMatch
import es.itram.basketmatch.data.datasource.local.assets.StaticMatchesData
import es.itram.basketmatch.data.datasource.local.assets.StaticTeam
import es.itram.basketmatch.data.datasource.local.assets.StaticTeamsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generador de datos estáticos desde la API real de EuroLeague
 * 
 * Este componente obtiene datos actualizados desde:
 * https://feeds.incrowdsports.com/provider/euroleague-feeds/v2
 * 
 * Y los convierte en archivos JSON estáticos para uso offline.
 */
@Singleton
class StaticDataGenerator @Inject constructor(
    private val euroLeagueApiScraper: EuroLeagueJsonApiScraper,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "StaticDataGenerator"
        private const val STATIC_DATA_DIR = "static_data"
        private const val TEAMS_FILE = "teams_2025_26.json"
        private const val MATCHES_FILE = "matches_calendar_2025_26.json"
    }
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }
    
    /**
     * Genera todos los datos estáticos desde la API real
     */
    suspend fun generateAllStaticData(): Result<GenerationResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏗️ [GENERATOR] Iniciando generación de datos estáticos desde API EuroLeague...")
            
            // 1. Obtener equipos desde API
            Log.d(TAG, "🏗️ [GENERATOR] Paso 1/4: Obteniendo equipos desde API...")
            val apiTeams = euroLeagueApiScraper.getTeams()
            Log.d(TAG, "🏗️ [GENERATOR] ✅ Equipos obtenidos desde API: ${apiTeams.size}")
            
            // 2. Convertir a formato estático con información completa
            Log.d(TAG, "🏗️ [GENERATOR] Paso 2/4: Convirtiendo equipos a formato estático...")
            val staticTeams = apiTeams.map { teamDto ->
                StaticTeam(
                    id = teamDto.shortCode ?: teamDto.id, // Usar shortCode como ID principal
                    name = teamDto.name,
                    shortName = teamDto.shortCode ?: teamDto.name.split(" ").firstOrNull() ?: teamDto.name,
                    logoUrl = teamDto.logoUrl ?: "",
                    primaryColor = "#000000", // Color por defecto - se puede mejorar con API
                    secondaryColor = "#FFFFFF", // Color por defecto - se puede mejorar con API
                    country = teamDto.country ?: "",
                    city = extractCityFromName(teamDto.name),
                    venue = teamDto.venue ?: "",
                    website = "", // No disponible en TeamWebDto actual
                    president = "", // No disponible en TeamWebDto actual
                    phone = "", // No disponible en TeamWebDto actual
                    address = "", // No disponible en TeamWebDto actual
                    twitterAccount = "", // No disponible en TeamWebDto actual
                    ticketsUrl = "" // No disponible en TeamWebDto actual
                )
            }
            
            // 3. Obtener partidos desde API (temporada completa)
            Log.d(TAG, "🏗️ [GENERATOR] Paso 3/4: Obteniendo partidos desde API...")
            val apiMatches = euroLeagueApiScraper.getMatchesWithProgress("2025-26") { current, total ->
                Log.d(TAG, "🏗️ [GENERATOR] Progreso partidos: $current/$total jornadas")
            }
            Log.d(TAG, "🏗️ [GENERATOR] ✅ Partidos obtenidos desde API: ${apiMatches.size}")
            
            // 4. Convertir partidos a formato estático
            Log.d(TAG, "🏗️ [GENERATOR] Paso 4/4: Convirtiendo partidos a formato estático...")
            val staticMatches = apiMatches.map { matchDto ->
                StaticMatch(
                    id = matchDto.id,
                    round = matchDto.round?.toIntOrNull() ?: 1,
                    homeTeamCode = matchDto.homeTeamId,
                    awayTeamCode = matchDto.awayTeamId,
                    venue = matchDto.venue ?: "",
                    season = "2025-26",
                    status = matchDto.status.name,
                    dateTime = "${matchDto.date}T${matchDto.time ?: "20:00"}:00", // Formato ISO
                    homeScore = null, // Los partidos futuros no tienen resultado
                    awayScore = null
                )
            }
            
            // 5. Guardar archivos JSON
            Log.d(TAG, "🏗️ [GENERATOR] Guardando archivos JSON...")
            saveTeamsToAssets(staticTeams)
            saveMatchesToAssets(staticMatches)
            
            val result = GenerationResult(
                teamsGenerated = staticTeams.size,
                matchesGenerated = staticMatches.size,
                generationTimestamp = System.currentTimeMillis()
            )
            
            Log.d(TAG, "🏗️ [GENERATOR] ✅ Generación completada: ${result.teamsGenerated} equipos, ${result.matchesGenerated} partidos")
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GENERATOR] Error generando datos estáticos", e)
            Result.failure(e)
        }
    }
    
    /**
     * Genera solo equipos estáticos desde API
     */
    suspend fun generateTeamsData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏗️ [GENERATOR] Generando solo equipos desde API...")
            
            val apiTeams = euroLeagueApiScraper.getTeams()
            val staticTeams = apiTeams.map { teamDto ->
                StaticTeam(
                    id = teamDto.shortCode ?: teamDto.id,
                    name = teamDto.name,
                    shortName = teamDto.shortCode ?: teamDto.name.split(" ").firstOrNull() ?: teamDto.name,
                    logoUrl = teamDto.logoUrl ?: "",
                    primaryColor = "#000000", 
                    secondaryColor = "#FFFFFF",
                    country = teamDto.country ?: "",
                    city = extractCityFromName(teamDto.name),
                    venue = teamDto.venue ?: "",
                    website = "",
                    president = "",
                    phone = "",
                    address = "",
                    twitterAccount = "",
                    ticketsUrl = ""
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
    suspend fun generateMatchesData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏗️ [GENERATOR] Generando solo partidos desde API...")
            
            val apiMatches = euroLeagueApiScraper.getMatchesWithProgress("2025-26") { current, total ->
                Log.d(TAG, "🏗️ [GENERATOR] Progreso: $current/$total jornadas")
            }
            
            val staticMatches = apiMatches.map { matchDto ->
                StaticMatch(
                    id = matchDto.id,
                    round = matchDto.round?.toIntOrNull() ?: 1,
                    homeTeamCode = matchDto.homeTeamId,
                    awayTeamCode = matchDto.awayTeamId,
                    venue = matchDto.venue ?: "",
                    season = "2025-26",
                    status = matchDto.status.name,
                    dateTime = "${matchDto.date}T${matchDto.time ?: "20:00"}:00",
                    homeScore = null,
                    awayScore = null
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
        val jsonContent = json.encodeToString(teamsData)
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
        val jsonContent = json.encodeToString(matchesData)
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
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo info de datos estáticos", e)
            StaticDataInfo(false, 0, 0, 0L)
        }
    }
    
    /**
     * Extrae la ciudad del nombre del equipo
     */
    private fun extractCityFromName(teamName: String): String {
        val cityMappings = mapOf(
            "anadolu efes" to "Istanbul",
            "as monaco" to "Monaco",
            "baskonia" to "Vitoria-Gasteiz",
            "crvena zvezda" to "Belgrade",
            "ea7 emporio armani" to "Milan",
            "fc barcelona" to "Barcelona",
            "fc bayern munich" to "Munich",
            "maccabi playtika" to "Tel Aviv",
            "ldlc asvel" to "Villeurbanne",
            "olympiacos" to "Athens",
            "paris basketball" to "Paris",
            "real madrid" to "Madrid",
            "valencia basket" to "Valencia",
            "virtus segafredo" to "Bologna",
            "zalgiris" to "Kaunas",
            "fenerbahce" to "Istanbul"
        )
        
        val lowerName = teamName.lowercase()
        return cityMappings.entries.find { 
            lowerName.contains(it.key) 
        }?.value ?: teamName.split(" ").first()
    }
}

/**
 * Resultado de la generación de datos estáticos
 */
data class GenerationResult(
    val teamsGenerated: Int,
    val matchesGenerated: Int,
    val generationTimestamp: Long
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
