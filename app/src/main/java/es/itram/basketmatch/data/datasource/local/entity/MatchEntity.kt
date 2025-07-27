package es.itram.basketmatch.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import java.time.LocalDateTime

/**
 * Entidad de Room para representar un partido en la base de datos local
 */
@Entity(
    tableName = "matches",
    indices = [
        Index(value = ["homeTeamId"]),
        Index(value = ["awayTeamId"]),
        Index(value = ["dateTime"]),
        Index(value = ["status"]),
        Index(value = ["seasonType"])
    ]
)
data class MatchEntity(
    @PrimaryKey
    val id: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamLogo: String?,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamLogo: String?,
    val dateTime: LocalDateTime,
    val venue: String,
    val round: Int,
    val status: MatchStatus,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val seasonType: SeasonType
)
