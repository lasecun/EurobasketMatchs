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
// JUGADORES
// ============================================================================

data class TeamRosterResponseDto(
    @SerializedName("data") val data: List<PlayerDto>
)

data class PlayerResponseDto(
    @SerializedName("data") val data: PlayerDto
)

data class PlayerDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("imageUrls") val imageUrls: PlayerImageUrlsDto? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("height") val height: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("country") val country: CountryDto? = null,
    @SerializedName("dorsal") val dorsal: Int? = null
)

data class PlayerImageUrlsDto(
    @SerializedName("profile") val profile: String? = null,
    @SerializedName("headshot") val headshot: String? = null
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
