package es.itram.basketmatch.data.datasource.remote.scraper

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
        
        // URL base de la API JSON de EuroLeague
        private const val BASE_JSON_URL = "https://www.euroleaguebasketball.net/_next/data"
        
        // Build ID - puede cambiar, necesitaremos manejarlo dinámicamente
        private const val BUILD_ID = "a52CgOKFrJehM6XbgT-b_"
        
        // URLs específicas
        private const val GAME_CENTER_JSON_URL = "$BASE_JSON_URL/$BUILD_ID/es/euroleague/game-center.json"
        
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
                        shortCode = extractShortCode(club.url),
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
     * Obtiene partidos directamente de la API JSON
     */
    suspend fun getMatches(season: String = "2025-26"): List<MatchWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "⚽ Obteniendo partidos desde API JSON para temporada $season...")
            
            val jsonResponse = fetchJsonFromUrl(GAME_CENTER_JSON_URL)
            val gameCenterData = json.decodeFromString<GameCenterResponse>(jsonResponse)
            
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
            
            Log.d(TAG, "✅ Partidos obtenidos exitosamente: ${matches.size}")
            matches
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo partidos desde API JSON", e)
            emptyList()
        }
    }
    
    /**
     * Convierte un Game de la API a nuestro MatchWebDto
     */
    private fun convertGameToMatchDto(game: Game, season: String): MatchWebDto {
        // Parsear la fecha ISO 8601
        val dateTime = LocalDateTime.parse(game.date, DateTimeFormatter.ISO_DATE_TIME)
        val dateStr = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val timeStr = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        
        return MatchWebDto(
            id = game.id,
            homeTeamId = game.home.code,
            homeTeamName = game.home.name,
            awayTeamId = game.away.code,
            awayTeamName = game.away.name,
            date = dateStr,
            time = timeStr,
            venue = game.venue?.name ?: "TBD",
            status = convertStatus(game.status),
            homeScore = game.home.score.takeIf { it > 0 },
            awayScore = game.away.score.takeIf { it > 0 },
            round = game.round?.round?.toString() ?: "1",
            season = season
        )
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
