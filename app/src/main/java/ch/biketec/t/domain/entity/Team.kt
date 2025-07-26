package ch.biketec.t.domain.entity

/**
 * Entidad de dominio que representa un equipo de la EuroLeague
 */
data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val city: String,
    val country: String,
    val logoUrl: String,
    val primaryColor: String,
    val secondaryColor: String,
    val isFavorite: Boolean = false
)
