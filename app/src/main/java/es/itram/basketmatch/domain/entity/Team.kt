package es.itram.basketmatch.domain.entity

/**
 * Entidad de dominio que representa un equipo de la EuroLeague
 */
data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val code: String,
    val country: String,
    val city: String,
    val founded: Int,
    val coach: String,
    val logoUrl: String,
    val isFavorite: Boolean = false
)
