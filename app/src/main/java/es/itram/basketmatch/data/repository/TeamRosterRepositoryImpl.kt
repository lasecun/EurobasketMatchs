package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.PlayerDao
import es.itram.basketmatch.data.datasource.local.dao.TeamRosterDao
import es.itram.basketmatch.data.datasource.remote.dto.CountryDto
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
import es.itram.basketmatch.utils.PlayerImageUtil
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

/**
 * Implementación del repositorio para roster de equipos
 * Con cache inteligente y logs mejorados para distinguir entre datos de red y cache
 */
@Singleton
class TeamRosterRepositoryImpl @Inject constructor(
    private val apiScraper: EuroLeagueJsonApiScraper,
    private val teamRosterDao: TeamRosterDao,
    private val playerDao: PlayerDao,
    private val playerImageUtil: PlayerImageUtil
) : TeamRosterRepository {
    
    companion object {
        private const val TAG = "TeamRosterRepository"
        private const val CACHE_VALIDITY_HOURS = 2 // Cache válido por 2 horas
    }
    
    override suspend fun getTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🔍 Iniciando obtención de roster para equipo $teamTla, temporada $season")
            
            // Verificar si el cache es válido (no muy antiguo)
            val isCacheValid = isRosterCacheValid(teamTla)
            
            // Primero intentar obtener desde cache local si es válido
            val cachedRoster = if (isCacheValid) getCachedTeamRoster(teamTla) else null
            if (cachedRoster != null) {
                Log.d(TAG, "📱 [LOCAL] ✅ Cache válido - Roster encontrado para $teamTla (${cachedRoster.players.size} jugadores)")
                return Result.success(cachedRoster)
            }
            
            // Si cache no válido o vacío, obtener desde API y guardar
            if (!isCacheValid) {
                Log.d(TAG, "📱 [LOCAL] ⏰ Cache expirado para $teamTla, actualizando desde API...")
            } else {
                Log.d(TAG, "📱 [LOCAL] ⚠️ Cache vacío para $teamTla, descargando desde API...")
            }
            
            Log.d(TAG, "🌐 [NETWORK] Obteniendo roster desde API para $teamTla")
            
            // Ejecutar roster y logo en paralelo para optimizar velocidad
            coroutineScope {
                val playersDeferred = async { apiScraper.getTeamRoster(teamTla, "E2025") }
                val logoDeferred = async { getTeamLogoFromFeeds(teamTla) }
                
                val playersDto = playersDeferred.await()
                val teamLogoUrl = logoDeferred.await()
                
                val roster = convertToTeamRoster(teamTla, playersDto, season, teamLogoUrl)
                
                // Guardar en cache local
                saveRosterToCache(roster)
                
                Log.d(TAG, "🌐 [NETWORK] ✅ Roster obtenido y guardado en cache para $teamTla (${roster.players.size} jugadores)")
                Result.success(roster)
            }
            
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
            Log.d(TAG, "📱 [LOCAL] 🔗 Logo URL desde cache: ${roster.logoUrl}")
            roster
            
        } catch (e: Exception) {
            Log.w(TAG, "📱 [LOCAL] Error accediendo al cache para $teamTla: ${e.message}")
            null
        }
    }
    
    private suspend fun isRosterCacheValid(teamTla: String): Boolean {
        val latestPlayer = playerDao.getLatestPlayerByTeam(teamTla)
        return latestPlayer?.let { 
            val cacheTime = it.lastUpdated
            val hoursAgo = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - cacheTime)
            hoursAgo < CACHE_VALIDITY_HOURS
        } ?: false
    }

    override suspend fun refreshTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🌐 [NETWORK] 🔄 Refrescando roster forzadamente desde API para equipo $teamTla")
            
            // Ejecutar roster y logo en paralelo para optimizar velocidad
            coroutineScope {
                val playersDeferred = async { apiScraper.getTeamRoster(teamTla, "E2025") }
                val logoDeferred = async { getTeamLogoFromFeeds(teamTla) }
                
                val playersDto = playersDeferred.await()
                val teamLogoUrl = logoDeferred.await()
                
                val roster = convertToTeamRoster(teamTla, playersDto, season, teamLogoUrl)
                
                // Guardar en cache local (reemplaza datos existentes)
                saveRosterToCache(roster)
                
                Log.d(TAG, "🌐 [NETWORK] ✅ Roster refrescado y guardado en cache para $teamTla (${roster.players.size} jugadores)")
                Result.success(roster)
            }
            
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
            
            // Guardar jugadores - usar directamente PlayerMapper.toEntity en lugar del workaround
            val playerEntities = roster.players.map { player ->
                PlayerMapper.toEntity(player, roster.teamCode)
            }
            
            playerDao.insertPlayers(playerEntities)
            
            Log.d(TAG, "💾 [SAVE] ✅ Roster guardado: ${roster.players.size} jugadores para ${roster.teamCode}")
            
        } catch (e: Exception) {
            Log.w(TAG, "💾 [SAVE] Error guardando roster en cache: ${e.message}")
        }
    }
    
    /**
     * Convierte los DTOs de jugadores de la API a un TeamRoster de dominio
     */
    private suspend fun convertToTeamRoster(
        teamTla: String, 
        playersDto: List<PlayerDto>, 
        season: String,
        logoUrl: String? = null
    ): TeamRoster {
        Log.d(TAG, "🔄 Convirtiendo ${playersDto.size} jugadores de DTO a domain model para $teamTla")
        
        // Filtrar primero y luego mapear para reducir trabajo
        val validPlayerDtos = playersDto.filter { it.type == "J" }
        Log.d(TAG, "🏀 ${validPlayerDtos.size} jugadores válidos después del filtro")
        
        val players = validPlayerDtos.mapNotNull { playerDto ->
            try {
                PlayerMapper.fromDto(playerDto, teamTla, playerImageUtil)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error convirtiendo jugador ${playerDto.person.name}: ${e.message}")
                null
            }
        }
        
        Log.d(TAG, "✅ ${players.size} jugadores convertidos exitosamente")
        
        return TeamRoster(
            teamCode = teamTla,
            teamName = getTeamNameFromTla(teamTla),
            season = season,
            players = players.sortedBy { it.jersey ?: 999 },
            coaches = emptyList(),
            logoUrl = logoUrl ?: getTeamLogoUrl(teamTla) // Usar logoUrl del parámetro o fallback
        ).also { roster ->
            Log.d(TAG, "🔗 TeamRoster creado con logoUrl: ${roster.logoUrl} para equipo: ${roster.teamName}")
        }
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
    private fun generatePlayerCode(name: String, surname: String?, jersey: String?): String {
        val cleanName = name.take(3).uppercase().replace(" ", "")
        val cleanSurname = surname?.take(3)?.uppercase()?.replace(" ", "") ?: ""
        val jerseyPart = jersey?.take(2)?.padStart(2, '0') ?: "00"
        
        return "${cleanName}${cleanSurname}_$jerseyPart"
    }
    
    /**
     * Obtiene el logo del equipo desde la API de feeds buscando en partidos recientes
     */
    private suspend fun getTeamLogoFromFeeds(teamTla: String): String? {
        return try {
            Log.d(TAG, "🔍 Buscando logo para $teamTla desde API de feeds...")
            
            // Obtener todos los partidos una sola vez en lugar de en cada iteración
            val matches = apiScraper.getMatches()
            val matchWithTeam = matches.find { match ->
                match.homeTeamId.equals(teamTla, ignoreCase = true) || 
                match.awayTeamId.equals(teamTla, ignoreCase = true)
            }
            
            if (matchWithTeam != null) {
                val logoUrl = if (matchWithTeam.homeTeamId.equals(teamTla, ignoreCase = true)) {
                    matchWithTeam.homeTeamLogo
                } else {
                    matchWithTeam.awayTeamLogo
                }
                
                if (!logoUrl.isNullOrEmpty()) {
                    Log.d(TAG, "🖼️ Logo encontrado para $teamTla: $logoUrl")
                    return logoUrl
                }
            }
            
            Log.w(TAG, "⚠️ No se encontró logo para $teamTla en la API de feeds, usando fallback")
            null
            
        } catch (e: Exception) {
            Log.w(TAG, "Error obteniendo logo desde feeds para $teamTla: ${e.message}")
            null
        }
    }

    /**
     * Mapea códigos TLA a nombres de equipos
     */
    private fun getTeamNameFromTla(tla: String): String {
        return when (tla.lowercase()) {
            "ber", "alb" -> "ALBA Berlin"
            "asm", "mon" -> "AS Monaco"
            "bas", "bkn" -> "Baskonia Vitoria-Gasteiz"
            "csk", "csk" -> "CSKA Moscow"
            "efs", "ist" -> "Anadolu Efes Istanbul"
            "fcb", "bar" -> "FC Barcelona"
            "bay", "mun" -> "FC Bayern Munich"
            "mta", "tel" -> "Maccabi Playtika Tel Aviv"
            "oly", "oly" -> "Olympiacos Piraeus"
            "pan", "pan" -> "Panathinaikos AKTOR Athens"
            "par", "prs" -> "Paris Basketball"
            "rea", "mad" -> "Real Madrid"
            "red", "mil" -> "EA7 Emporio Armani Milan"
            "ulk", "fen" -> "Fenerbahce Beko Istanbul"
            "vir", "vir" -> "Virtus Segafredo Bologna"
            "vil", "asv" -> "LDLC ASVEL Villeurbanne"
            "zal", "zal" -> "Zalgiris Kaunas"
            "val", "pam" -> "Valencia Basket"
            else -> tla.uppercase()
        }
    }
    
    /**
     * Mapea códigos TLA a URLs de logos de equipos
     */
    private fun getTeamLogoUrl(tla: String): String? {
        return when (tla.lowercase()) {
            "ber", "alb" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/alba-berlin.png"
            "asm", "mon" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/as-monaco.png"
            "bas", "bkn" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/baskonia-vitoria-gasteiz.png"
            "csk", "csk" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/cska-moscow.png"
            "efs", "ist" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/anadolu-efes-istanbul.png"
            "fcb", "bar" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fc-barcelona.png"
            "bay", "mun" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fc-bayern-munich.png"
            "mta", "tel" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/maccabi-playtika-tel-aviv.png"
            "oly", "oly" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/olympiacos-piraeus.png"
            "pan", "pan" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/panathinaikos-aktor-athens.png"
            "par", "prs" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/paris-basketball.png"
            "rea", "mad" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/real-madrid.png"
            "red", "mil" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/ea7-emporio-armani-milan.png"
            "ulk", "fen" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fenerbahce-beko-istanbul.png"
            "vir", "vir" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/virtus-segafredo-bologna.png"
            "vil", "asv" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/ldlc-asvel-villeurbanne.png"
            "zal", "zal" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/zalgiris-kaunas.png"
            "val", "pam" -> "https://img.euroleaguebasketball.net/design/ec/logos/clubs/valencia-basket.png"
            else -> null
        }
    }
}
