package es.itram.basketmatch.domain.model

/**
 * Modelo de dominio para el roster de un equipo
 */
data class TeamRoster(
    val teamCode: String,
    val teamName: String,
    val season: String,
    val players: List<Player>,
    val coaches: List<Coach> = emptyList()
)

/**
 * Modelo de dominio para un jugador
 */
data class Player(
    val code: String,
    val name: String,
    val surname: String,
    val fullName: String,
    val jersey: Int?,
    val position: PlayerPosition?,
    val height: String?,
    val dateOfBirth: String?,
    val placeOfBirth: String?,
    val nationality: String?,
    val experience: Int?,
    val profileImageUrl: String?,
    val isActive: Boolean = true,
    val isStarter: Boolean = false,
    val isCaptain: Boolean = false
)

/**
 * Modelo de dominio para un entrenador
 */
data class Coach(
    val code: String,
    val name: String,
    val role: CoachRole,
    val nationality: String? = null,
    val profileImageUrl: String? = null
)

/**
 * Posiciones de jugadores en baloncesto
 */
enum class PlayerPosition(val displayName: String, val shortName: String) {
    POINT_GUARD("Base", "PG"),
    SHOOTING_GUARD("Escolta", "SG"), 
    SMALL_FORWARD("Alero", "SF"),
    POWER_FORWARD("Ala-Pívot", "PF"),
    CENTER("Pívot", "C"),
    GUARD("Guard", "G"),
    FORWARD("Forward", "F"),
    UNKNOWN("Desconocido", "?");
    
    companion object {
        fun fromString(position: String?): PlayerPosition {
            return when (position?.uppercase()) {
                "PG", "POINT GUARD", "BASE" -> POINT_GUARD
                "SG", "SHOOTING GUARD", "ESCOLTA" -> SHOOTING_GUARD
                "SF", "SMALL FORWARD", "ALERO" -> SMALL_FORWARD
                "PF", "POWER FORWARD", "ALA-PIVOT", "ALA-PÍVOT" -> POWER_FORWARD
                "C", "CENTER", "PIVOT", "PÍVOT" -> CENTER
                "G", "GUARD" -> GUARD
                "F", "FORWARD" -> FORWARD
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Roles de entrenadores
 */
enum class CoachRole(val displayName: String) {
    HEAD_COACH("Entrenador Principal"),
    ASSISTANT_COACH("Entrenador Asistente"),
    PHYSICAL_TRAINER("Preparador Físico"),
    TEAM_MANAGER("Manager del Equipo"),
    UNKNOWN("Desconocido");
    
    companion object {
        fun fromString(role: String?): CoachRole {
            return when (role?.uppercase()) {
                "HEAD COACH", "ENTRENADOR PRINCIPAL" -> HEAD_COACH
                "ASSISTANT COACH", "ENTRENADOR ASISTENTE" -> ASSISTANT_COACH
                "PHYSICAL TRAINER", "PREPARADOR FÍSICO" -> PHYSICAL_TRAINER
                "TEAM MANAGER", "MANAGER" -> TEAM_MANAGER
                else -> UNKNOWN
            }
        }
    }
}
