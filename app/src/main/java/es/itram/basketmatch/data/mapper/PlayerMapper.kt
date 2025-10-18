package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster

/**
 * Mapper para convertir entre diferentes representaciones de jugadores y rosters
 * ACTUALIZADO: Mapea correctamente la estructura anidada de la API oficial
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
     * Genera una URL de imagen placeholder basada en las iniciales del jugador
     * Maneja de forma segura strings vacíos, null y caracteres especiales
     */
    private fun generatePlaceholderImageUrl(playerName: String?): String {
        // Si el nombre es null, está vacío o es solo espacios, usar "??" como iniciales
        if (playerName.isNullOrBlank()) {
            return "https://ui-avatars.com/api/?name=??&size=400&background=004996&color=ffffff&font-size=0.4"
        }

        val initials = try {
            playerName.trim()
                .split(" ", "-", "_")
                .filter { it.isNotBlank() }
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
                .ifEmpty { "??" }
        } catch (_: Exception) {
            "??"
        }

        // Usar un servicio de avatares con las iniciales
        return "https://ui-avatars.com/api/?name=$initials&size=400&background=004996&color=ffffff&font-size=0.4"
    }

    /**
     * Convierte PlayerDto de la API oficial a Player (modelo de dominio)
     * Usa la estructura anidada REAL: person.name, person.height, images.action, etc.
     */
    fun fromApiDto(
        dto: es.itram.basketmatch.data.datasource.remote.dto.api.PlayerDto,
        teamCode: String
    ): Player {
        val playerCode = dto.validCode // Usar validCode que siempre devuelve un valor válido
        val playerName = dto.validName // Usar el nombre válido que nunca es null

        // Usar las imágenes que vienen directamente en el DTO desde la API oficial
        // La API usa los campos "action" (principal) y "headshot"
        val actionImage = dto.images?.action
        val headshotImage = dto.images?.headshot
        val profileImage = dto.images?.profile // Por compatibilidad

        // Prioridad: action > profile > headshot > placeholder
        val imageUrl = actionImage
            ?: profileImage
            ?: headshotImage
            ?: generatePlaceholderImageUrl(playerName)

        return Player(
            code = playerCode,
            name = dto.person.passportName ?: dto.person.name ?: playerName,
            surname = dto.person.passportSurname ?: dto.person.jerseyName ?: "",
            fullName = playerName,
            jersey = dto.dorsalNumber, // Usar la propiedad computada que maneja strings vacíos
            position = PlayerPosition.fromString(dto.positionName), // ✅ Usar positionName (String), no position (Int)
            height = dto.height, // Ya formateado como "XXXcm"
            weight = dto.person.weight?.let { "${it}kg" },
            dateOfBirth = dto.person.birthDate,
            placeOfBirth = dto.person.birthCountry?.name ?: dto.person.country?.name,
            nationality = dto.person.country?.name,
            experience = null, // No disponible en la API
            profileImageUrl = imageUrl,
            isActive = dto.active,
            isStarter = false, // No disponible en la API oficial
            isCaptain = false  // No disponible en la API oficial
        )
    }

    /**
     * Convierte PlayerDto del scraper (DEPRECATED - solo para compatibilidad)
     * Este método se mantiene para el scraper viejo pero ya no se usa
     */
    fun fromDto(dto: PlayerDto, teamCode: String): Player {
        val playerCode = dto.person.code ?: generatePlayerCode(
            dto.person.name, 
            dto.person.passportSurname, 
            dto.dorsal
        )
        
        // Usar las imágenes que vienen directamente en el PlayerDto desde feeds API
        val profileImage = dto.images?.profile
        val headshotImage = dto.images?.headshot
        
        // Simplemente usar las imágenes de la API o generar placeholder
        val imageUrl = profileImage
            ?: headshotImage 
            ?: generatePlaceholderImageUrl(dto.person.name)
        
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
