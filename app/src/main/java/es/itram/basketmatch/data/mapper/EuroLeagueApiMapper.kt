package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.datasource.remote.dto.api.*

/**
 * 🔄 Mappers para convertir DTOs de la API oficial de EuroLeague
 * a los DTOs existentes del proyecto
 */
object EuroLeagueApiMapper {

    /**
     * Convierte TeamApiDto (API oficial) a TeamWebDto (proyecto existente)
     */
    fun TeamApiDto.toTeamWebDto(): TeamWebDto {
        return TeamWebDto(
            id = this.code,
            name = this.tvName ?: this.name,
            fullName = this.clubName ?: this.name,
            shortCode = this.code,
            logoUrl = this.images?.crest ?: this.images?.logo,  // ✅ Correcto: usar "crest" primero, "logo" como fallback
            country = this.country?.name,
            venue = this.venue?.name,
            profileUrl = "" // La API oficial no proporciona URL de perfil
        )
    }

    /**
     * Convierte GameApiDto (API oficial) a MatchWebDto (proyecto existente)
     * Devuelve null si faltan datos críticos
     */
    fun GameApiDto.toMatchWebDto(): MatchWebDto? {
        // Validar que los campos críticos no sean null
        val gameCode = this.code
        val gameDate = this.date
        val localTeam = this.local
        val roadTeam = this.road

        if (gameCode == null || gameDate == null || localTeam == null || roadTeam == null) {
            android.util.Log.w("ApiMapper", "⚠️ Partido con datos incompletos ignorado: code=$gameCode, date=$gameDate")
            return null
        }

        // Logging detallado para debugging de marcadores
        android.util.Log.d("ApiMapper", "🔍 Partido: $gameCode")
        android.util.Log.d("ApiMapper", "   Estado RAW: '${this.gameState?.code}' - '${this.gameState?.name}'")
        android.util.Log.d("ApiMapper", "   ${localTeam.club.code} score: ${localTeam.score}")
        android.util.Log.d("ApiMapper", "   ${roadTeam.club.code} score: ${roadTeam.score}")
        android.util.Log.d("ApiMapper", "   Boxscore local: ${this.boxscore?.local?.score}")
        android.util.Log.d("ApiMapper", "   Boxscore road: ${this.boxscore?.road?.score}")
        android.util.Log.d("ApiMapper", "   Logo local: ${localTeam.club.images?.crest}")  // ✅ Correcto: usar "images.crest"
        android.util.Log.d("ApiMapper", "   Logo visitante: ${roadTeam.club.images?.crest}")  // ✅ Correcto: usar "images.crest"

        // Priorizar boxscore sobre score directo (más confiable para partidos finalizados)
        val homeScore = this.boxscore?.local?.score ?: localTeam.score
        val awayScore = this.boxscore?.road?.score ?: roadTeam.score

        // Mapear estado, pero si es null e inferir FINISHED si hay marcadores
        val mappedStatus = when {
            this.gameState != null -> this.gameState.toMatchStatus()
            homeScore != null && awayScore != null && (homeScore > 0 || awayScore > 0) -> {
                // Si hay marcadores pero gameState es null, inferir que está finalizado
                android.util.Log.d("ApiMapper", "   ⚠️ gameState es null pero hay marcadores → Inferido como FINISHED")
                MatchStatus.FINISHED
            }
            else -> MatchStatus.SCHEDULED
        }

        android.util.Log.d("ApiMapper", "   Estado mapeado: $mappedStatus")
        android.util.Log.d("ApiMapper", "   Marcadores finales: $homeScore - $awayScore")

        return MatchWebDto(
            id = gameCode,
            homeTeamId = localTeam.club.code,
            homeTeamName = localTeam.club.tvName ?: localTeam.club.name,
            homeTeamLogo = localTeam.club.images?.crest ?: localTeam.club.images?.logo,  // ✅ Correcto: usar "crest" primero
            awayTeamId = roadTeam.club.code,
            awayTeamName = roadTeam.club.tvName ?: roadTeam.club.name,
            awayTeamLogo = roadTeam.club.images?.crest ?: roadTeam.club.images?.logo,  // ✅ Correcto: usar "crest" primero
            date = gameDate.substring(0, 10), // Extraer solo fecha (YYYY-MM-DD)
            time = if (gameDate.length > 11) gameDate.substring(11, 16) else null, // Extraer solo hora (HH:MM)
            venue = this.venue?.name,
            status = mappedStatus,
            homeScore = homeScore,
            awayScore = awayScore,
            round = this.round?.name ?: "Jornada ${this.round?.number ?: ""}",
            season = "2025-26"
        )
    }

    /**
     * Convierte el estado del partido de la API oficial al enum local
     */
    private fun GameStateDto.toMatchStatus(): MatchStatus {
        return when (this.code.uppercase()) {
            "SCHEDULED", "PRE" -> MatchStatus.SCHEDULED
            "LIVE", "1Q", "2Q", "3Q", "4Q", "OT", "OT2", "OT3" -> MatchStatus.LIVE
            "FINAL", "FINISHED" -> MatchStatus.FINISHED
            "POSTPONED" -> MatchStatus.POSTPONED
            "CANCELLED" -> MatchStatus.CANCELLED
            else -> MatchStatus.SCHEDULED
        }
    }

    /**
     * Convierte una lista de equipos de la API oficial
     */
    fun List<TeamApiDto>.toTeamWebDtoList(): List<TeamWebDto> {
        return this.map { it.toTeamWebDto() }
    }

    /**
     * Convierte una lista de partidos de la API oficial
     */
    fun List<GameApiDto>.toMatchWebDtoList(): List<MatchWebDto> {
        return this.mapNotNull { it.toMatchWebDto() }
    }

    /**
     * Convierte PlayerDto a un formato más simple para roster
     */
    fun PlayerDto.toSimplePlayer(): SimplePlayerDto {
        return SimplePlayerDto(
            code = this.code,
            name = this.name,
            firstName = this.firstName,
            lastName = this.lastName,
            position = this.position,
            dorsal = this.dorsal,
            height = this.height,
            country = this.country?.name,
            imageUrl = this.imageUrls?.profile ?: this.imageUrls?.headshot  // ✅ Correcto: jugadores usan "imageUrls"
        )
    }

    /**
     * Convierte una lista de jugadores
     */
    fun List<PlayerDto>.toSimplePlayerList(): List<SimplePlayerDto> {
        return this.map { it.toSimplePlayer() }
    }
}

/**
 * DTO simplificado para jugadores
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
