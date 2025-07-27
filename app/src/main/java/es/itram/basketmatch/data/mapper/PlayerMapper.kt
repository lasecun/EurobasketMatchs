package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster

/**
 * Mapper para convertir entre diferentes representaciones de jugadores y rosters
 */
object PlayerMapper {
    
    /**
     * Convierte PlayerDto (de la API) a Player (dominio)
     */
    fun fromDto(dto: PlayerDto): Player {
        return Player(
            code = dto.person.code,
            name = dto.person.name,
            surname = dto.person.surname ?: "",
            fullName = "${dto.person.name} ${dto.person.surname ?: ""}".trim(),
            jersey = dto.jersey ?: dto.dorsalRaw?.toIntOrNull(),
            position = PlayerPosition.fromString(dto.positionName),
            height = dto.person.height?.let { "${it}cm" },
            weight = dto.person.weight?.let { "${it}kg" },
            dateOfBirth = dto.person.dateOfBirth ?: dto.person.birthDate,
            placeOfBirth = dto.person.placeOfBirth,
            nationality = dto.person.nationality,
            experience = null, // No disponible en la API
            profileImageUrl = dto.person.imageUrls?.profile ?: dto.person.imageUrls?.headshot,
            isActive = dto.isActive,
            isStarter = dto.isStarter,
            isCaptain = dto.isCaptain
        )
    }
    
    /**
     * Convierte Player (dominio) a PlayerEntity (base de datos)
     */
    fun toEntity(player: Player, teamCode: String): PlayerEntity {
        return PlayerEntity(
            id = "${teamCode}_${player.code}",
            teamCode = teamCode,
            playerCode = player.code,
            name = player.name,
            surname = player.surname,
            fullName = player.fullName,
            jersey = player.jersey,
            position = player.position?.name,
            height = player.height,
            weight = player.weight,
            dateOfBirth = player.dateOfBirth,
            placeOfBirth = player.placeOfBirth,
            nationality = player.nationality,
            experience = player.experience,
            profileImageUrl = player.profileImageUrl,
            isActive = player.isActive,
            isStarter = player.isStarter,
            isCaptain = player.isCaptain,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Convierte PlayerEntity (base de datos) a Player (dominio)
     */
    fun fromEntity(entity: PlayerEntity): Player {
        return Player(
            code = entity.playerCode,
            name = entity.name,
            surname = entity.surname,
            fullName = entity.fullName,
            jersey = entity.jersey,
            position = entity.position?.let { PlayerPosition.valueOf(it) },
            height = entity.height,
            weight = entity.weight,
            dateOfBirth = entity.dateOfBirth,
            placeOfBirth = entity.placeOfBirth,
            nationality = entity.nationality,
            experience = entity.experience,
            profileImageUrl = entity.profileImageUrl,
            isActive = entity.isActive,
            isStarter = entity.isStarter,
            isCaptain = entity.isCaptain
        )
    }
    
    /**
     * Convierte lista de PlayerDto a lista de PlayerEntity
     */
    fun fromDtoListToEntityList(dtoList: List<PlayerDto>, teamCode: String): List<PlayerEntity> {
        return dtoList.map { dto ->
            val player = fromDto(dto)
            toEntity(player, teamCode)
        }
    }
    
    /**
     * Convierte lista de PlayerEntity a lista de Player
     */
    fun fromEntityListToDomainList(entityList: List<PlayerEntity>): List<Player> {
        return entityList.map { fromEntity(it) }
    }
}

/**
 * Mapper para TeamRoster
 */
object TeamRosterMapper {
    
    /**
     * Convierte TeamRosterEntity a TeamRoster
     */
    fun fromEntity(entity: TeamRosterEntity, players: List<Player>): TeamRoster {
        return TeamRoster(
            teamCode = entity.teamCode,
            teamName = entity.teamName,
            season = entity.season,
            players = players,
            coaches = emptyList() // Por ahora no manejamos coaches
        )
    }
    
    /**
     * Convierte TeamRoster a TeamRosterEntity
     */
    fun toEntity(teamRoster: TeamRoster): TeamRosterEntity {
        return TeamRosterEntity(
            teamCode = teamRoster.teamCode,
            teamName = teamRoster.teamName,
            season = teamRoster.season,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
