package es.itram.basketmatch.data.mapper

import android.util.Log
import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

/**
 * Mapper para convertir entre diferentes representaciones de jugadores y rosters
 */
object PlayerMapper {
    
    private const val TAG = "PlayerMapper"
    private val httpClient = OkHttpClient()
    
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
     */
    private fun generatePlaceholderImageUrl(playerName: String): String {
        val initials = playerName.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercase() ?: "" }
            .joinToString("")
        
        // Usar un servicio de avatares con las iniciales
        return "https://ui-avatars.com/api/?name=$initials&size=400&background=004996&color=ffffff&font-size=0.4"
    }
    
    /**
     * Obtiene la URL de imagen real del jugador desde el sitio web oficial
     */
    private suspend fun getPlayerImageFromWeb(playerCode: String?, playerName: String): String? = withContext(Dispatchers.IO) {
        try {
            if (playerCode.isNullOrBlank()) return@withContext null
            
            val normalizedName = playerName.lowercase()
                .replace(" ", "-")
                .replace(",", "")
                .replace(".", "")
                .replace("'", "")
            
            val url = "https://www.euroleaguebasketball.net/euroleague/players/$normalizedName/$playerCode/"
            Log.d(TAG, "🌐 Trying to get image for $playerName from: $url")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Android 14)")
                .build()
            
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            
            val html = response.body?.string() ?: return@withContext null
            
            // Buscar el patrón "photo":"https://media-cdn.incrowdsports.com/[UUID].png"
            val pattern = Pattern.compile("\"photo\":\"(https://media-cdn\\.incrowdsports\\.com/[^\"]+\\.(?:png|jpg))\"")
            val matcher = pattern.matcher(html)
            
            if (matcher.find()) {
                val imageUrl = matcher.group(1)
                Log.d(TAG, "✅ Found image for $playerName: $imageUrl")
                return@withContext imageUrl
            }
            
            Log.d(TAG, "❌ No image found for $playerName")
            return@withContext null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image for $playerName", e)
            return@withContext null
        }
    }
    
    /**
     * Convierte PlayerDto a Player (modelo de dominio)
     * Obtiene la imagen del jugador desde la web oficial o genera un placeholder
     */
    suspend fun fromDto(dto: PlayerDto, teamCode: String): Player {
        val playerCode = dto.person.code ?: generatePlayerCode(
            dto.person.name, 
            dto.person.passportSurname, 
            dto.dorsal
        )
        
        // Usar las imágenes que vienen directamente en el PlayerDto desde feeds API
        val profileImage = dto.images?.profile
        val headshotImage = dto.images?.headshot
        
        // Si las imágenes de la API están vacías, intentar obtener desde el web oficial
        val imageUrl = profileImage 
            ?: headshotImage 
            ?: getPlayerImageFromWeb(dto.person.code, dto.person.name)
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
     * Convierte lista de PlayerDto a lista de PlayerEntity
     * Filtra solo los jugadores (type="J"), excluyendo entrenadores
     * Nota: Esta función ya no se usa con las nuevas imágenes, se mantiene para compatibilidad
     */
    suspend fun fromDtoListToEntityList(dtoList: List<PlayerDto>, teamCode: String): List<PlayerEntity> {
        return dtoList
            .filter { it.type == "J" } // Solo jugadores, no entrenadores
            .map { dto ->
                val player = fromDto(dto, teamCode)
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
