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
            homeTeamLogo = dto.homeTeamLogo ?: getTeamLogoUrl(dto.homeTeamId ?: ""),
            awayTeamId = dto.awayTeamId ?: "",
            awayTeamName = getTeamFullName(dto.awayTeamId ?: "") ?: dto.awayTeamName ?: "",
            awayTeamLogo = dto.awayTeamLogo ?: getTeamLogoUrl(dto.awayTeamId ?: ""),
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
     * Mapea códigos de equipo a URLs de logos de equipos
     * Basado en los TLA (Three Letter Acronym) de los equipos de EuroLeague
     */
    private fun getTeamLogoUrl(teamId: String): String? {
        // Convertir el teamId a TLA (algunos pueden venir con formato diferente)
        val tla = extractTlaFromTeamId(teamId)
        
        return when (tla.lowercase()) {
            "ber", "alb" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/alba-berlin.png"
            "asm", "mon" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/as-monaco.png"
            "bas", "bkn" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/baskonia-vitoria-gasteiz.png"
            "csk", "csk" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/cska-moscow.png"
            "efs", "ist" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/anadolu-efes-istanbul.png"
            "fcb", "bar" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fc-barcelona.png"
            "bay", "mun" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fc-bayern-munich.png"
            "mta", "tel" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/maccabi-playtika-tel-aviv.png"
            "oly", "oly" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/olympiacos-piraeus.png"
            "pan", "pan" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/panathinaikos-aktor-athens.png"
            "par", "prs" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/paris-basketball.png"
            "rea", "mad" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/real-madrid.png"
            "red", "mil" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/ea7-emporio-armani-milan.png"
            "ulk", "fen" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fenerbahce-beko-istanbul.png"
            "vir", "vir" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/virtus-segafredo-bologna.png"
            "vil", "asv" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/ldlc-asvel-villeurbanne.png"
            "zal", "zal" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/zalgiris-kaunas.png"
            "val", "pam" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/valencia-basket.png"
            else -> null
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
