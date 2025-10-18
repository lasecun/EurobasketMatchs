package es.itram.basketmatch.data.datasource.remote.dto.api

import com.google.gson.annotations.SerializedName

/**
 * 🏀 DTOs para la API oficial de EuroLeague
 *
 * Estos DTOs mapean las respuestas JSON de la API oficial de EuroLeague
 * https://api-live.euroleague.net/swagger/
 *
 * IMPORTANTE: Estos DTOs usan SOLO anotaciones de Gson (@SerializedName)
 * NO usar @Serializable de Kotlinx porque interfiere con Gson
 */

// ============================================================================
// EQUIPOS
// ============================================================================

data class TeamsResponseDto(
    @SerializedName("data") val data: List<TeamApiDto>
)

data class TeamDetailsResponseDto(
    @SerializedName("data") val data: TeamApiDto
)

data class TeamApiDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("tvName") val tvName: String? = null,
    @SerializedName("abbreviatedName") val abbreviatedName: String? = null,
    @SerializedName("editorialName") val editorialName: String? = null,
    @SerializedName("clubName") val clubName: String? = null,
    @SerializedName("images") val images: TeamImagesDto? = null,  // ✅ Correcto: la API usa "images"
    @SerializedName("country") val country: CountryDto? = null,
    @SerializedName("venue") val venue: VenueDto? = null
)

data class TeamImagesDto(  // ✅ Correcto: renombrado de "TeamImageUrlsDto"
    @SerializedName("crest") val crest: String? = null,  // ✅ PRINCIPAL: logo del escudo del equipo
    @SerializedName("logo") val logo: String? = null,
    @SerializedName("logoDark") val logoDark: String? = null,
    @SerializedName("logoHorizontal") val logoHorizontal: String? = null
)

data class CountryDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)

data class VenueDto(
    @SerializedName("name") val name: String,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("city") val city: String? = null
)

// ============================================================================
// PARTIDOS
// ============================================================================

data class GamesResponseDto(
    @SerializedName("data") val data: List<GameApiDto>
)

data class GameDetailsResponseDto(
    @SerializedName("data") val data: GameApiDto
)

data class GameApiDto(
    @SerializedName("gameCode") val code: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("local") val local: GameTeamDto?,
    @SerializedName("road") val road: GameTeamDto?,
    @SerializedName("venue") val venue: VenueDto? = null,
    @SerializedName("phase") val phase: PhaseDto? = null,
    @SerializedName("round") val round: RoundDto? = null,
    @SerializedName("gameState") val gameState: GameStateDto? = null,
    @SerializedName("boxscore") val boxscore: BoxscoreDto? = null
)

data class GameTeamDto(
    @SerializedName("club") val club: TeamApiDto,
    @SerializedName("score") val score: Int? = null
)

data class PhaseDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)

data class RoundDto(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String? = null
)

data class GameStateDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)

data class BoxscoreDto(
    @SerializedName("local") val local: TeamScoreDto,
    @SerializedName("road") val road: TeamScoreDto
)

data class TeamScoreDto(
    @SerializedName("score") val score: Int,
    @SerializedName("partialResults") val partialResults: List<Int>? = null
)

// ============================================================================
// ESTADÍSTICAS
// ============================================================================

data class GameStatsResponseDto(
    @SerializedName("data") val data: GameStatsDto
)

data class GameStatsDto(
    @SerializedName("local") val local: TeamStatsDto,
    @SerializedName("road") val road: TeamStatsDto
)

data class TeamStatsDto(
    @SerializedName("club") val club: TeamApiDto,
    @SerializedName("playersStats") val playersStats: List<PlayerStatsDto>
)

data class PlayerStatsDto(
    @SerializedName("person") val person: PlayerDto,
    @SerializedName("stats") val stats: Map<String, String>? = null
)

// ============================================================================
// ROSTER DE EQUIPOS Y JUGADORES
// ============================================================================

// La API devuelve directamente un array de jugadores, no un objeto con campo data
typealias TeamRosterResponseDto = List<PlayerDto>

data class PlayerResponseDto(
    @SerializedName("data") val data: PlayerDto
)

/**
 * DTO para jugadores que mapea la estructura REAL de la API oficial
 * La API devuelve objetos anidados: person, images, club, season
 */
data class PlayerDto(
    @SerializedName("person") val person: PersonDto,
    @SerializedName("type") val type: String? = null, // "J" = Jugador, "C" = Coach, "Z" = Staff
    @SerializedName("typeName") val typeName: String? = null,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("dorsal") val dorsal: String? = null,
    @SerializedName("dorsalRaw") val dorsalRaw: String? = null,
    @SerializedName("position") val position: Int? = null, // Número de posición
    @SerializedName("positionName") val positionName: String? = null, // Nombre de la posición
    @SerializedName("images") val images: PlayerImageUrlsDto? = null,
    @SerializedName("club") val club: TeamApiDto? = null,
    @SerializedName("season") val season: SeasonDto? = null
) {
    // Propiedades computadas para compatibilidad con código existente
    val code: String? get() = person.code
    val name: String? get() = person.name
    val firstName: String? get() = person.passportName
    val lastName: String? get() = person.passportSurname
    val imageUrls: PlayerImageUrlsDto? get() = images
    val height: String? get() = person.height?.let { "${it}cm" }
    val birthDate: String? get() = person.birthDate
    val country: CountryDto? get() = person.country

    // Propiedad computada para obtener el dorsal como Int de forma segura
    val dorsalNumber: Int? get() = dorsal?.toIntOrNull() ?: dorsalRaw?.toIntOrNull()

    // Propiedad computada para obtener un nombre válido
    val validName: String get() = person.name ?: person.passportName ?: person.passportSurname ?: "Unknown"

    // Propiedad computada para obtener un código válido (genera uno si no existe)
    val validCode: String get() {
        if (!person.code.isNullOrBlank()) return person.code

        // Generar código basado en nombre y dorsal
        val namePart = (person.passportName ?: person.name ?: person.passportSurname ?: "UNK").take(3).uppercase()
        val dorsalPart = (dorsal ?: dorsalRaw ?: "00").take(2).padStart(2, '0')
        return "${namePart}${dorsalPart}"
    }
}

/**
 * DTO para la información personal del jugador (objeto anidado "person")
 */
data class PersonDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("passportName") val passportName: String? = null,
    @SerializedName("passportSurname") val passportSurname: String? = null,
    @SerializedName("jerseyName") val jerseyName: String? = null,
    @SerializedName("abbreviatedName") val abbreviatedName: String? = null,
    @SerializedName("country") val country: CountryDto? = null,
    @SerializedName("height") val height: Int? = null, // En centímetros
    @SerializedName("weight") val weight: Int? = null, // En kilogramos
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("birthCountry") val birthCountry: CountryDto? = null,
    @SerializedName("twitterAccount") val twitterAccount: String? = null,
    @SerializedName("instagramAccount") val instagramAccount: String? = null,
    @SerializedName("facebookAccount") val facebookAccount: String? = null,
    @SerializedName("isReferee") val isReferee: Boolean = false
)

data class PlayerImageUrlsDto(
    @SerializedName("action") val action: String? = null,      // ✅ CORRECTO: imagen principal del jugador
    @SerializedName("headshot") val headshot: String? = null,  // ✅ Imagen headshot
    @SerializedName("profile") val profile: String? = null     // Mantener por compatibilidad si existe
)

// ============================================================================
// CLASIFICACIONES
// ============================================================================

data class StandingsResponseDto(
    @SerializedName("data") val data: List<StandingDto>
)

data class StandingDto(
    @SerializedName("club") val club: TeamApiDto,
    @SerializedName("position") val position: Int,
    @SerializedName("gamesPlayed") val gamesPlayed: Int,
    @SerializedName("gamesWon") val gamesWon: Int,
    @SerializedName("gamesLost") val gamesLost: Int,
    @SerializedName("pointsFor") val pointsFor: Int,
    @SerializedName("pointsAgainst") val pointsAgainst: Int,
    @SerializedName("pointsDifference") val pointsDifference: Int
)

// ============================================================================
// COMPETICIONES Y TEMPORADAS (mantienen @Serializable si se usan con Kotlinx)
// ============================================================================

data class CompetitionsResponseDto(
    @SerializedName("data") val data: List<CompetitionDto>
)

data class CompetitionDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("seasonCode") val seasonCode: String? = null
)

data class SeasonResponseDto(
    @SerializedName("data") val data: SeasonDto
)

data class SeasonDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("competitionCode") val competitionCode: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String
)
