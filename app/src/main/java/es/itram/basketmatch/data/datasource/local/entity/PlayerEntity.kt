package es.itram.basketmatch.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de base de datos para un jugador
 */
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey
    val id: String, // Combinación de teamCode + playerCode
    val teamCode: String,
    val playerCode: String,
    val name: String,
    val surname: String,
    val fullName: String,
    val jersey: Int?,
    val position: String?, // Almacenamos como String
    val height: String?,
    val weight: String?,
    val dateOfBirth: String?,
    val placeOfBirth: String?,
    val nationality: String?,
    val experience: Int?,
    val profileImageUrl: String?,
    val isActive: Boolean = true,
    val isStarter: Boolean = false,
    val isCaptain: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
