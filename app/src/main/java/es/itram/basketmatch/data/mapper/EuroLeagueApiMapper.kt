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
            logoUrl = this.imageUrls?.logo,
            country = this.country?.name,
            venue = this.venue?.name,
            profileUrl = "" // La API oficial no proporciona URL de perfil
        )
    }

    /**
     * Convierte GameApiDto (API oficial) a MatchWebDto (proyecto existente)
     */
    fun GameApiDto.toMatchWebDto(): MatchWebDto {
        return MatchWebDto(
            id = this.code,
            homeTeamId = this.local.club.code,
            homeTeamName = this.local.club.tvName ?: this.local.club.name,
            homeTeamLogo = this.local.club.imageUrls?.logo,
            awayTeamId = this.road.club.code,
            awayTeamName = this.road.club.tvName ?: this.road.club.name,
            awayTeamLogo = this.road.club.imageUrls?.logo,
            date = this.date.substring(0, 10), // Extraer solo fecha (YYYY-MM-DD)
            time = this.date.substring(11, 16), // Extraer solo hora (HH:MM)
            venue = this.venue?.name,
            status = this.gameState.toMatchStatus(),
            homeScore = this.local.score ?: this.boxscore?.local?.score,
            awayScore = this.road.score ?: this.boxscore?.road?.score,
            round = this.round?.name ?: "Jornada ${this.round?.number ?: ""}",
            season = "2024-25" // Temporada actual
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
        return this.map { it.toMatchWebDto() }
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
            imageUrl = this.imageUrls?.profile ?: this.imageUrls?.headshot
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
