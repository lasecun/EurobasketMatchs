package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.utils.PlayerImageUtil

/**
 * Mapper para convertir entre diferentes representaciones de jugadores y rosters
 */
object PlayerMapper {
    
    /**
     * Genera un código único para jugadores que no tienen código en la API
     */
    private fun generatePlayerCode(name: String, surname: String?, jersey: String?): String {
        val namePart = name.take(3).uppercase()
        val surnamePart = surname?.take(3)?.uppercase() ?: "XXX"
        val jerseyPart = jersey?.take(2) ?: "00"
        return "${namePart}${surnamePart}${jerseyPart}"
    }
    
    /**
     * Convierte PlayerDto a Player (modelo de dominio)
     * Obtiene la imagen del jugador desde la web oficial o genera un placeholder
     */
    suspend fun fromDto(dto: PlayerDto, teamCode: String, imageUtil: PlayerImageUtil): Player {
        val playerCode = dto.person.code ?: generatePlayerCode(
            dto.person.name, 
            dto.person.passportSurname, 
            dto.dorsal
        )
        
        // Intentar obtener la imagen desde la web oficial o usar placeholder
        val imageUrl = PlayerImageUtil.DEFAULT_PLAYER_IMAGES[playerCode] 
            ?: imageUtil.getPlayerImageUrl(playerCode, dto.person.name, teamCode)
            ?: imageUtil.generatePlaceholderImageUrl(dto.person.name)
        
        return Player(
            code = playerCode,
            name = dto.person.name,
            surname = dto.person.passportSurname ?: dto.person.jerseyName ?: "",
            fullName = "${dto.person.name} ${dto.person.passportSurname ?: ""}".trim(),
            jersey = dto.dorsal?.toIntOrNull() ?: dto.dorsalRaw?.toIntOrNull(),
            position = PlayerPosition.fromString(dto.positionName),
            height = dto.person.height?.let { "${it}cm" },
            weight = dto.person.weight?.let { "${it}kg" },
            dateOfBirth = dto.person.birthDate,
            placeOfBirth = dto.person.birthCountry?.name,
            nationality = dto.person.country?.name,
            experience = null, // No disponible en la API
            profileImageUrl = imageUrl,
            isActive = dto.active,
            isStarter = false, // No disponible en la nueva API
            isCaptain = false  // No disponible en la nueva API
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
     * Filtra solo los jugadores (type="J"), excluyendo entrenadores
     * Nota: Esta función ya no se usa con las nuevas imágenes, se mantiene para compatibilidad
     */
    suspend fun fromDtoListToEntityList(dtoList: List<PlayerDto>, teamCode: String, imageUtil: PlayerImageUtil): List<PlayerEntity> {
        return dtoList
            .filter { it.type == "J" } // Solo jugadores, no entrenadores
            .map { dto ->
                val player = fromDto(dto, teamCode, imageUtil)
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
            coaches = emptyList(), // Por ahora no manejamos coaches
            logoUrl = entity.logoUrl
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
            logoUrl = teamRoster.logoUrl,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
