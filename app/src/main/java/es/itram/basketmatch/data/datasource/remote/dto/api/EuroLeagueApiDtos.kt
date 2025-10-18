package es.itram.basketmatch.data.datasource.remote.dto.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 🏀 DTOs para la API oficial de EuroLeague
 *
 * Estos DTOs mapean las respuestas JSON de la API oficial de EuroLeague
 * https://api-live.euroleague.net/swagger/
 */

// ============================================================================
// COMPETICIONES
// ============================================================================

@Serializable
data class CompetitionsResponseDto(
    @SerialName("data") val data: List<CompetitionDto>
)

@Serializable
data class CompetitionDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("seasonCode") val seasonCode: String? = null
)

// ============================================================================
// TEMPORADA
// ============================================================================

@Serializable
data class SeasonResponseDto(
    @SerialName("data") val data: SeasonDto
)

@Serializable
data class SeasonDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("competitionCode") val competitionCode: String,
    @SerialName("startDate") val startDate: String,
    @SerialName("endDate") val endDate: String
)

// ============================================================================
// EQUIPOS
// ============================================================================

@Serializable
data class TeamsResponseDto(
    @SerialName("data") val data: List<TeamApiDto>
)

@Serializable
data class TeamDetailsResponseDto(
    @SerialName("data") val data: TeamApiDto
)

@Serializable
data class TeamApiDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("tvName") val tvName: String? = null,
    @SerialName("clubName") val clubName: String? = null,
    @SerialName("imageUrls") val imageUrls: TeamImageUrlsDto? = null,
    @SerialName("country") val country: CountryDto? = null,
    @SerialName("venue") val venue: VenueDto? = null
)

@Serializable
data class TeamImageUrlsDto(
    @SerialName("logo") val logo: String? = null,
    @SerialName("logoDark") val logoDark: String? = null,
    @SerialName("logoHorizontal") val logoHorizontal: String? = null
)

@Serializable
data class CountryDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String
)

@Serializable
data class VenueDto(
    @SerialName("name") val name: String,
    @SerialName("capacity") val capacity: Int? = null,
    @SerialName("city") val city: String? = null
)

// ============================================================================
// PARTIDOS
// ============================================================================

@Serializable
data class GamesResponseDto(
    @SerialName("data") val data: List<GameApiDto>
)

@Serializable
data class GameDetailsResponseDto(
    @SerialName("data") val data: GameApiDto
)

@Serializable
data class GameApiDto(
    @SerialName("gameCode") val code: String?,
    @SerialName("date") val date: String?,
    @SerialName("local") val local: GameTeamDto?,
    @SerialName("road") val road: GameTeamDto?,
    @SerialName("venue") val venue: VenueDto? = null,
    @SerialName("phase") val phase: PhaseDto? = null,
    @SerialName("round") val round: RoundDto? = null,
    @SerialName("gameState") val gameState: GameStateDto? = null,
    @SerialName("boxscore") val boxscore: BoxscoreDto? = null
)

@Serializable
data class GameTeamDto(
    @SerialName("club") val club: TeamApiDto,
    @SerialName("score") val score: Int? = null
)

@Serializable
data class PhaseDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String
)

@Serializable
data class RoundDto(
    @SerialName("number") val number: Int,
    @SerialName("name") val name: String? = null
)

@Serializable
data class GameStateDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String
)

@Serializable
data class BoxscoreDto(
    @SerialName("local") val local: TeamScoreDto,
    @SerialName("road") val road: TeamScoreDto
)

@Serializable
data class TeamScoreDto(
    @SerialName("score") val score: Int,
    @SerialName("partialResults") val partialResults: List<Int>? = null
)

// ============================================================================
// ESTADÍSTICAS
// ============================================================================

@Serializable
data class GameStatsResponseDto(
    @SerialName("data") val data: GameStatsDto
)

@Serializable
data class GameStatsDto(
    @SerialName("local") val local: TeamStatsDto,
    @SerialName("road") val road: TeamStatsDto
)

@Serializable
data class TeamStatsDto(
    @SerialName("club") val club: TeamApiDto,
    @SerialName("playersStats") val playersStats: List<PlayerStatsDto>
)

@Serializable
data class PlayerStatsDto(
    @SerialName("person") val person: PlayerDto,
    @SerialName("stats") val stats: Map<String, String>? = null
)

// ============================================================================
// JUGADORES
// ============================================================================

@Serializable
data class TeamRosterResponseDto(
    @SerialName("data") val data: List<PlayerDto>
)

@Serializable
data class PlayerResponseDto(
    @SerialName("data") val data: PlayerDto
)

@Serializable
data class PlayerDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("imageUrls") val imageUrls: PlayerImageUrlsDto? = null,
    @SerialName("position") val position: String? = null,
    @SerialName("height") val height: String? = null,
    @SerialName("birthDate") val birthDate: String? = null,
    @SerialName("country") val country: CountryDto? = null,
    @SerialName("dorsal") val dorsal: Int? = null
)

@Serializable
data class PlayerImageUrlsDto(
    @SerialName("profile") val profile: String? = null,
    @SerialName("headshot") val headshot: String? = null
)

// ============================================================================
// CLASIFICACIONES
// ============================================================================

@Serializable
data class StandingsResponseDto(
    @SerialName("data") val data: List<StandingDto>
)

@Serializable
data class StandingDto(
    @SerialName("club") val club: TeamApiDto,
    @SerialName("position") val position: Int,
    @SerialName("gamesPlayed") val gamesPlayed: Int,
    @SerialName("gamesWon") val gamesWon: Int,
    @SerialName("gamesLost") val gamesLost: Int,
    @SerialName("pointsFor") val pointsFor: Int,
    @SerialName("pointsAgainst") val pointsAgainst: Int,
    @SerialName("pointsDifference") val pointsDifference: Int
)
