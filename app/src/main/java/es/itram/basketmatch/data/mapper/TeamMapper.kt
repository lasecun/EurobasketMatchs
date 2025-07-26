package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.domain.entity.Team

/**
 * Mapeador entre entidades de Team del dominio y de datos
 */
object TeamMapper {

    fun toDomain(entity: TeamEntity): Team {
        return Team(
            id = entity.id,
            name = entity.name,
            shortName = entity.shortName,
            code = entity.code,
            logoUrl = entity.logoUrl,
            country = entity.country,
            city = entity.city,
            founded = entity.founded,
            coach = entity.coach,
            website = entity.website,
            primaryColor = entity.primaryColor,
            secondaryColor = entity.secondaryColor,
            isFavorite = entity.isFavorite
        )
    }

    fun fromDomain(domain: Team): TeamEntity {
        return TeamEntity(
            id = domain.id,
            name = domain.name,
            shortName = domain.shortName,
            code = domain.code,
            logoUrl = domain.logoUrl,
            country = domain.country,
            city = domain.city,
            founded = domain.founded,
            coach = domain.coach,
            website = domain.website,
            primaryColor = domain.primaryColor,
            secondaryColor = domain.secondaryColor,
            isFavorite = domain.isFavorite
        )
    }

    fun toDomainList(entities: List<TeamEntity>): List<Team> {
        return entities.map { toDomain(it) }
    }

    fun fromDomainList(domains: List<Team>): List<TeamEntity> {
        return domains.map { fromDomain(it) }
    }
}
