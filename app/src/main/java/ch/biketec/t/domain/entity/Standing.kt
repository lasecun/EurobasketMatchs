package ch.biketec.t.domain.entity

/**
 * Entidad de dominio que representa la clasificación de un equipo
 */
data class Standing(
    val team: Team,
    val position: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val winPercentage: Double
) {
    val pointsDifference: Int
        get() = pointsFor - pointsAgainst
}
