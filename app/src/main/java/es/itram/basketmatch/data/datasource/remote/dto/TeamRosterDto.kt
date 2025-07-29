package es.itram.basketmatch.data.datasource.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data classes para la respuesta de roster de equipos de la API de EuroLeague Feeds
 * API endpoint: /competitions/E/seasons/E2025/clubs/{tla}/people
 * 
 * La API devuelve directamente un array de jugadores con la estructura actualizada de 2025
 */

@Serializable
data class PlayerDto(
    val person: PersonDto,
    val type: String, // "J" para jugador, "E" para entrenador
    val typeName: String, // "Player" o "Coach"
    val active: Boolean = true,
    val startDate: String? = null,
    val endDate: String? = null,
    val order: Int = 0,
    val dorsal: String? = null, // Número de camiseta como string
    val dorsalRaw: String? = null,
    val position: Int? = null, // 1=Guard, 2=Forward, 3=Center
    val positionName: String? = null,
    val lastTeam: String? = null,
    val externalId: Int = 0,
    val images: PlayerImageUrls? = null,
    val club: PlayerClubDto? = null,
    val season: PlayerSeasonDto? = null
)

@Serializable
data class PersonDto(
    val code: String? = null,
    val name: String,
    val alias: String? = null,
    val aliasRaw: String? = null,
    val passportName: String? = null,
    val passportSurname: String? = null,
    val jerseyName: String? = null,
    val abbreviatedName: String? = null,
    val country: CountryDto? = null,
    val height: Int? = null, // En centímetros
    val weight: Int? = null, // En kilogramos
    val birthDate: String? = null, // Formato ISO: "1995-12-15T00:00:00"
    val birthCountry: CountryDto? = null,
    val twitterAccount: String? = null,
    val instagramAccount: String? = null,
    val facebookAccount: String? = null,
    val isReferee: Boolean = false,
    val images: PlayerImageUrls? = null
)

@Serializable
data class CountryDto(
    val code: String, // "ESP", "USA", etc.
    val name: String  // "Spain", "United States of America", etc.
)

@Serializable
data class PlayerImageUrls(
    val profile: String? = null,
    val headshot: String? = null
)

@Serializable
data class PlayerClubDto(
    val code: String,
    val name: String,
    val abbreviatedName: String? = null,
    val editorialName: String? = null,
    val tvCode: String? = null,
    val isVirtual: Boolean = false,
    val images: PlayerClubImageUrls? = null
)

@Serializable
data class PlayerClubImageUrls(
    val crest: String? = null
)

@Serializable
data class PlayerSeasonDto(
    val name: String, // "EuroLeague 2025-26"
    val code: String, // "E2025"
    val alias: String? = null, // "2025-26"
    val competitionCode: String? = null, // "E"
    val year: Int, // 2025
    val startDate: String? = null // "2025-07-01T00:00:00"
)

// Type alias para facilitar el uso - la API devuelve directamente un array
typealias TeamRosterResponse = List<PlayerDto>
