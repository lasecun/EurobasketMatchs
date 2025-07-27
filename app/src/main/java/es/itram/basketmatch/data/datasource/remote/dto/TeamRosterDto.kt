package es.itram.basketmatch.data.datasource.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data classes para la respuesta de roster de equipos de la API de EuroLeague Feeds
 * API endpoint: /competitions/E/seasons/E2025/clubs/{tla}/people
 * 
 * La API devuelve directamente un array de jugadores, no un objeto wrapper
 */

@Serializable
data class PlayerDto(
    val person: PersonDto,
    val jersey: Int? = null,
    val dorsalRaw: String? = null, // Campo alternativo para jersey
    val position: Int? = null, // Viene como número, no string
    val positionName: String? = null, // Campo alternativo como string
    val isActive: Boolean = true,
    val isStarter: Boolean = false,
    val isCaptain: Boolean = false
)

@Serializable
data class PersonDto(
    val code: String,
    val name: String,
    val surname: String? = null, // Campo opcional, algunos jugadores no tienen surname
    val height: Int? = null, // Viene como número, no string
    val weight: Int? = null,
    val dateOfBirth: String? = null,
    val birthDate: String? = null, // Campo alternativo
    val placeOfBirth: String? = null,
    val nationality: String? = null,
    val imageUrls: PlayerImageUrls? = null
)

@Serializable
data class PlayerImageUrls(
    val profile: String? = null,
    val headshot: String? = null
)

// Type alias para facilitar el uso - la API devuelve directamente un array
typealias TeamRosterResponse = List<PlayerDto>
