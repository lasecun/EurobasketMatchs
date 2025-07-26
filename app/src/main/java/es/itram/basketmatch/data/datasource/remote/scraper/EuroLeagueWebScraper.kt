package es.itram.basketmatch.data.datasource.remote.scraper

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para extraer datos de la web oficial de EuroLeague
 */
@Singleton
class EuroLeagueWebScraper @Inject constructor() {
    
    companion object {
        private const val TAG = "EuroLeagueWebScraper"
        private const val BASE_URL = "https://www.euroleaguebasketball.net"
        private const val TEAMS_URL = "$BASE_URL/euroleague/teams/"
        private const val SCHEDULE_URL = "$BASE_URL/euroleague/schedule/"
        private const val STANDINGS_URL = "$BASE_URL/euroleague/standings/"
        private const val GAMES_URL = "$BASE_URL/euroleague/games/"
        
        // User agent para simular navegador
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Android SDK built for x86) AppleWebKit/537.36"
    }
    
    /**
     * Obtiene la lista de equipos de EuroLeague
     */
    suspend fun getTeams(): List<TeamWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando scraping de equipos desde: $TEAMS_URL")
            
            val document = Jsoup.connect(TEAMS_URL)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
                
            val teams = parseTeamsFromDocument(document)
            Log.d(TAG, "Equipos extraídos: ${teams.size}")
            teams
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener equipos", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene los partidos del calendario
     */
    suspend fun getMatches(season: String = "2024-25"): List<MatchWebDto> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando scraping de partidos desde: $SCHEDULE_URL")
            
            val document = Jsoup.connect(SCHEDULE_URL)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
                
            val matches = parseMatchesFromDocument(document, season)
            Log.d(TAG, "Partidos extraídos: ${matches.size}")
            matches
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener partidos", e)
            emptyList()
        }
    }
    
    /**
     * Parsea los equipos del documento HTML
     */
    private fun parseTeamsFromDocument(document: Document): List<TeamWebDto> {
        val teams = mutableListOf<TeamWebDto>()
        
        try {
            // Buscar elementos de equipos en la página
            // La estructura puede variar, por lo que usamos múltiples selectores
            val teamElements = document.select("a[href*='/teams/']") + 
                              document.select(".team-card") + 
                              document.select("[data-team-id]")
            
            teamElements.forEachIndexed { index, element ->
                try {
                    val teamDto = parseTeamElement(element, index)
                    if (teamDto != null) {
                        teams.add(teamDto)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parseando equipo en elemento: ${element.text()}", e)
                }
            }
            
            // Fallback: buscar texto que contenga nombres de equipos conocidos
            if (teams.isEmpty()) {
                teams.addAll(extractTeamsFromText(document))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error general parseando equipos", e)
        }
        
        return teams.distinctBy { it.id }
    }
    
    /**
     * Parsea un elemento de equipo individual
     */
    private fun parseTeamElement(element: Element, index: Int): TeamWebDto? {
        return try {
            val href = element.attr("href")
            val teamName = element.text().trim()
            
            if (teamName.isBlank()) return null
            
            // Extraer ID del equipo desde la URL o usar índice
            val teamId = extractTeamIdFromUrl(href) ?: "team_$index"
            
            // Buscar logo
            val logoElement = element.select("img").firstOrNull()
            val logoUrl = logoElement?.attr("src")?.let { 
                if (it.startsWith("http")) it else "$BASE_URL$it"
            }
            
            TeamWebDto(
                id = teamId,
                name = teamName,
                fullName = teamName,
                shortCode = generateShortCode(teamName),
                logoUrl = logoUrl,
                country = extractCountryFromText(teamName),
                venue = null, // Se podría obtener en una segunda pasada
                profileUrl = if (href.startsWith("http")) href else "$BASE_URL$href"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parseando elemento de equipo", e)
            null
        }
    }
    
    /**
     * Extrae equipos de texto cuando no se encuentran elementos estructurados
     */
    private fun extractTeamsFromText(document: Document): List<TeamWebDto> {
        val knownTeams = listOf(
            "Real Madrid", "FC Barcelona", "Panathinaikos", "Olympiacos",
            "Fenerbahce", "Anadolu Efes", "CSKA Moscow", "Baskonia",
            "Valencia Basket", "Zalgiris Kaunas", "Maccabi Tel Aviv",
            "AS Monaco", "Bayern Munich", "Virtus Bologna", "Milan",
            "Red Star Belgrade", "Partizan Belgrade"
        )
        
        val foundTeams = mutableListOf<TeamWebDto>()
        val documentText = document.text().lowercase()
        
        knownTeams.forEachIndexed { index, teamName ->
            if (documentText.contains(teamName.lowercase())) {
                foundTeams.add(
                    TeamWebDto(
                        id = "team_${index + 1}",
                        name = teamName,
                        fullName = teamName,
                        shortCode = generateShortCode(teamName),
                        logoUrl = null,
                        country = getKnownTeamCountry(teamName),
                        venue = null,
                        profileUrl = "$BASE_URL/euroleague/teams/"
                    )
                )
            }
        }
        
        return foundTeams
    }
    
    /**
     * Parsea partidos del documento HTML
     */
    private fun parseMatchesFromDocument(document: Document, season: String): List<MatchWebDto> {
        val matches = mutableListOf<MatchWebDto>()
        
        try {
            // Buscar elementos de partidos
            val matchElements = document.select(".game-card") + 
                               document.select("[data-game-id]") +
                               document.select(".match-item")
            
            matchElements.forEachIndexed { index, element ->
                try {
                    val matchDto = parseMatchElement(element, index, season)
                    if (matchDto != null) {
                        matches.add(matchDto)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parseando partido", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error general parseando partidos", e)
        }
        
        return matches
    }
    
    /**
     * Parsea un elemento de partido individual
     */
    private fun parseMatchElement(element: Element, index: Int, season: String): MatchWebDto? {
        return try {
            val matchText = element.text()
            
            // ID único para el partido
            val matchId = element.attr("data-game-id").ifEmpty { "match_$index" }
            
            // Extraer equipos (esto es una aproximación, la estructura real puede variar)
            val teams = extractTeamsFromMatchText(matchText)
            if (teams.size < 2) return null
            
            MatchWebDto(
                id = matchId,
                homeTeamId = teams[0].second,
                homeTeamName = teams[0].first,
                awayTeamId = teams[1].second,
                awayTeamName = teams[1].first,
                date = extractDateFromElement(element),
                time = extractTimeFromElement(element),
                venue = null,
                status = extractMatchStatus(element),
                homeScore = extractScore(element, true),
                awayScore = extractScore(element, false),
                round = null,
                season = season
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parseando elemento de partido", e)
            null
        }
    }
    
    // Métodos auxiliares
    private fun extractTeamIdFromUrl(url: String): String? {
        return url.split("/").lastOrNull { it.isNotBlank() }
    }
    
    private fun generateShortCode(teamName: String): String {
        return teamName.split(" ")
            .take(2)
            .joinToString("") { it.take(3).uppercase() }
            .take(6)
    }
    
    private fun extractCountryFromText(teamName: String): String? {
        val countryMappings = mapOf(
            "madrid" to "Spain",
            "barcelona" to "Spain",
            "baskonia" to "Spain",
            "valencia" to "Spain",
            "panathinaikos" to "Greece",
            "olympiacos" to "Greece",
            "fenerbahce" to "Turkey",
            "efes" to "Turkey",
            "bayern" to "Germany",
            "milan" to "Italy",
            "virtus" to "Italy",
            "monaco" to "Monaco",
            "maccabi" to "Israel",
            "zalgiris" to "Lithuania"
        )
        
        val lowerName = teamName.lowercase()
        return countryMappings.entries.find { 
            lowerName.contains(it.key) 
        }?.value
    }
    
    private fun getKnownTeamCountry(teamName: String): String {
        return extractCountryFromText(teamName) ?: "Unknown"
    }
    
    private fun extractTeamsFromMatchText(matchText: String): List<Pair<String, String>> {
        // Implementación simplificada - en la realidad habría que analizar mejor la estructura
        val parts = matchText.split(" vs ", " - ", " x ")
        return if (parts.size >= 2) {
            listOf(
                Pair(parts[0].trim(), generateTeamId(parts[0].trim())),
                Pair(parts[1].trim(), generateTeamId(parts[1].trim()))
            )
        } else {
            emptyList()
        }
    }
    
    private fun generateTeamId(teamName: String): String {
        return teamName.lowercase().replace(" ", "_")
    }
    
    private fun extractDateFromElement(element: Element): String {
        // Buscar fecha en varios posibles selectores
        val dateElement = element.select(".date, .game-date, [data-date]").firstOrNull()
        return dateElement?.text() ?: ""
    }
    
    private fun extractTimeFromElement(element: Element): String? {
        val timeElement = element.select(".time, .game-time, [data-time]").firstOrNull()
        return timeElement?.text()?.takeIf { it.isNotBlank() }
    }
    
    private fun extractMatchStatus(element: Element): MatchStatus {
        val statusText = element.select(".status, .game-status").text().lowercase()
        return when {
            statusText.contains("live") -> MatchStatus.LIVE
            statusText.contains("final") || statusText.contains("finished") -> MatchStatus.FINISHED
            statusText.contains("postponed") -> MatchStatus.POSTPONED
            statusText.contains("cancelled") -> MatchStatus.CANCELLED
            else -> MatchStatus.SCHEDULED
        }
    }
    
    private fun extractScore(element: Element, isHome: Boolean): Int? {
        val scoreElements = element.select(".score, .game-score")
        return try {
            val scoreText = scoreElements.text()
            val scores = scoreText.split("-", " ")
                .mapNotNull { it.trim().toIntOrNull() }
            
            if (scores.size >= 2) {
                if (isHome) scores[0] else scores[1]
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
