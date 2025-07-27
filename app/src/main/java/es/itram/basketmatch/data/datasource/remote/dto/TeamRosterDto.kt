package es.itram.basketmatch.data.datasource.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data classes para la respuesta de roster de equipos de la API de EuroLeague Feeds
 * API endpoint: /competitions/E/seasons/E2025/clubs/{tla}/people
 */

@Serializable
data class TeamRosterResponse(
    val status: String,
    val data: List<PlayerDto>,
    val metadata: RosterMetadata? = null
)

@Serializable
data class PlayerDto(
    val code: String,
    val name: String,
    val surname: String,
    val fullName: String? = null,
    val jersey: Int? = null,
    val position: String? = null,
    val positionFull: String? = null,
    val height: String? = null,
    val dateOfBirth: String? = null,
    val placeOfBirth: String? = null,
    val nationality: String? = null,
    val experience: Int? = null,
    val imageUrls: PlayerImageUrls? = null,
    val isActive: Boolean = true,
    val isStarter: Boolean = false,
    val isCaptain: Boolean = false
)

@Serializable
data class PlayerImageUrls(
    val profile: String? = null,
    val headshot: String? = null
)

@Serializable
data class RosterMetadata(
    val createdAt: String? = null,
    val totalItems: Int = 0,
    val season: String? = null,
    val team: String? = null
)
