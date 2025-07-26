package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus as WebMatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Mapper para convertir DTOs web de partidos a entidades de dominio
 */
object MatchWebMapper {
    
    fun toDomain(dto: MatchWebDto): Match {
        return Match(
            id = dto.id,
            homeTeamId = dto.homeTeamId,
            awayTeamId = dto.awayTeamId,
            dateTime = parseDateTime(dto.date, dto.time),
            venue = dto.venue ?: "TBD",
            round = parseRound(dto.round),
            status = parseMatchStatus(dto.status),
            homeScore = dto.homeScore,
            awayScore = dto.awayScore,
            seasonType = SeasonType.REGULAR // Por defecto, ya que no viene en el DTO
        )
    }
    
    fun toDomainList(dtos: List<MatchWebDto>): List<Match> {
        return dtos.map { toDomain(it) }
    }
    
    private fun parseDateTime(date: String?, time: String?): LocalDateTime {
        if (date.isNullOrBlank()) {
            return LocalDateTime.now()
        }
        
        val dateTimeStr = if (!time.isNullOrBlank()) {
            "$date $time"
        } else {
            date
        }
        
        return try {
            // Formato esperado: "yyyy-MM-dd HH:mm" o similares
            val formats = listOf(
                "yyyy-MM-dd HH:mm",
                "dd/MM/yyyy HH:mm",
                "dd-MM-yyyy HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd"
            )
            
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    val date = sdf.parse(dateTimeStr)
                    return date?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.toLocalDateTime() ?: LocalDateTime.now()
                } catch (e: Exception) {
                    // Continuar con el siguiente formato
                }
            }
            
            LocalDateTime.now()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
    
    private fun parseRound(roundStr: String?): Int {
        if (roundStr.isNullOrBlank()) return 1
        
        return try {
            // Intentar extraer número de strings como "Round 1", "Jornada 5", etc.
            val numberRegex = "\\d+".toRegex()
            val match = numberRegex.find(roundStr)
            match?.value?.toInt() ?: 1
        } catch (e: Exception) {
            1
        }
    }
    
    private fun parseMatchStatus(status: WebMatchStatus?): MatchStatus {
        return when (status) {
            WebMatchStatus.SCHEDULED -> MatchStatus.SCHEDULED
            WebMatchStatus.LIVE -> MatchStatus.LIVE
            WebMatchStatus.FINISHED -> MatchStatus.FINISHED
            WebMatchStatus.CANCELLED -> MatchStatus.CANCELLED
            WebMatchStatus.POSTPONED -> MatchStatus.POSTPONED
            null -> MatchStatus.SCHEDULED
        }
    }
}
