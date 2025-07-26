package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.domain.entity.Team

/**
 * Mapper para convertir DTOs web de equipos a entidades de dominio
 */
object TeamWebMapper {
    
    fun toDomain(dto: TeamWebDto): Team {
        return Team(
            id = dto.id,
            name = dto.name,
            shortName = dto.shortCode,
            code = dto.shortCode,
            city = extractCityFromVenue(dto.venue),
            country = dto.country ?: "Unknown",
            logoUrl = dto.logoUrl ?: "",
            isFavorite = false // Por defecto no es favorito
        )
    }
    
    fun toDomainList(dtos: List<TeamWebDto>): List<Team> {
        return dtos.map { toDomain(it) }
    }
    
    private fun extractCityFromVenue(venue: String?): String {
        if (venue.isNullOrBlank()) return "Unknown"
        
        // Intentar extraer la ciudad del formato "Arena, Ciudad"
        val parts = venue.split(",")
        return if (parts.size >= 2) {
            parts[1].trim()
        } else {
            // Si no hay coma, usar el venue completo
            venue.trim()
        }
    }
}
