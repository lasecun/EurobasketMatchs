package es.itram.basketmatch.data.datasource.remote.dto

/**
 * DTO para representar un equipo obtenido desde las APIs remotas
 */
data class TeamWebDto(
    val id: String,
    val name: String,
    val fullName: String,
    val shortCode: String,
    val logoUrl: String? = null,
    val country: String? = null,
    val venue: String? = null,
    val profileUrl: String = ""
)

/**
 * DTO para representar un partido obtenido desde las APIs remotas
 */
data class MatchWebDto(
    val id: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamLogo: String? = null,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamLogo: String? = null,
    val date: String,
    val time: String? = null,
    val venue: String? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val round: String? = null,
    val season: String = "2025-26"
)

/**
 * Estado de un partido
 */
enum class MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED,
    CANCELLED
}
