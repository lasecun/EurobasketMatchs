package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.domain.entity.Team

/**
 * Mapper para convertir TeamWebDto a Team de dominio
 */
object TeamWebMapper {
    
    fun toDomain(dto: TeamWebDto): Team {
        return Team(
            id = dto.id ?: "",
            name = dto.name ?: "",
            shortName = dto.name?.take(3)?.uppercase() ?: "", // Generar nombre corto desde el nombre
            code = dto.id ?: "", // Usar ID como código por ahora
            city = "", // No disponible en el DTO
            country = dto.country ?: "",
            logoUrl = dto.logoUrl ?: ""
        )
    }
    
    fun toDomainList(dtos: List<TeamWebDto>): List<Team> {
        return dtos.map { toDomain(it) }
    }
}
