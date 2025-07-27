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
        private const val SCHEDULE_URL = "$BASE_URL/en/euroleague/game-center/"
        private const val STANDINGS_URL = "$BASE_URL/euroleague/standings/"
        private const val GAMES_URL = "$BASE_URL/euroleague/games/"
        
        // User agent para simular navegador
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
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
    suspend fun getMatches(season: String = "2025-26"): List<MatchWebDto> = withContext(Dispatchers.IO) {
        try {
            // Convertir temporada al formato de la API (ej: "2025-26" -> "E2025")
            val seasonCode = convertSeasonToCode(season)
            val url = "$SCHEDULE_URL?season=$seasonCode"
            
            Log.d(TAG, "Iniciando scraping de partidos desde: $url")
            
            val document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(15000)
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
     * Convierte el formato de temporada a código de la API
     */
    private fun convertSeasonToCode(season: String): String {
        return when (season) {
            "2025-26" -> "E2025"
            "2024-25" -> "E2024"
            "2023-24" -> "E2023"
            else -> "E2025" // Default a 2025-26
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
            // Primero intentar extraer datos JSON de los scripts
            val jsonMatches = extractMatchesFromJsonData(document, season)
            if (jsonMatches.isNotEmpty()) {
                Log.d(TAG, "Partidos extraídos de datos JSON: ${jsonMatches.size}")
                return jsonMatches
            }
            
            // Fallback a selectores CSS si no hay datos JSON
            val matchElements = document.select(".game-card, .GameCard, .match-card, .match-item, [data-game-id], .game-item, .gamecard") +
                               document.select("div[class*='game'], div[class*='match'], div[class*='Game']") +
                               document.select(".fixture, .fixture-item")
            
            Log.d(TAG, "Elementos de partidos encontrados: ${matchElements.size}")
            
            if (matchElements.isEmpty()) {
                val allDivs = document.select("div")
                Log.d(TAG, "Total divs en la página: ${allDivs.size}")
                
                val possibleMatches = allDivs.filter { div ->
                    val text = div.text().lowercase()
                    text.contains("vs") || text.contains("v.") || 
                    (text.contains("real") && text.contains("madrid")) ||
                    (text.contains("barcelona") && text.length < 200)
                }
                
                Log.d(TAG, "Posibles partidos encontrados por contenido: ${possibleMatches.size}")
                matches.addAll(possibleMatches.mapIndexed { index, element ->
                    parseMatchElement(element, index, season)
                }.filterNotNull())
            } else {
                matchElements.forEachIndexed { index, element ->
                    try {
                        val matchDto = parseMatchElement(element, index, season)
                        if (matchDto != null) {
                            matches.add(matchDto)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parseando partido $index", e)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error general parseando partidos", e)
        }
        
        return matches
    }
    
    /**
     * Extrae partidos de los datos JSON embebidos en la página
     */
    private fun extractMatchesFromJsonData(document: Document, season: String): List<MatchWebDto> {
        val matches = mutableListOf<MatchWebDto>()
        
        try {
            // Buscar el script que contiene __NEXT_DATA__
            val scripts = document.select("script")
            for (script in scripts) {
                val scriptContent = script.html()
                
                // Buscar el script que contiene __NEXT_DATA__ y currentRoundGameGroups
                if (scriptContent.contains("__NEXT_DATA__") && scriptContent.contains("currentRoundGameGroups")) {
                    Log.d(TAG, "Encontrado script con __NEXT_DATA__ y datos de partidos")
                    
                    // Buscar los partidos usando un método más directo
                    // Buscar todos los objetos que contengan "home" y "away" con nombres de equipos
                    val teamNamesPattern = """("name":\s*"[^"]*(?:Barcelona|Real Madrid|Panathinaikos|Olympiacos|Fenerbahce|Anadolu Efes|Partizan|Crvena Zvezda|Monaco|Bayern|Baskonia|Valencia|ASVEL|Zalgiris|Maccabi|Hapoel|Milan|Dubai|Paris)[^"]*")"""
                    val teamRegex = Regex(teamNamesPattern, RegexOption.IGNORE_CASE)
                    
                    // Buscar patrones de fechas de 2025
                    val datePattern = """"date":\s*"(2025-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{3}Z)""""
                    val dateRegex = Regex(datePattern)
                    
                    // Encontrar todas las fechas
                    val dateMatches = dateRegex.findAll(scriptContent)
                    
                    dateMatches.forEach { dateMatch ->
                        try {
                            val fullDate = dateMatch.groupValues[1] // "2025-09-30T16:00:00.000Z"
                            val gameDate = fullDate.substring(0, 10) // "2025-09-30"
                            val gameTime = fullDate.substring(11, 16) // "16:00"
                            
                            // Buscar el contexto alrededor de esta fecha para encontrar los equipos
                            val dateIndex = dateMatch.range.first
                            val contextStart = maxOf(0, dateIndex - 2000)
                            val contextEnd = minOf(scriptContent.length, dateIndex + 2000)
                            val context = scriptContent.substring(contextStart, contextEnd)
                            
                            // Buscar equipos en este contexto
                            val homeTeamPattern = """"home":\s*\{[^}]*"name":\s*"([^"]+)"[^}]*"code":\s*"([^"]+)""""
                            val awayTeamPattern = """"away":\s*\{[^}]*"name":\s*"([^"]+)"[^}]*"code":\s*"([^"]+)""""
                            
                            val homeMatch = Regex(homeTeamPattern).find(context)
                            val awayMatch = Regex(awayTeamPattern).find(context)
                            
                            if (homeMatch != null && awayMatch != null) {
                                val homeTeamName = homeMatch.groupValues[1]
                                val homeTeamCode = homeMatch.groupValues[2]
                                val awayTeamName = awayMatch.groupValues[1]
                                val awayTeamCode = awayMatch.groupValues[2]
                                
                                // Buscar ID del partido
                                val idPattern = """"id":\s*"([^"]+)""""
                                val idMatch = Regex(idPattern).find(context)
                                val matchId = idMatch?.groupValues?.get(1) ?: "generated_${matches.size}"
                                
                                // Buscar venue
                                val venuePattern = """"venue":\s*\{[^}]*"name":\s*"([^"]+)""""
                                val venueMatch = Regex(venuePattern).find(context)
                                val venue = venueMatch?.groupValues?.get(1) ?: "TBD"
                                
                                val match = MatchWebDto(
                                    id = matchId,
                                    homeTeamId = homeTeamCode,
                                    homeTeamName = homeTeamName,
                                    homeTeamLogo = null, // No disponible en web scraping
                                    awayTeamId = awayTeamCode,
                                    awayTeamName = awayTeamName,
                                    awayTeamLogo = null, // No disponible en web scraping
                                    date = gameDate,
                                    time = gameTime,
                                    venue = venue,
                                    status = MatchStatus.SCHEDULED,
                                    homeScore = null,
                                    awayScore = null,
                                    round = "1", // Por defecto Round 1
                                    season = season
                                )
                                
                                matches.add(match)
                                Log.d(TAG, "Partido extraído: $homeTeamName vs $awayTeamName el $gameDate")
                            }
                            
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parseando contexto de fecha: ${e.message}")
                        }
                    }
                    
                    if (matches.isNotEmpty()) {
                        break // Si encontramos partidos, no necesitamos seguir buscando
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Error extrayendo datos JSON: ${e.message}")
        }
        
        Log.d(TAG, "Total de partidos extraídos: ${matches.size}")
        return matches.distinctBy { it.id } // Eliminar duplicados
    }
    
    /**
     * Extrae un valor JSON simple usando regex
     */
    private fun extractJsonValue(json: String, key: String): String? {
        return try {
            val pattern = "\"$key\":\\s*\"([^\"]*)\""
            val regex = Regex(pattern)
            regex.find(json)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extrae un valor JSON anidado
     */
    private fun extractNestedJsonValue(json: String, parentKey: String, childKey: String): String? {
        return try {
            val parentPattern = "\"$parentKey\":\\s*\\{([^\\}]+)\\}"
            val parentRegex = Regex(parentPattern)
            val parentMatch = parentRegex.find(json)?.groupValues?.get(1)
            
            if (parentMatch != null) {
                extractJsonValue(parentMatch, childKey)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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
                homeTeamLogo = null, // No disponible en web scraping
                awayTeamId = teams[1].second,
                awayTeamName = teams[1].first,
                awayTeamLogo = null, // No disponible en web scraping
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
