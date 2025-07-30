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
            homeTeamName = getTeamFullName(dto.homeTeamId ?: "") ?: dto.homeTeamName ?: "",
            homeTeamLogo = dto.homeTeamLogo, // Solo usar logo del DTO, sin fallback obsoleto
            awayTeamId = dto.awayTeamId ?: "",
            awayTeamName = getTeamFullName(dto.awayTeamId ?: "") ?: dto.awayTeamName ?: "",
            awayTeamLogo = dto.awayTeamLogo, // Solo usar logo del DTO, sin fallback obsoleto
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
    
    /**
     * Mapea códigos de equipo (TLA) a nombres completos de equipos
     */
    private fun getTeamFullName(teamId: String): String? {
        val tla = extractTlaFromTeamId(teamId)
        
        return when (tla.lowercase()) {
            "ber", "alb" -> "ALBA Berlin"
            "asm", "mon" -> "AS Monaco"
            "bas", "bkn" -> "Baskonia Vitoria-Gasteiz"
            "csk", "csk" -> "CSKA Moscow"
            "efs", "ist" -> "Anadolu Efes Istanbul"
            "fcb", "bar" -> "FC Barcelona"
            "bay", "mun" -> "FC Bayern Munich"
            "mta", "tel" -> "Maccabi Playtika Tel Aviv"
            "oly", "oly" -> "Olympiacos Piraeus"
            "pan", "pan" -> "Panathinaikos AKTOR Athens"
            "par", "prs" -> "Paris Basketball"
            "rea", "mad" -> "Real Madrid"
            "red", "mil" -> "EA7 Emporio Armani Milan"
            "ulk", "fen" -> "Fenerbahce Beko Istanbul"
            "vir", "vir" -> "Virtus Segafredo Bologna"
            "vil", "asv" -> "LDLC ASVEL Villeurbanne"
            "zal", "zal" -> "Zalgiris Kaunas"
            "val", "pam" -> "Valencia Basket"
            else -> null
        }
    }
    
    /**
     * Extrae el TLA de diferentes formatos de teamId
     */
    private fun extractTlaFromTeamId(teamId: String): String {
        return when {
            teamId.length == 3 -> teamId // Ya es TLA
            teamId.contains("_") -> teamId.split("_").lastOrNull()?.take(3) ?: teamId.take(3)
            teamId.length > 3 -> teamId.take(3) // Tomar los primeros 3 caracteres
            else -> teamId
        }
    }
}
