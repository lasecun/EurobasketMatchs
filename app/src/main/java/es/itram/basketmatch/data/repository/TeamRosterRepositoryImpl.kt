package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.PlayerDao
import es.itram.basketmatch.data.datasource.local.dao.TeamRosterDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.mapper.PlayerMapper
import es.itram.basketmatch.data.mapper.TeamRosterMapper
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.TeamRosterRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio para roster de equipos
 *
 * ACTUALIZADO: Usa la API oficial de EuroLeague
 * ✅ Datos reales de plantillas con imágenes de jugadores
 * ✅ Cache inteligente mejorado
 * ✅ Sin scraper - API estable y oficial
 */
@Singleton
class TeamRosterRepositoryImpl @Inject constructor(
    private val teamRosterDao: TeamRosterDao,
    private val playerDao: PlayerDao,
    private val matchRepository: MatchRepository,
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource
) : TeamRosterRepository {

    companion object {
        private const val TAG = "TeamRosterRepository"
        private const val CACHE_VALIDITY_HOURS = 24L // Cache válido por 24 horas
    }
    
    // Cache en memoria para logos de equipos para evitar búsquedas repetidas
    private val teamLogoCache = mutableMapOf<String, String?>()
    
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
            
            Log.d(TAG, "🌐 [NETWORK] Obteniendo roster desde API oficial para $teamTla")

            // Obtener roster real desde la API oficial
            val seasonCode = if (season.startsWith("E")) season else "E2025"
            val apiResult = officialApiDataSource.getTeamRoster(teamTla, seasonCode = seasonCode)

            if (apiResult.isFailure) {
                Log.e(TAG, "❌ Error obteniendo roster desde API: ${apiResult.exceptionOrNull()?.message}")
                return Result.failure(apiResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }

            val playerDtos = apiResult.getOrNull() ?: emptyList()

            if (playerDtos.isEmpty()) {
                Log.w(TAG, "⚠️ No se encontraron jugadores para $teamTla en la API")
                return Result.failure(Exception("No se encontraron jugadores para $teamTla"))
            }

            // Convertir DTOs de la API oficial a modelo de dominio
            val players = playerDtos.map { PlayerMapper.fromApiDto(it, teamTla) }

            // Log detallado de los jugadores convertidos
            Log.d(TAG, "📋 Jugadores convertidos: ${players.size}")
            if (players.isNotEmpty()) {
                val firstPlayer = players.first()
                Log.d(TAG, "📋 Primer jugador: code=${firstPlayer.code}, name=${firstPlayer.name}, fullName=${firstPlayer.fullName}")
                Log.d(TAG, "📋 Imagen: ${firstPlayer.profileImageUrl}")
                Log.d(TAG, "📋 Dorsal: ${firstPlayer.jersey}, Posición: ${firstPlayer.position}")
            }

            val teamLogoUrl = getTeamLogoFromFeeds(teamTla)
            val roster = TeamRoster(
                teamCode = teamTla,
                teamName = getTeamNameFromTla(teamTla),
                season = seasonCode,
                players = players.sortedBy { it.jersey ?: 999 },
                coaches = emptyList(),
                logoUrl = teamLogoUrl
            )

            // Guardar en cache local
            saveRosterToCache(roster)

            Log.d(TAG, "🌐 [NETWORK] ✅ Roster obtenido y guardado en cache para $teamTla (${roster.players.size} jugadores)")
            Result.success(roster)

        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error obteniendo roster para $teamTla", e)
            Result.failure(Exception("Error cargando roster de $teamTla: ${e.message}", e))
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

            // VALIDACIÓN MEJORADA: Detectar datos antiguos o corruptos
            val playersWithoutImages = players.count { it.profileImageUrl == null || it.profileImageUrl?.contains("ui-avatars.com") == true }
            val playersWithoutNames = players.count { it.name.isBlank() || it.fullName.isBlank() }
            val percentageWithoutImages = (playersWithoutImages.toFloat() / players.size) * 100
            val percentageWithoutNames = (playersWithoutNames.toFloat() / players.size) * 100

            // Si más del 50% no tiene imágenes reales O más del 30% no tiene nombres, invalidar caché
            if (percentageWithoutImages > 50 || percentageWithoutNames > 30) {
                Log.w(TAG, "📱 [LOCAL] ⚠️ Cache inválido detectado:")
                Log.w(TAG, "   - ${percentageWithoutImages.toInt()}% sin imágenes reales")
                Log.w(TAG, "   - ${percentageWithoutNames.toInt()}% sin nombres")
                Log.w(TAG, "📱 [LOCAL] 🔄 Forzando recarga desde API para obtener datos actualizados...")

                // Eliminar datos antiguos para forzar recarga
                playerDao.deletePlayersByTeam(teamTla)
                teamRosterDao.deleteTeamRoster(teamTla)

                return null
            }

            val roster = TeamRosterMapper.fromEntity(rosterEntity, players)
            
            Log.d(TAG, "📱 [LOCAL] ✅ Roster encontrado en cache para $teamTla (${players.size} jugadores)")
            Log.d(TAG, "📱 [LOCAL] 🔗 Logo URL desde cache: ${roster.logoUrl}")
            Log.d(TAG, "📱 [LOCAL] 📸 Jugadores con imagen real: ${players.size - playersWithoutImages}/${players.size}")
            Log.d(TAG, "📱 [LOCAL] 📝 Jugadores con nombre completo: ${players.size - playersWithoutNames}/${players.size}")
            roster
            
        } catch (e: Exception) {
            Log.e(TAG, "📱 [LOCAL] ❌ Error accediendo al cache para $teamTla", e)
            null
        }
    }
    
    private suspend fun isRosterCacheValid(teamTla: String): Boolean {
        return try {
            val latestPlayer = playerDao.getLatestPlayerByTeam(teamTla)
            if (latestPlayer == null) {
                Log.d(TAG, "📱 [CACHE] No hay cache para $teamTla")
                return false
            }
            
            val cacheTime = latestPlayer.lastUpdated
            val hoursAgo = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - cacheTime)
            val isValid = hoursAgo < CACHE_VALIDITY_HOURS
            
            Log.d(TAG, "📱 [CACHE] Cache para $teamTla: ${if (isValid) "VÁLIDO" else "EXPIRADO"} (${hoursAgo}h)")
            isValid
        } catch (e: Exception) {
            Log.w(TAG, "📱 [CACHE] Error verificando cache para $teamTla: ${e.message}")
            false
        }
    }

    override suspend fun refreshTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🌐 [NETWORK] 🔄 Refrescando roster forzadamente para equipo $teamTla")

            // Obtener roster real desde la API oficial
            val seasonCode = if (season.startsWith("E")) season else "E2025"
            val apiResult = officialApiDataSource.getTeamRoster(teamTla, seasonCode = seasonCode)

            if (apiResult.isFailure) {
                Log.e(TAG, "❌ Error obteniendo roster desde API: ${apiResult.exceptionOrNull()?.message}")
                return Result.failure(apiResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }

            val playerDtos = apiResult.getOrNull() ?: emptyList()

            if (playerDtos.isEmpty()) {
                Log.w(TAG, "⚠️ No se encontraron jugadores para $teamTla en la API")
                return Result.failure(Exception("No se encontraron jugadores para $teamTla"))
            }

            // Convertir DTOs de la API oficial a modelo de dominio
            val players = playerDtos.map { PlayerMapper.fromApiDto(it, teamTla) }

            val teamLogoUrl = getTeamLogoFromFeeds(teamTla)
            val roster = TeamRoster(
                teamCode = teamTla,
                teamName = getTeamNameFromTla(teamTla),
                season = seasonCode,
                players = players.sortedBy { it.jersey ?: 999 },
                coaches = emptyList(),
                logoUrl = teamLogoUrl
            )

            // Guardar en cache local (reemplaza datos existentes)
            saveRosterToCache(roster)

            Log.d(TAG, "🌐 [NETWORK] ✅ Roster refrescado y guardado en cache para $teamTla (${roster.players.size} jugadores)")
            Result.success(roster)

        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error refrescando roster para $teamTla", e)
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
     * Obtiene el logo del equipo desde el cache de partidos para evitar llamadas duplicadas a la API
     */
    private suspend fun getTeamLogoFromFeeds(teamTla: String): String? {
        return try {
            Log.d(TAG, "🔍 Buscando logo para $teamTla...")
            
            // Primero verificar si ya tenemos el logo en cache
            teamLogoCache[teamTla]?.let { cachedLogo ->
                Log.d(TAG, "🖼️ Logo encontrado en cache para $teamTla: $cachedLogo")
                return cachedLogo
            }
            
            Log.d(TAG, "💾 [CACHE] Buscando logo en partidos cacheados para $teamTla")
            
            // Usar el repositorio de partidos que ya tiene cache inteligente
            val matches = matchRepository.getAllMatches().first()
            Log.d(TAG, "💾 [CACHE] ✅ Usando ${matches.size} partidos desde cache local")
            
            val matchWithTeam = matches.find { match ->
                match.homeTeamId.equals(teamTla, ignoreCase = true) || 
                match.awayTeamId.equals(teamTla, ignoreCase = true)
            }
            
            val logoUrl = if (matchWithTeam != null) {
                if (matchWithTeam.homeTeamId.equals(teamTla, ignoreCase = true)) {
                    matchWithTeam.homeTeamLogo
                } else {
                    matchWithTeam.awayTeamLogo
                }
            } else {
                null
            }
            
            // Guardar en cache (incluso si es null para evitar búsquedas repetidas)
            teamLogoCache[teamTla] = logoUrl
            
            if (!logoUrl.isNullOrEmpty()) {
                Log.d(TAG, "🖼️ Logo encontrado para $teamTla: $logoUrl")
                logoUrl
            } else {
                Log.w(TAG, "⚠️ No se encontró logo para $teamTla en partidos cacheados")
                null
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "❌ Error obteniendo logo desde cache de partidos para $teamTla: ${e.message}")
            // Guardar null en cache para evitar reintentos
            teamLogoCache[teamTla] = null
            null
        }
    }

    /**
     * Mapea códigos TLA a nombres de equipos
     */
    private fun getTeamNameFromTla(teamTla: String): String {
        return when (teamTla.lowercase()) {
            "ber", "alb" -> "ALBA Berlin"
            "asm", "mon" -> "AS Monaco"
            "bas", "bkn" -> "Baskonia Vitoria-Gasteiz"
            "csk" -> "CSKA Moscow"
            "efs", "ist" -> "Anadolu Efes Istanbul"
            "fcb", "bar" -> "FC Barcelona"
            "bay", "mun" -> "FC Bayern Munich"
            "mta", "tel" -> "Maccabi Playtika Tel Aviv"
            "oly" -> "Olympiacos Piraeus"
            "pan" -> "Panathinaikos AKTOR Athens"
            "par", "prs" -> "Paris Basketball"
            "rea", "mad" -> "Real Madrid"
            "red" -> "Crvena Zvezda Meridianbet Belgrade"
            "mil" -> "EA7 Emporio Armani Milan"
            "ulk", "fen" -> "Fenerbahce Beko Istanbul"
            "vir" -> "Virtus Segafredo Bologna"
            "vil", "asv" -> "LDLC ASVEL Villeurbanne"
            "zal" -> "Zalgiris Kaunas"
            "val", "pam" -> "Valencia Basket"
            else -> teamTla.uppercase()
        }
    }
}
