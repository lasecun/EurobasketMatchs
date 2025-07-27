package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.repository.TeamRosterRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio para roster de equipos
 * Con logs mejorados para distinguir entre datos de red y cache
 */
@Singleton
class TeamRosterRepositoryImpl @Inject constructor(
    private val apiScraper: EuroLeagueJsonApiScraper
) : TeamRosterRepository {
    
    companion object {
        private const val TAG = "TeamRosterRepository"
    }
    
    override suspend fun getTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🔍 Iniciando obtención de roster para equipo $teamTla, temporada $season")
            Log.d(TAG, "🌐 [NETWORK] Obteniendo roster desde API para $teamTla")
            
            val playersDto = apiScraper.getTeamRoster(teamTla, "E2025")
            val roster = convertToTeamRoster(teamTla, playersDto, season)
            
            Log.d(TAG, "🌐 [NETWORK] ✅ Roster obtenido exitosamente desde API para $teamTla (${roster.players.size} jugadores)")
            Result.success(roster)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error obteniendo roster para $teamTla", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCachedTeamRoster(teamTla: String): TeamRoster? {
        Log.d(TAG, "📱 [LOCAL] ⚠️ Cache no implementado actualmente, devolviendo null para $teamTla")
        return null
    }
    
    override suspend fun refreshTeamRoster(teamTla: String, season: String): Result<TeamRoster> {
        return try {
            Log.d(TAG, "🌐 [NETWORK] 🔄 Refrescando roster forzadamente desde API para equipo $teamTla")
            
            val playersDto = apiScraper.getTeamRoster(teamTla, "E2025")
            val roster = convertToTeamRoster(teamTla, playersDto, season)
            
            Log.d(TAG, "🌐 [NETWORK] ✅ Roster refrescado exitosamente desde API para $teamTla")
            Result.success(roster)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error refrescando roster desde API para $teamTla", e)
            Result.failure(e)
        }
    }
    
    /**
     * Convierte los DTOs de jugadores de la API a un TeamRoster de dominio
     */
    private fun convertToTeamRoster(teamTla: String, playersDto: List<PlayerDto>, season: String): TeamRoster {
        Log.d(TAG, "🔄 Convirtiendo ${playersDto.size} jugadores de DTO a domain model para $teamTla")
        
        val players = playersDto.mapNotNull { playerDto ->
            try {
                Player(
                    code = playerDto.person.code,
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
