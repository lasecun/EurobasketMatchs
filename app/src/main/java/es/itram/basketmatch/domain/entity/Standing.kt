package es.itram.basketmatch.domain.entity

/**
 * Entidad de dominio que representa la clasificación de un equipo
 */
data class Standing(
    val teamId: String,
    val position: Int,
    val played: Int,
    val won: Int,
    val lost: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val pointsDifference: Int,
    val seasonType: SeasonType
) {
    val winPercentage: Double
        get() = if (played > 0) (won.toDouble() / played.toDouble()) * 100 else 0.0
}
