package es.itram.basketmatch.domain.entity

import java.time.LocalDateTime

/**
 * Entidad de dominio que representa un partido de la EuroLeague
 */
data class Match(
    val id: String,
    val homeTeamId: String,
    val awayTeamId: String,
    val dateTime: LocalDateTime,
    val venue: String,
    val round: Int,
    val status: MatchStatus,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val seasonType: SeasonType = SeasonType.REGULAR
)

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED,
    CANCELLED
}

enum class SeasonType {
    REGULAR,
    PLAYOFFS,
    FINAL_FOUR
}
