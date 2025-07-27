package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.PlayerDao
import es.itram.basketmatch.data.datasource.local.dao.TeamRosterDao
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.data.datasource.remote.dto.PersonDto
import es.itram.basketmatch.data.datasource.remote.dto.PlayerImageUrls
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.data.mapper.PlayerMapper
import es.itram.basketmatch.data.mapper.TeamRosterMapper
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.repository.TeamRosterRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio para roster de equipos
 * Con cache inteligente y logs mejorados para distinguir entre datos de red y cache
 */
@Singleton
class TeamRosterRepositoryImpl @Inject constructor(
    private val apiScraper: EuroLeagueJsonApiScraper,
    private val teamRosterDao: TeamRosterDao,
    private val playerDao: PlayerDao
) : TeamRosterRepository {
    
    companion object {
        private const val TAG = "TeamRosterRepository"
    }
    
    override suspend fun getTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🔍 Iniciando obtención de roster para equipo $teamTla, temporada $season")
            
            // Primero intentar obtener desde cache local
            val cachedRoster = getCachedTeamRoster(teamTla)
            if (cachedRoster != null) {
                Log.d(TAG, "📱 [LOCAL] ✅ Roster encontrado en cache para $teamTla (${cachedRoster.players.size} jugadores)")
                return Result.success(cachedRoster)
            }
            
            // Si no hay cache, obtener desde API y guardar
            Log.d(TAG, "📱 [LOCAL] ⚠️ Cache vacío para $teamTla, descargando desde API...")
            Log.d(TAG, "🌐 [NETWORK] Obteniendo roster desde API para $teamTla")
            
            val playersDto = apiScraper.getTeamRoster(teamTla, "E2025")
            val roster = convertToTeamRoster(teamTla, playersDto, season)
            
            // Guardar en cache local
            saveRosterToCache(roster)
            
            Log.d(TAG, "🌐 [NETWORK] ✅ Roster obtenido y guardado en cache para $teamTla (${roster.players.size} jugadores)")
            Result.success(roster)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error obteniendo roster para $teamTla", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCachedTeamRoster(teamTla: String): TeamRoster? {
        return try {
            Log.d(TAG, "📱 [LOCAL] Verificando cache para roster de $teamTla")
            
            val rosterEntity = teamRosterDao.getTeamRoster(teamTla)
            if (rosterEntity == null) {
                Log.d(TAG, "📱 [LOCAL] ⚠️ No hay roster en cache para $teamTla")
                return null
            }
            
            val playerEntities = playerDao.getPlayersByTeamSync(teamTla)
            if (playerEntities.isEmpty()) {
                Log.d(TAG, "📱 [LOCAL] ⚠️ No hay jugadores en cache para $teamTla")
                return null
            }
            
            val players = PlayerMapper.fromEntityListToDomainList(playerEntities)
            val roster = TeamRosterMapper.fromEntity(rosterEntity, players)
            
            Log.d(TAG, "📱 [LOCAL] ✅ Roster encontrado en cache para $teamTla (${players.size} jugadores)")
            roster
            
        } catch (e: Exception) {
            Log.w(TAG, "📱 [LOCAL] Error accediendo al cache para $teamTla: ${e.message}")
            null
        }
    }
    
    override suspend fun refreshTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🌐 [NETWORK] 🔄 Refrescando roster forzadamente desde API para equipo $teamTla")
            
            val playersDto = apiScraper.getTeamRoster(teamTla, "E2025")
            val roster = convertToTeamRoster(teamTla, playersDto, season)
            
            // Guardar en cache local (reemplaza datos existentes)
            saveRosterToCache(roster)
            
            Log.d(TAG, "🌐 [NETWORK] ✅ Roster refrescado y guardado en cache para $teamTla (${roster.players.size} jugadores)")
            Result.success(roster)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error refrescando roster desde API para $teamTla", e)
            Result.failure(e)
        }
    }
    
    /**
     * Guarda un roster en el cache local
     */
    private suspend fun saveRosterToCache(roster: TeamRoster) {
        try {
            Log.d(TAG, "💾 [SAVE] Guardando roster de ${roster.teamCode} en cache local...")
            
            // Guardar información del roster
            val rosterEntity = TeamRosterMapper.toEntity(roster)
            teamRosterDao.insertTeamRoster(rosterEntity)
            
            // Guardar jugadores
            val playerEntities = PlayerMapper.fromDtoListToEntityList(
                roster.players.map { player ->
                    // Convertir Player de vuelta a PlayerDto para usar el mapper existente
                    // Esto es un workaround temporal hasta refactorizar los mappers
                    PlayerDto(
                        person = PersonDto(
                            code = player.code,
                            name = player.name,
                            surname = player.surname.takeIf { it.isNotBlank() },
                            height = player.height?.replace("cm", "")?.toIntOrNull(),
                            weight = player.weight?.replace("kg", "")?.toIntOrNull(),
                            dateOfBirth = player.dateOfBirth,
                            placeOfBirth = player.placeOfBirth,
                            nationality = player.nationality,
                            imageUrls = player.profileImageUrl?.let { PlayerImageUrls(profile = it) }
                        ),
                        jersey = player.jersey,
                        position = player.position?.ordinal,
                        positionName = player.position?.name,
                        isActive = player.isActive,
                        isStarter = player.isStarter,
                        isCaptain = player.isCaptain
                    )
                },
                roster.teamCode
            )
            
            playerDao.insertPlayers(playerEntities)
            
            Log.d(TAG, "💾 [SAVE] ✅ Roster guardado: ${roster.players.size} jugadores para ${roster.teamCode}")
            
        } catch (e: Exception) {
            Log.w(TAG, "💾 [SAVE] Error guardando roster en cache: ${e.message}")
        }
    }
    
    /**
     * Convierte los DTOs de jugadores de la API a un TeamRoster de dominio
     */
    private fun convertToTeamRoster(teamTla: String, playersDto: List<PlayerDto>, season: String): TeamRoster {
        Log.d(TAG, "🔄 Convirtiendo ${playersDto.size} jugadores de DTO a domain model para $teamTla")
        
        val players = playersDto.mapNotNull { playerDto ->
            try {
                // Generar un código único si no está disponible
                val playerCode = playerDto.person.code 
                    ?: generatePlayerCode(playerDto.person.name, playerDto.person.surname, playerDto.jersey)
                
                Player(
                    code = playerCode,
                    name = playerDto.person.name,
                    surname = playerDto.person.surname ?: "",
                    fullName = "${playerDto.person.name} ${playerDto.person.surname ?: ""}".trim(),
                    jersey = playerDto.jersey ?: playerDto.dorsalRaw?.toIntOrNull(),
                    position = convertPosition(playerDto.positionName),
                    height = playerDto.person.height?.toString(),
                    weight = playerDto.person.weight?.toString(),
                    dateOfBirth = playerDto.person.dateOfBirth ?: playerDto.person.birthDate,
                    placeOfBirth = playerDto.person.placeOfBirth,
                    nationality = playerDto.person.nationality,
                    experience = null,
                    profileImageUrl = playerDto.person.imageUrls?.profile ?: playerDto.person.imageUrls?.headshot,
                    isActive = playerDto.isActive,
                    isStarter = playerDto.isStarter,
                    isCaptain = playerDto.isCaptain
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error convirtiendo jugador ${playerDto.person.name}: ${e.message}")
                null
            }
        }
        
        return TeamRoster(
            teamCode = teamTla,
            teamName = getTeamNameFromTla(teamTla),
            season = season,
            players = players.sortedBy { it.jersey ?: 999 },
            coaches = emptyList()
        )
    }
    
    /**
     * Convierte la posición de string a enum
     */
    private fun convertPosition(positionName: String?): PlayerPosition {
        return when (positionName?.lowercase()) {
            "point guard", "pg" -> PlayerPosition.POINT_GUARD
            "shooting guard", "sg" -> PlayerPosition.SHOOTING_GUARD
            "small forward", "sf" -> PlayerPosition.SMALL_FORWARD
            "power forward", "pf" -> PlayerPosition.POWER_FORWARD
            "center", "c" -> PlayerPosition.CENTER
            "guard", "g" -> PlayerPosition.GUARD
            "forward", "f" -> PlayerPosition.FORWARD
            else -> PlayerPosition.UNKNOWN
        }
    }
    
    /**
     * Genera un código único para jugadores que no tienen código en la API
     */
    private fun generatePlayerCode(name: String, surname: String?, jersey: Int?): String {
        val cleanName = name.take(3).uppercase().replace(" ", "")
        val cleanSurname = surname?.take(3)?.uppercase()?.replace(" ", "") ?: ""
        val jerseyPart = jersey?.toString()?.padStart(2, '0') ?: "00"
        
        return "${cleanName}${cleanSurname}_$jerseyPart"
    }
    
    /**
     * Mapea códigos TLA a nombres de equipos
     */
    private fun getTeamNameFromTla(tla: String): String {
        return when (tla.lowercase()) {
            "ber" -> "ALBA Berlin"
            "asm" -> "AS Monaco"
            "bas" -> "Baskonia Vitoria-Gasteiz"
            "csk" -> "CSKA Moscow"
            "efs" -> "Anadolu Efes Istanbul"
            "fcb" -> "FC Barcelona"
            "bay" -> "FC Bayern Munich"
            "mta" -> "Maccabi Playtika Tel Aviv"
            "oly" -> "Olympiacos Piraeus"
            "pan" -> "Panathinaikos AKTOR Athens"
            "par" -> "Paris Basketball"
            "rea" -> "Real Madrid"
            "red" -> "EA7 Emporio Armani Milan"
            "ulk" -> "Fenerbahce Beko Istanbul"
            "vir" -> "Virtus Segafredo Bologna"
            "vil" -> "LDLC ASVEL Villeurbanne"
            "zal" -> "Zalgiris Kaunas"
            "val" -> "Valencia Basket"
            else -> tla.uppercase()
        }
    }
}
