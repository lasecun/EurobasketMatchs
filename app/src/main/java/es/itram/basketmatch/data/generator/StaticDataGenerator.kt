package es.itram.basketmatch.data.generator

import android.content.Context
import android.util.Log
import es.itram.basketmatch.data.datasource.local.assets.StaticMatch
import es.itram.basketmatch.data.datasource.local.assets.StaticMatchesData
import es.itram.basketmatch.data.datasource.local.assets.StaticTeam
import es.itram.basketmatch.data.datasource.local.assets.StaticTeamsData
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
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

}

/**
 * Resultado de la generación de datos estáticos
 */
data class GenerationResult(
    val teamsGenerated: Int,
    val matchesGenerated: Int,
    val timestamp: Long
)
