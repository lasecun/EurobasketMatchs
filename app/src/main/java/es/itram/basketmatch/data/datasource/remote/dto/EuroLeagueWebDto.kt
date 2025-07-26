package es.itram.basketmatch.data.datasource.remote.dto

/**
 * DTO para equipos obtenidos del web scraping de EuroLeague
 */
data class TeamWebDto(
    val id: String,
    val name: String,
    val fullName: String,
    val shortCode: String,
    val logoUrl: String?,
    val country: String?,
    val venue: String?,
    val profileUrl: String
)

/**
 * DTO para partidos obtenidos del web scraping
 */
data class MatchWebDto(
    val id: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val awayTeamId: String,
    val awayTeamName: String,
    val date: String,
    val time: String?,
    val venue: String?,
    val status: MatchStatus,
    val homeScore: Int?,
    val awayScore: Int?,
    val round: String?,
    val season: String
)

/**
 * Estados posibles de un partido
 */
enum class MatchStatus {
    SCHEDULED,    // Programado
    LIVE,         // En vivo
    FINISHED,     // Terminado
    POSTPONED,    // Pospuesto
    CANCELLED     // Cancelado
}

/**
 * DTO para la respuesta de la clasificación
 */
data class StandingsWebDto(
    val teams: List<TeamStandingDto>,
    val season: String,
    val lastUpdated: String
)

/**
 * DTO para la posición de un equipo en la clasificación
 */
data class TeamStandingDto(
    val position: Int,
    val teamId: String,
    val teamName: String,
    val wins: Int,
    val losses: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val pointsDifference: Int,
    val isPlayoffQualified: Boolean
)
