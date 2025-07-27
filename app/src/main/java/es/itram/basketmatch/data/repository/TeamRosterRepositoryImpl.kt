package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.repository.TeamRosterRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio para roster de equipos
 */
@Singleton
class TeamRosterRepositoryImpl @Inject constructor(
    private val apiScraper: EuroLeagueJsonApiScraper
) : TeamRosterRepository {
    
    companion object {
        private const val TAG = "TeamRosterRepository"
    }
    
    // Caché simple en memoria para rosters
    private val rosterCache = mutableMapOf<String, TeamRoster>()
    
    override suspend fun getTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🔍 Obteniendo roster para equipo $teamTla, temporada $season")
            
            // Intentar obtener desde caché primero
            val cachedRoster = getCachedTeamRoster(teamTla)
            if (cachedRoster != null) {
                Log.d(TAG, "📋 Usando roster desde caché para $teamTla")
                return Result.success(cachedRoster)
            }
            
            // Obtener desde API
            val playersDto = apiScraper.getTeamRoster(teamTla, "E2025")
            val roster = convertToTeamRoster(teamTla, playersDto, season)
            
            // Guardar en caché
            rosterCache[teamTla] = roster
            
            Log.d(TAG, "✅ Roster obtenido exitosamente para $teamTla: ${roster.players.size} jugadores")
            Result.success(roster)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo roster para $teamTla", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCachedTeamRoster(teamTla: String): TeamRoster? {
        return rosterCache[teamTla]
    }
    
    override suspend fun refreshTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        // Limpiar caché para este equipo
        rosterCache.remove(teamTla)
        
        // Obtener datos frescos
        return getTeamRoster(teamTla, season)
    }
    
    /**
     * Convierte la lista de PlayerDto a TeamRoster
     */
    private fun convertToTeamRoster(
        teamTla: String,
        playersDto: List<PlayerDto>,
        season: String
    ): TeamRoster {
        val players = playersDto.map { playerDto ->
            Player(
                code = playerDto.code,
                name = playerDto.name,
                surname = playerDto.surname,
                fullName = playerDto.fullName ?: "${playerDto.name} ${playerDto.surname}",
                jersey = playerDto.jersey,
                position = PlayerPosition.fromString(playerDto.position),
                height = playerDto.height,
                dateOfBirth = playerDto.dateOfBirth,
                placeOfBirth = playerDto.placeOfBirth,
                nationality = playerDto.nationality,
                experience = playerDto.experience,
                profileImageUrl = playerDto.imageUrls?.profile,
                isActive = playerDto.isActive,
                isStarter = playerDto.isStarter,
                isCaptain = playerDto.isCaptain
            )
        }
        
        return TeamRoster(
            teamCode = teamTla,
            teamName = getTeamNameFromTla(teamTla),
            season = season,
            players = players.sortedBy { it.jersey ?: 999 }, // Ordenar por número de camiseta
            coaches = emptyList() // Por ahora no tenemos información de coaches
        )
    }
    
    /**
     * Mapea códigos TLA a nombres de equipos (básico)
     * En el futuro podríamos obtener esto desde la API de equipos
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
