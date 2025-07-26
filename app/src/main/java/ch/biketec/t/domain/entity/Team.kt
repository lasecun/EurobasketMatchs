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
    val venue: String,
    val website: String? = null,
    val founded: Int? = null
)
