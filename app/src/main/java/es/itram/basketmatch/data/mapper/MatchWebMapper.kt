package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus as WebMatchStatus
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mapper para convertir MatchWebDto a Match de dominio
 */
object MatchWebMapper {
    
    fun toDomain(dto: MatchWebDto): Match {
        val dateTime = parseDateTime(dto.date, dto.time)
        
        return Match(
            id = dto.id ?: "",
            homeTeamId = dto.homeTeamId ?: "",
            homeTeamName = dto.homeTeamName ?: "",
            homeTeamLogo = null, // No disponible en DTO
            awayTeamId = dto.awayTeamId ?: "",
            awayTeamName = dto.awayTeamName ?: "",
            awayTeamLogo = null, // No disponible en DTO
            dateTime = dateTime,
            venue = dto.venue ?: "",
            round = dto.round?.toIntOrNull() ?: 1,
            status = mapStatus(dto.status),
            homeScore = dto.homeScore,
            awayScore = dto.awayScore,
            seasonType = SeasonType.REGULAR
        )
    }
    
    fun toDomainList(dtos: List<MatchWebDto>): List<Match> {
        return dtos.map { toDomain(it) }
    }
    
    private fun parseDateTime(date: String?, time: String?): LocalDateTime {
        return try {
            val dateStr = date ?: LocalDateTime.now().toString().split("T")[0]
            if (time != null) {
                LocalDateTime.parse("${dateStr}T${time}:00")
            } else {
                LocalDateTime.parse("${dateStr}T20:00:00")
            }
        } catch (e: Exception) {
            LocalDateTime.now().plusDays(1)
        }
    }
    
    private fun mapStatus(webStatus: WebMatchStatus?): MatchStatus {
        return when (webStatus) {
            WebMatchStatus.SCHEDULED -> MatchStatus.SCHEDULED
            WebMatchStatus.LIVE -> MatchStatus.LIVE
            WebMatchStatus.FINISHED -> MatchStatus.FINISHED
            WebMatchStatus.POSTPONED -> MatchStatus.POSTPONED
            WebMatchStatus.CANCELLED -> MatchStatus.CANCELLED
            null -> MatchStatus.SCHEDULED
        }
    }
}
