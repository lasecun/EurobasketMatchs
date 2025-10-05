package es.itram.basketmatch.data.datasource.remote.dto

/**
 * DTO simplificado para jugadores desde la API oficial de EuroLeague
 * Este DTO contiene los campos esenciales de un jugador
 */
data class SimplePlayerDto(
    val code: String,
    val name: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val position: String? = null,
    val dorsal: Int? = null,
    val height: String? = null,
    val country: String? = null,
    val imageUrl: String? = null
)
