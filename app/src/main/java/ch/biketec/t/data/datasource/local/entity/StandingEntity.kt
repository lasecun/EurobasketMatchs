package ch.biketec.t.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
        Index(value = ["teamId"], unique = true),
        Index(value = ["position"])
    ]
)
data class StandingEntity(
    @PrimaryKey
    val teamId: String,
    val position: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val winPercentage: Double
)
