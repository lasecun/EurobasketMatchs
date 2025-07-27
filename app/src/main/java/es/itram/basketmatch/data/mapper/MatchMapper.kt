package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.domain.entity.Match

/**
 * Mapeador entre entidades de Match del dominio y de datos
 */
object MatchMapper {

    fun toDomain(entity: MatchEntity): Match {
        return Match(
            id = entity.id,
            homeTeamId = entity.homeTeamId,
            homeTeamName = entity.homeTeamName,
            homeTeamLogo = entity.homeTeamLogo,
            awayTeamId = entity.awayTeamId,
            awayTeamName = entity.awayTeamName,
            awayTeamLogo = entity.awayTeamLogo,
            dateTime = entity.dateTime,
            venue = entity.venue,
            round = entity.round,
            seasonType = entity.seasonType,
            status = entity.status,
            homeScore = entity.homeScore,
            awayScore = entity.awayScore
        )
    }

    fun fromDomain(domain: Match): MatchEntity {
        return MatchEntity(
            id = domain.id,
            homeTeamId = domain.homeTeamId,
            homeTeamName = domain.homeTeamName,
            homeTeamLogo = domain.homeTeamLogo,
            awayTeamId = domain.awayTeamId,
            awayTeamName = domain.awayTeamName,
            awayTeamLogo = domain.awayTeamLogo,
            dateTime = domain.dateTime,
            venue = domain.venue,
            round = domain.round,
            seasonType = domain.seasonType,
            status = domain.status,
            homeScore = domain.homeScore,
            awayScore = domain.awayScore
        )
    }

    fun toDomainList(entities: List<MatchEntity>): List<Match> {
        return entities.map { toDomain(it) }
    }

    fun fromDomainList(domains: List<Match>): List<MatchEntity> {
        return domains.map { fromDomain(it) }
    }
}
