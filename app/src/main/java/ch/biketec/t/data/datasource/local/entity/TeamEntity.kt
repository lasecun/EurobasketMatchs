package ch.biketec.t.data.datasource.local.entity

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
    val city: String,
    val country: String,
    val logoUrl: String,
    val venue: String,
    val website: String? = null,
    val founded: Int? = null
)
