package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
import es.itram.basketmatch.domain.entity.Standing

/**
 * Mapeador entre entidades de Standing del dominio y de datos
 */
object StandingMapper {

    fun toDomain(entity: StandingEntity): Standing {
        return Standing(
            teamId = entity.teamId,
            position = entity.position,
            played = entity.played,
            won = entity.won,
            lost = entity.lost,
            pointsFor = entity.pointsFor,
            pointsAgainst = entity.pointsAgainst,
            pointsDifference = entity.pointsDifference,
            seasonType = entity.seasonType
        )
    }

    fun fromDomain(domain: Standing): StandingEntity {
        return StandingEntity(
            teamId = domain.teamId,
            position = domain.position,
            played = domain.played,
            won = domain.won,
            lost = domain.lost,
            pointsFor = domain.pointsFor,
            pointsAgainst = domain.pointsAgainst,
            pointsDifference = domain.pointsDifference,
            seasonType = domain.seasonType
        )
    }

    fun toDomainList(entities: List<StandingEntity>): List<Standing> {
        return entities.map { toDomain(it) }
    }

    fun fromDomainList(domains: List<Standing>): List<StandingEntity> {
        return domains.map { fromDomain(it) }
    }
}
