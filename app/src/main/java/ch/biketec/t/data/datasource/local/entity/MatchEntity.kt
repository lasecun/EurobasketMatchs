package ch.biketec.t.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.biketec.t.domain.entity.MatchStatus
import ch.biketec.t.domain.entity.SeasonType
import java.time.LocalDateTime

/**
 * Entidad de Room para representar un partido en la base de datos local
 */
@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["homeTeamId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["awayTeamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
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
    val awayTeamId: String,
    val dateTime: LocalDateTime,
    val venue: String,
    val round: Int,
    val status: MatchStatus,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val seasonType: SeasonType
)
