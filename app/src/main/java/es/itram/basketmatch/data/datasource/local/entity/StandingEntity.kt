package es.itram.basketmatch.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import es.itram.basketmatch.domain.entity.SeasonType

/**
 * Entidad de Room para representar la clasificación en la base de datos local
 */
@Entity(
    tableName = "standings",
    indices = [
        Index(value = ["teamId"]),
        Index(value = ["position"]),
        Index(value = ["seasonType"])
    ]
)
data class StandingEntity(
    @PrimaryKey
    val teamId: String,
    val position: Int,
    val played: Int,
    val won: Int,
    val lost: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val pointsDifference: Int,
    val seasonType: SeasonType
)
