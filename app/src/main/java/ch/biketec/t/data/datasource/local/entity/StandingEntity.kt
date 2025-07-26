package ch.biketec.t.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.biketec.t.domain.entity.SeasonType

/**
 * Entidad de Room para representar la clasificación en la base de datos local
 */
@Entity(
    tableName = "standings",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["teamId"]),
        Index(value = ["position"]),
        Index(value = ["seasonType"])
    ]
)
data class StandingEntity(
    @PrimaryKey
    val id: String,
    val teamId: String,
    val position: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val seasonType: SeasonType
)
