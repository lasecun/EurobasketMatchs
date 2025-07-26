package ch.biketec.t.domain.entity

import java.time.LocalDateTime

/**
 * Entidad de dominio que representa un partido de la EuroLeague
 */
data class Match(
    val id: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val dateTime: LocalDateTime,
    val arena: String,
    val city: String,
    val round: Int,
    val status: MatchStatus,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val seasonType: SeasonType = SeasonType.REGULAR_SEASON
)

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED,
    CANCELLED
}

enum class SeasonType {
    REGULAR_SEASON,
    PLAYOFFS,
    FINAL_FOUR
}
