package es.itram.basketmatch.data.datasource.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para representar un equipo en la base de datos local
 */
@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val shortName: String,
    val code: String,
    val city: String,
    val country: String,
    val logoUrl: String,
    val founded: Int = 0,
    val coach: String = "",
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
