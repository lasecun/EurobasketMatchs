package es.itram.basketmatch.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de base de datos para un roster de equipo
 */
@Entity(tableName = "team_rosters")
data class TeamRosterEntity(
    @PrimaryKey
    val teamCode: String,
    val teamName: String,
    val season: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
