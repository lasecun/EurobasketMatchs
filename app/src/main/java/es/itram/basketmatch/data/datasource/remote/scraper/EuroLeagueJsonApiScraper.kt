package es.itram.basketmatch.data.datasource.remote.scraper

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamRosterResponse
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scraper que utiliza la API JSON oficial de EuroLeague
 * Esta es mucho más confiable que el scraping HTML
 */
@Singleton
class EuroLeagueJsonApiScraper @Inject constructor() {
    
    companion object {
        private const val TAG = "EuroLeagueJsonApiScraper"
        
        // API de EuroLeague feeds
        private const val FEEDS_BASE_URL = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2"
        private const val GAMES_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/games"
        private const val ROSTER_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/clubs"
        
        private val json = Json { 
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
    
    /**
     * Obtiene equipos directamente de la API JSON extrayéndolos de los partidos
     */
    suspend fun getTeams(): List<TeamWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏀 [NETWORK] Obteniendo equipos desde API de feeds...")
            
            // Obtenemos algunos partidos para extraer todos los equipos únicos
            val teams = mutableSetOf<TeamWebDto>()
            
            // Obtenemos partidos de las primeras jornadas para conseguir todos los equipos
            for (round in 1..3) {
                try {
                    val apiUrl = "${FEEDS_BASE_URL}/competitions/E/seasons/E2025/games?teamCode=&phaseTypeCode=RS&roundNumber=$round"
                    Log.d(TAG, "🌐 [NETWORK] Extrayendo equipos desde jornada $round...")
                    
                    val jsonResponse = fetchJsonFromUrl(apiUrl)
                    val response = json.decodeFromString<EuroLeagueFeedsResponse>(jsonResponse)
                    
                    response.data.forEach { match ->
                        // Agregar equipo local
                        teams.add(
                            TeamWebDto(
                                id = generateTeamId(match.home.name),
                                name = match.home.name,
                                fullName = match.home.name,
                                shortCode = match.home.code,
                                logoUrl = match.home.imageUrls?.crest,
                                country = extractCountryFromName(match.home.name),
                                venue = null,
                                profileUrl = generateTeamProfileUrl(match.home.code)
                            )
                        )
                        
                        // Agregar equipo visitante
                        teams.add(
                            TeamWebDto(
                                id = generateTeamId(match.away.name),
                                name = match.away.name,
                                fullName = match.away.name,
                                shortCode = match.away.code,
                                logoUrl = match.away.imageUrls?.crest,
                                country = extractCountryFromName(match.away.name),
                                venue = null,
                                profileUrl = generateTeamProfileUrl(match.away.code)
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ [NETWORK] Error obteniendo jornada $round para equipos: ${e.message}")
                }
            }
            
            val teamsList = teams.toList()
            Log.d(TAG, "� [NETWORK] ✅ Equipos extraídos exitosamente desde API de feeds: ${teamsList.size}")
            teamsList
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error obteniendo equipos desde API de feeds", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene todos los partidos de la temporada desde la nueva API de feeds
     */
    suspend fun getMatches(season: String = "2025-26"): List<MatchWebDto> = withContext(Dispatchers.IO) {
        getMatchesWithProgress(season) { _, _ -> }
    }
    
    /**
     * Obtiene todos los partidos de la temporada con callback de progreso
     */
    suspend fun getMatchesWithProgress(
        season: String = "2025-26",
        onProgress: (current: Int, total: Int) -> Unit
    ): List<MatchWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 [NETWORK] Iniciando obtención de partidos desde API EuroLeague para temporada $season...")
            
            val allMatches = mutableListOf<MatchWebDto>()
            val totalRounds = 38
            
            // Iterar por todas las 38 jornadas de la temporada regular
            for (round in 1..totalRounds) {
                try {
                    Log.d(TAG, "🌐 [NETWORK] Obteniendo jornada $round desde API...")
                    onProgress(round, totalRounds)
                    
                    val roundMatches = getMatchesForRound(round, season)
                    allMatches.addAll(roundMatches)
                    Log.d(TAG, "🌐 [NETWORK] ✅ Jornada $round obtenida desde API: ${roundMatches.size} partidos")
                    
                    // Pequeña pausa para no saturar la API
                    kotlinx.coroutines.delay(100)
                } catch (e: Exception) {
                    Log.w(TAG, "🌐 [NETWORK] ⚠️ Error obteniendo jornada $round desde API: ${e.message}")
                }
            }
            
            Log.d(TAG, "� [NETWORK] ✅ Total partidos obtenidos desde API: ${allMatches.size} de $totalRounds jornadas")
            allMatches
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error obteniendo partidos desde API de feeds", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene partidos de una jornada específica
     */
    private suspend fun getMatchesForRound(round: Int, season: String): List<MatchWebDto> {
        val url = "$GAMES_URL?teamCode=&phaseTypeCode=RS&roundNumber=$round"
        Log.d(TAG, "🌐 [NETWORK] Petición API: $url")
        val jsonResponse = fetchJsonFromUrl(url)
        
        val feedsResponse = json.decodeFromString<EuroLeagueFeedsResponse>(jsonResponse)
        Log.d(TAG, "🌐 [NETWORK] ✅ Respuesta API recibida para jornada $round: ${feedsResponse.data.size} partidos")
        
        return feedsResponse.data.map { game ->
            convertFeedsGameToMatchDto(game, season)
        }
    }
    
    /**
     * Obtiene partidos de una jornada específica (para refresh)
     */
    suspend fun getMatchesForRoundRefresh(round: Int, season: String = "2025-26"): List<MatchWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 [NETWORK] 🔄 Refrescando jornada $round desde API...")
            val matches = getMatchesForRound(round, season)
            Log.d(TAG, "🌐 [NETWORK] ✅ Jornada $round refrescada: ${matches.size} partidos")
            matches
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error refrescando jornada $round desde API", e)
            emptyList()
        }
    }
    
    /**
     * Convierte un partido de la nueva API de feeds a MatchWebDto
     */
    private fun convertFeedsGameToMatchDto(feedsGame: FeedsGame, season: String): MatchWebDto {
        // Parsear el timestamp ISO para extraer fecha y hora por separado
        val dateTime = parseIsoDateTime(feedsGame.date)
        
        return MatchWebDto(
            id = feedsGame.id,
            homeTeamId = feedsGame.home.code,
            homeTeamName = feedsGame.home.name,
            homeTeamLogo = feedsGame.home.imageUrls?.crest,
            awayTeamId = feedsGame.away.code,
            awayTeamName = feedsGame.away.name,
            awayTeamLogo = feedsGame.away.imageUrls?.crest,
            date = dateTime.first, // Solo la fecha (YYYY-MM-DD)
            time = dateTime.second, // Solo la hora (HH:mm)
            venue = feedsGame.venue?.name,
            status = convertFeedsStatus(feedsGame.status),
            homeScore = if (feedsGame.home.score > 0) feedsGame.home.score else null,
            awayScore = if (feedsGame.away.score > 0) feedsGame.away.score else null,
            round = feedsGame.round.round.toString(),
            season = season
        )
    }

    /**
     * Convierte el status de la nueva API al enum MatchStatus
     */
    private fun convertFeedsStatus(status: String): MatchStatus {
        return when (status.lowercase()) {
            "confirmed", "scheduled" -> MatchStatus.SCHEDULED
            "live", "playing" -> MatchStatus.LIVE
            "finished", "closed" -> MatchStatus.FINISHED
            "postponed" -> MatchStatus.POSTPONED
            "cancelled" -> MatchStatus.CANCELLED
            else -> MatchStatus.SCHEDULED
        }
    }

    /**
     * Genera un ID único para el equipo
     */
    private fun generateTeamId(teamName: String): String {
        return teamName.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace(".", "")
    }
    
    /**
     * Extrae el país basado en el nombre del equipo
     */
    private fun extractCountryFromName(teamName: String): String? {
        val countryMappings = mapOf(
            "istanbul" to "Turkey",
            "monaco" to "Monaco", 
            "vitoria" to "Spain",
            "gasteiz" to "Spain",
            "belgrade" to "Serbia",
            "dubai" to "UAE",
            "milan" to "Italy",
            "barcelona" to "Spain",
            "munich" to "Germany",
            "tel aviv" to "Israel",
            "villeurbanne" to "France",
            "athens" to "Greece",
            "paris" to "France",
            "madrid" to "Spain",
            "valencia" to "Spain",
            "bologna" to "Italy",
            "kaunas" to "Lithuania"
        )
        
        val lowerName = teamName.lowercase()
        return countryMappings.entries.find { 
            lowerName.contains(it.key) 
        }?.value
    }
    
    /**
     * Genera URL del perfil del equipo basada en feeds API data
     * Nota: Mantenemos compatibilidad con el sitio web oficial pero indicamos que los datos vienen de feeds API
     */
    private fun generateTeamProfileUrl(teamCode: String): String {
        // Podríamos generar URLs más específicas basadas en el código del equipo si fuera necesario
        // Por ahora usamos una URL genérica que indica que los datos vienen de la API de feeds
        return "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/teams/$teamCode"
    }
    
    /**
     * Realiza una petición HTTP GET y obtiene el JSON
     */
    private fun fetchJsonFromUrl(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
    
    /**
     * Obtiene el roster de un equipo por su código TLA
     */
    suspend fun getTeamRoster(teamTla: String, season: String = "E2025"): List<PlayerDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 [NETWORK] Obteniendo roster del equipo $teamTla para temporada $season desde API...")
            
            val url = "$ROSTER_URL/$teamTla/people"
            val jsonResponse = fetchJsonFromUrl(url)
            
            // La API devuelve directamente un array de PlayerDto
            val rosterResponse = json.decodeFromString<TeamRosterResponse>(jsonResponse)
            
            Log.d(TAG, "🌐 [NETWORK] ✅ Roster obtenido exitosamente desde API: ${rosterResponse.size} jugadores para $teamTla")
            rosterResponse
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error obteniendo roster del equipo $teamTla desde API", e)
            emptyList()
        }
    }

    /**
     * Parsea un timestamp ISO (ej: "2025-09-30T18:00:00.000Z") y retorna fecha y hora por separado
     */
    private fun parseIsoDateTime(isoDateTime: String): Pair<String, String> {
        return try {
            if (isoDateTime.contains("T")) {
                val parts = isoDateTime.split("T")
                val date = parts[0] // YYYY-MM-DD
                val timePart = parts[1].substringBefore(".").substringBefore("Z") // HH:mm:ss
                val time = timePart.substring(0, minOf(5, timePart.length)) // Solo HH:mm
                Pair(date, time)
            } else {
                // Si no es formato ISO, usar como está
                Pair(isoDateTime, "")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parseando fecha ISO: $isoDateTime", e)
            Pair(isoDateTime.substringBefore("T"), "")
        }
    }
}

// ==================== CLASES DTO PARA LA API DE FEEDS ====================

@Serializable
data class EuroLeagueFeedsResponse(
    val status: String,
    val data: List<FeedsGame>,
    val metadata: FeedsMetadata? = null
)

@Serializable
data class FeedsGame(
    val id: String,
    val identifier: String? = null,
    val code: Int? = null,
    val season: FeedsSeason,
    val competition: FeedsCompetition,
    val group: FeedsGroup? = null,
    val phaseType: FeedsPhaseType,
    val round: FeedsRound,
    val date: String, // ISO format: "2025-09-30T18:00:00.000Z"
    val status: String,
    val minute: Int? = null,
    val remainingTime: String? = null,
    val quarter: Int? = null,
    val quarterMinute: String? = null,
    val home: FeedsTeam,
    val away: FeedsTeam,
    val referees: List<String>? = null,
    val venue: FeedsVenue? = null,
    val confirmedDate: Boolean = true,
    val confirmedTime: Boolean = true,
    val audience: Int = 0,
    val audienceConfirmed: Boolean = false,
    val broadcasters: List<String> = emptyList()
)

@Serializable
data class FeedsSeason(
    val code: String,
    val name: String,
    val alias: String? = null,
    val year: Int
)

@Serializable
data class FeedsCompetition(
    val code: String,
    val name: String
)

@Serializable
data class FeedsGroup(
    val id: String,
    val name: String,
    val order: Int
)

@Serializable
data class FeedsPhaseType(
    val code: String,
    val name: String,
    val alias: String? = null,
    val isGroupPhase: Boolean = false
)

@Serializable
data class FeedsRound(
    val round: Int,
    val name: String,
    val alias: String? = null
)

@Serializable
data class FeedsTeam(
    val code: String,
    val name: String,
    val abbreviatedName: String? = null,
    val tla: String? = null,
    val score: Int = 0,
    val standingsScore: Int = 0,
    val quarters: FeedsQuarters,
    val coach: FeedsCoach? = null,
    val imageUrls: FeedsImageUrls? = null,
    val editorialName: String? = null
)

@Serializable
data class FeedsQuarters(
    val q1: Int = 0,
    val q2: Int = 0,
    val q3: Int = 0,
    val q4: Int = 0,
    val ot1: Int? = null,
    val ot2: Int? = null,
    val ot3: Int? = null,
    val ot4: Int? = null,
    val ot5: Int? = null
)

@Serializable
data class FeedsCoach(
    val code: String? = null,
    val name: String? = null
)

@Serializable
data class FeedsImageUrls(
    val crest: String? = null
)

@Serializable
data class FeedsVenue(
    val code: String,
    val name: String,
    val capacity: Int = 0,
    val address: String? = null,
    val notes: String? = null
)

@Serializable
data class FeedsMetadata(
    val createdAt: String? = null,
    val pageItems: Int = 0,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val sort: String? = null
)
