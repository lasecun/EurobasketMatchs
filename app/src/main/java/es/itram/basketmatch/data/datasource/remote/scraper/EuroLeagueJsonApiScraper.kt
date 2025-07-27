package es.itram.basketmatch.data.datasource.remote.scraper

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        
        // Nueva API de EuroLeague feeds
        private const val FEEDS_BASE_URL = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2"
        private const val GAMES_URL = "$FEEDS_BASE_URL/competitions/E/seasons/E2025/games"
        
        // URLs legacy para retrocompatibilidad
        private const val BASE_JSON_URL = "https://www.euroleaguebasketball.net/_next/data"
        private const val BUILD_ID = "a52CgOKFrJehM6XbgT-b_"
        private const val GAME_CENTER_JSON_URL = "$BASE_JSON_URL/$BUILD_ID/es/euroleague/game-center.json"
        private const val SCHEDULE_JSON_URL = "$BASE_JSON_URL/$BUILD_ID/es/euroleague/calendar.json"
        private const val ALL_GAMES_URL = "$BASE_JSON_URL/$BUILD_ID/es/euroleague/results.json"
        
        private val json = Json { 
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
    
    /**
     * Obtiene equipos directamente de la API JSON
     */
    suspend fun getTeams(): List<TeamWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏀 Obteniendo equipos desde API JSON...")
            
            val jsonResponse = fetchJsonFromUrl(GAME_CENTER_JSON_URL)
            val gameCenterData = json.decodeFromString<GameCenterResponse>(jsonResponse)
            
            val teams = mutableListOf<TeamWebDto>()
            
            // Extraer equipos de headerData.euroleague.clubs
            gameCenterData.headerData?.euroleague?.clubs?.clubs?.forEach { club ->
                teams.add(
                    TeamWebDto(
                        id = generateTeamId(club.name),
                        name = club.name,
                        fullName = club.name,
                        shortCode = extractShortCode(club.url) ?: club.name.take(3).uppercase(),
                        logoUrl = club.logo.image,
                        country = extractCountryFromName(club.name),
                        venue = null, // No disponible en esta API
                        profileUrl = "https://www.euroleaguebasketball.net${club.url}"
                    )
                )
            }
            
            Log.d(TAG, "✅ Equipos obtenidos exitosamente: ${teams.size}")
            teams.distinctBy { it.id }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo equipos desde API JSON", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene todos los partidos de la temporada desde la nueva API de feeds
     */
    suspend fun getMatches(season: String = "2025-26"): List<MatchWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "⚽ Obteniendo partidos desde nueva API de feeds para temporada $season...")
            
            val allMatches = mutableListOf<MatchWebDto>()
            
            // Iterar por todas las 38 jornadas de la temporada regular
            for (round in 1..38) {
                try {
                    Log.d(TAG, "📅 Obteniendo jornada $round...")
                    val roundMatches = getMatchesForRound(round, season)
                    allMatches.addAll(roundMatches)
                    Log.d(TAG, "✅ Jornada $round: ${roundMatches.size} partidos obtenidos")
                    
                    // Pequeña pausa para no saturar la API
                    kotlinx.coroutines.delay(100)
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Error obteniendo jornada $round: ${e.message}")
                }
            }
            
            Log.d(TAG, "🏆 Total partidos obtenidos: ${allMatches.size} de 38 jornadas")
            allMatches
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo partidos desde nueva API", e)
            // Fallback a la API anterior
            getMatchesLegacy(season)
        }
    }
    
    /**
     * Obtiene partidos de una jornada específica
     */
    private suspend fun getMatchesForRound(round: Int, season: String): List<MatchWebDto> {
        val url = "$GAMES_URL?teamCode=&phaseTypeCode=RS&roundNumber=$round"
        val jsonResponse = fetchJsonFromUrl(url)
        
        val feedsResponse = json.decodeFromString<EuroLeagueFeedsResponse>(jsonResponse)
        
        return feedsResponse.data.map { game ->
            convertFeedsGameToMatchDto(game, season)
        }
    }
    
    /**
     * Método legacy como fallback
     */
    private suspend fun getMatchesLegacy(season: String): List<MatchWebDto> {
        Log.d(TAG, "🔄 Usando API legacy como fallback...")
        
        val matches = mutableListOf<MatchWebDto>()
        
        // Intentar múltiples endpoints legacy
        val endpoints = listOf(
            GAME_CENTER_JSON_URL,
            SCHEDULE_JSON_URL,
            ALL_GAMES_URL
        )
        
        for (endpoint in endpoints) {
            try {
                Log.d(TAG, "📡 Intentando endpoint legacy: $endpoint")
                val jsonResponse = fetchJsonFromUrl(endpoint)
                val endpointMatches = parseMatchesFromEndpoint(jsonResponse, season, endpoint)
                matches.addAll(endpointMatches)
                Log.d(TAG, "✅ Obtenidos ${endpointMatches.size} partidos desde $endpoint")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error en endpoint $endpoint: ${e.message}")
            }
        }
        
        val uniqueMatches = matches.distinctBy { it.id }
        Log.d(TAG, "✅ Total partidos únicos legacy: ${uniqueMatches.size}")
        
        return uniqueMatches
    }
    
    /**
     * Parsea partidos desde diferentes endpoints
     */
    private fun parseMatchesFromEndpoint(jsonResponse: String, season: String, endpoint: String): List<MatchWebDto> {
        return try {
            when {
                endpoint.contains("game-center") -> {
                    val gameCenterData = json.decodeFromString<GameCenterResponse>(jsonResponse)
                    parseGameCenterMatches(gameCenterData, season)
                }
                endpoint.contains("calendar") -> {
                    // Intentar parsear como calendario (estructura puede ser diferente)
                    parseCalendarMatches(jsonResponse, season)
                }
                endpoint.contains("results") -> {
                    // Intentar parsear como resultados (estructura puede ser diferente)
                    parseResultsMatches(jsonResponse, season)
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parseando endpoint $endpoint: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Parsea partidos desde game-center (método original)
     */
    private fun parseGameCenterMatches(gameCenterData: GameCenterResponse, season: String): List<MatchWebDto> {
        val matches = mutableListOf<MatchWebDto>()
        
        // Extraer partidos de pageProps.currentRoundGameGroups
        gameCenterData.pageProps?.currentRoundGameGroups?.forEach { gameGroup ->
            gameGroup.games?.forEach { game ->
                try {
                    val matchDto = convertGameToMatchDto(game, season)
                    matches.add(matchDto)
                } catch (e: Exception) {
                    Log.w(TAG, "Error procesando partido ${game.id}: ${e.message}")
                }
            }
        }
        
        return matches
    }
    
    /**
     * Intenta parsear partidos desde endpoint de calendario
     */
    private fun parseCalendarMatches(jsonResponse: String, season: String): List<MatchWebDto> {
        // Por ahora, intentamos la misma estructura que game-center
        // Si falla, devolvemos lista vacía
        return try {
            val gameCenterData = json.decodeFromString<GameCenterResponse>(jsonResponse)
            parseGameCenterMatches(gameCenterData, season)
        } catch (e: Exception) {
            Log.w(TAG, "Calendar endpoint no compatible con estructura game-center")
            emptyList()
        }
    }
    
    /**
     * Intenta parsear partidos desde endpoint de resultados
     */
    private fun parseResultsMatches(jsonResponse: String, season: String): List<MatchWebDto> {
        // Por ahora, intentamos la misma estructura que game-center
        // Si falla, devolvemos lista vacía
        return try {
            val gameCenterData = json.decodeFromString<GameCenterResponse>(jsonResponse)
            parseGameCenterMatches(gameCenterData, season)
        } catch (e: Exception) {
            Log.w(TAG, "Results endpoint no compatible con estructura game-center")
            emptyList()
        }
    }
    
    /**
     * Convierte un partido de la nueva API de feeds a MatchWebDto
     */
    private fun convertFeedsGameToMatchDto(feedsGame: FeedsGame, season: String): MatchWebDto {
        return MatchWebDto(
            id = feedsGame.id,
            homeTeamId = feedsGame.home.code,
            homeTeamName = feedsGame.home.name,
            awayTeamId = feedsGame.away.code,
            awayTeamName = feedsGame.away.name,
            date = feedsGame.date.substringBefore("T"), // Extraer solo la fecha (YYYY-MM-DD)
            time = feedsGame.date,
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
     * Convierte un objeto Game de la API a MatchWebDto
     */
    private fun convertGameToMatchDto(game: Game, season: String): MatchWebDto {
        val matchId = game.id ?: generateMatchId(game)
        
        return MatchWebDto(
            id = matchId,
            homeTeamId = game.home.code,
            homeTeamName = game.home.name,
            awayTeamId = game.away.code,
            awayTeamName = game.away.name,
            date = game.date.substringBefore("T"), // Extraer solo la fecha (YYYY-MM-DD)
            time = game.date,
            venue = game.venue?.name,
            status = convertStatus(game.status),
            homeScore = if (game.home.score > 0) game.home.score else null,
            awayScore = if (game.away.score > 0) game.away.score else null,
            round = game.round?.round?.toString() ?: "1",
            season = season
        )
    }
    
    /**
     * Genera un ID único para un partido cuando no está disponible
     */
    private fun generateMatchId(game: Game): String {
        val homeTeam = game.home.code.take(3)
        val awayTeam = game.away.code.take(3)
        val round = game.round?.round ?: 1
        val date = game.date.substringBefore("T")
        return "${homeTeam}_${awayTeam}_R${round}_${date}".replace(" ", "_")
    }
    
    /**
     * Convierte el estado del partido
     */
    private fun convertStatus(status: String): MatchStatus {
        return when (status.lowercase()) {
            "confirmed" -> MatchStatus.SCHEDULED
            "live" -> MatchStatus.LIVE
            "finished" -> MatchStatus.FINISHED
            "postponed" -> MatchStatus.POSTPONED
            "cancelled" -> MatchStatus.CANCELLED
            else -> MatchStatus.SCHEDULED
        }
    }
    
    /**
     * Extrae el código corto del equipo desde la URL
     */
    private fun extractShortCode(url: String): String? {
        return try {
            // URL ejemplo: "/euroleague/teams/anadolu-efes-istanbul/roster/ist/"
            val parts = url.split("/")
            parts.getOrNull(parts.size - 2)?.uppercase()
        } catch (e: Exception) {
            null
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
}

// Data classes para deserializar el JSON de la API
@Serializable
data class GameCenterResponse(
    val headerData: HeaderData? = null,
    val pageProps: PageProps? = null
)

@Serializable
data class HeaderData(
    val euroleague: EuroLeagueData? = null
)

@Serializable
data class EuroLeagueData(
    val clubs: ClubsData? = null
)

@Serializable
data class ClubsData(
    val clubs: List<Club>? = null
)

@Serializable
data class Club(
    val name: String,
    val url: String,
    val logo: Logo,
    val order: Int
)

@Serializable
data class Logo(
    val image: String,
    val title: String? = null
)

@Serializable
data class PageProps(
    val currentRoundGameGroups: List<GameGroup>? = null
)

@Serializable
data class GameGroup(
    val gameGroupFormattedDay: String,
    val games: List<Game>? = null
)

@Serializable
data class Game(
    val id: String,
    val date: String, // ISO format: "2025-09-30T16:00:00.000Z"
    val status: String,
    val home: TeamInGame,
    val away: TeamInGame,
    val venue: Venue? = null,
    val round: Round? = null
)

@Serializable
data class TeamInGame(
    val name: String,
    val code: String,
    val score: Int = 0,
    val imageUrls: String? = null
)

@Serializable
data class Venue(
    val name: String,
    val capacity: Int = 0,
    val address: String? = null
)

@Serializable
data class Round(
    val round: Int,
    val name: String? = null
)

// ==================== CLASES DTO PARA LA NUEVA API DE FEEDS ====================

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
