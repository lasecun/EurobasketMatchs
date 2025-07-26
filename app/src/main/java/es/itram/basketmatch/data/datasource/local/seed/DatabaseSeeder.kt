package es.itram.basketmatch.data.datasource.local.seed

import android.util.Log
import es.itram.basketmatch.data.datasource.local.EuroLeagueDatabase
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.domain.entity.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase para poblar la base de datos con datos reales de EuroLeague
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: EuroLeagueDatabase,
    private val remoteDataSource: EuroLeagueRemoteDataSource
) {

    suspend fun seedDatabase() = withContext(Dispatchers.IO) {
        try {
            Log.d("DatabaseSeeder", "🏀 Iniciando seed con datos REALES de EuroLeague...")
            
            val teamDao = database.teamDao()

            // Verificar si ya hay datos
            val existingTeamsCount = teamDao.getTeamCount()
            Log.d("DatabaseSeeder", "Equipos existentes: $existingTeamsCount")

            if (existingTeamsCount > 0) {
                Log.d("DatabaseSeeder", "Ya hay datos en la base de datos, saltando seed")
                return@withContext
            }

            // Intentar obtener datos reales primero
            Log.d("DatabaseSeeder", "🌐 Obteniendo datos reales de EuroLeague...")
            
            try {
                val teamsResult = remoteDataSource.getAllTeams()
                val matchesResult = remoteDataSource.getAllMatches()
                
                if (teamsResult.isSuccess) {
                    val realTeamDtos = teamsResult.getOrNull() ?: emptyList()
                    
                    if (realTeamDtos.isNotEmpty()) {
                        Log.d("DatabaseSeeder", "✅ Usando ${realTeamDtos.size} equipos REALES de EuroLeague")
                        
                        // Guardar equipos reales
                        val teamEntities = realTeamDtos.map { dto ->
                            es.itram.basketmatch.data.datasource.local.entity.TeamEntity(
                                id = dto.id,
                                name = dto.name,
                                shortName = dto.fullName,
                                code = dto.shortCode,
                                logoUrl = dto.logoUrl ?: "",
                                country = dto.country ?: "",
                                city = dto.venue ?: "",
                                founded = 1900,
                                coach = "",
                                website = dto.profileUrl
                            )
                        }
                        teamDao.insertTeams(teamEntities)
                        
                        // Intentar guardar partidos reales también
                        if (matchesResult.isSuccess) {
                            val realMatchDtos = matchesResult.getOrNull() ?: emptyList()
                            if (realMatchDtos.isNotEmpty()) {
                                Log.d("DatabaseSeeder", "✅ Usando ${realMatchDtos.size} partidos REALES de EuroLeague")
                                
                                val matchEntities = realMatchDtos.map { dto ->
                                    es.itram.basketmatch.data.datasource.local.entity.MatchEntity(
                                        id = dto.id,
                                        homeTeamId = dto.homeTeamId,
                                        awayTeamId = dto.awayTeamId,
                                        homeScore = dto.homeScore,
                                        awayScore = dto.awayScore,
                                        dateTime = parseMatchDateTime(dto.date, dto.time),
                                        status = convertMatchStatus(dto.status),
                                        venue = dto.venue ?: "",
                                        round = parseRoundNumber(dto.round),
                                        seasonType = es.itram.basketmatch.domain.entity.SeasonType.REGULAR
                                    )
                                }
                                val matchDao = database.matchDao()
                                matchDao.insertMatches(matchEntities)
                            } else {
                                Log.d("DatabaseSeeder", "⚠️ No hay partidos reales disponibles (temporada no iniciada)")
                                Log.d("DatabaseSeeder", "🎯 Generando partidos simulados basados en equipos reales...")
                                
                                // Generar partidos simulados para la temporada 2025-26
                                generateSimulatedMatches(teamEntities)
                            }
                        } else {
                            Log.d("DatabaseSeeder", "⚠️ Generando partidos simulados basados en equipos reales...")
                            generateSimulatedMatches(teamEntities)
                        }
                        
                        Log.d("DatabaseSeeder", "🎯 Base de datos poblada con datos REALES de EuroLeague")
                        return@withContext
                    }
                } else {
                    Log.w("DatabaseSeeder", "⚠️ Error obteniendo datos reales: ${teamsResult.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.w("DatabaseSeeder", "⚠️ Error en web scraping: ${e.message}")
            }
            
            // Solo como último recurso, usar datos de fallback
            Log.d("DatabaseSeeder", "🔄 Usando datos de fallback de EuroLeague...")
            seedWithFallbackData(teamDao)
            
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "❌ Error en seed de base de datos: ${e.message}", e)
        }
    }

    private suspend fun seedWithFallbackData(teamDao: es.itram.basketmatch.data.datasource.local.dao.TeamDao) {
        Log.d("DatabaseSeeder", "📦 Poblando con equipos reales de EuroLeague (fallback)")
        
        val realTeams = listOf(
            Team(
                id = "real_madrid",
                name = "Real Madrid",
                shortName = "Real Madrid",
                code = "MAD",
                logoUrl = "",
                country = "Spain",
                city = "Madrid",
                founded = 1902,
                coach = "Chus Mateo",
                website = "https://www.realmadrid.com"
            ),
            Team(
                id = "fc_barcelona", 
                name = "FC Barcelona",
                shortName = "Barcelona",
                code = "BAR",
                logoUrl = "",
                country = "Spain", 
                city = "Barcelona",
                founded = 1899,
                coach = "Roger Grimau",
                website = "https://www.fcbarcelona.com"
            ),
            Team(
                id = "olympiacos",
                name = "Olympiacos Piraeus",
                shortName = "Olympiacos",
                code = "OLY", 
                logoUrl = "",
                country = "Greece",
                city = "Piraeus",
                founded = 1925,
                coach = "Georgios Bartzokas",
                website = "https://www.olympiacos.org"
            ),
            Team(
                id = "panathinaikos",
                name = "Panathinaikos Athens", 
                shortName = "Panathinaikos",
                code = "PAO",
                logoUrl = "",
                country = "Greece",
                city = "Athens", 
                founded = 1908,
                coach = "Ergin Ataman",
                website = "https://www.paobc.gr"
            ),
            Team(
                id = "fenerbahce",
                name = "Fenerbahce Istanbul",
                shortName = "Fenerbahce", 
                code = "FEN",
                logoUrl = "",
                country = "Turkey",
                city = "Istanbul",
                founded = 1907,
                coach = "Sarunas Jasikevicius", 
                website = "https://www.fenerbahce.org"
            )
        )

        val teamEntities = TeamMapper.fromDomainList(realTeams)
        teamDao.insertTeams(teamEntities)
        
        Log.d("DatabaseSeeder", "✅ ${realTeams.size} equipos reales de EuroLeague guardados (fallback)")
    }
    
    private fun parseMatchDateTime(date: String?, time: String?): java.time.LocalDateTime {
        return if (date != null && time != null) {
            try {
                // Ejemplo: "17/10/2024" y "20:30"
                val dateParts = date.split("/")
                if (dateParts.size == 3) {
                    val day = dateParts[0].toInt()
                    val month = dateParts[1].toInt()
                    val year = dateParts[2].toInt()
                    
                    val timeParts = time.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toInt()
                        val minute = timeParts[1].toInt()
                        
                        java.time.LocalDateTime.of(year, month, day, hour, minute)
                    } else {
                        java.time.LocalDateTime.of(year, month, day, 20, 0)
                    }
                } else {
                    java.time.LocalDateTime.now().plusDays(1)
                }
            } catch (e: Exception) {
                Log.w("DatabaseSeeder", "Error parseando fecha: $date $time", e)
                java.time.LocalDateTime.now().plusDays(1)
            }
        } else {
            java.time.LocalDateTime.now().plusDays(1)
        }
    }
    
    private fun parseRoundNumber(round: String?): Int {
        return try {
            round?.filter { it.isDigit() }?.toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }
    
    private fun convertMatchStatus(dtoStatus: es.itram.basketmatch.data.datasource.remote.dto.MatchStatus): es.itram.basketmatch.domain.entity.MatchStatus {
        return when (dtoStatus) {
            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.SCHEDULED -> es.itram.basketmatch.domain.entity.MatchStatus.SCHEDULED
            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.LIVE -> es.itram.basketmatch.domain.entity.MatchStatus.LIVE
            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.FINISHED -> es.itram.basketmatch.domain.entity.MatchStatus.FINISHED
            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.POSTPONED -> es.itram.basketmatch.domain.entity.MatchStatus.POSTPONED
            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.CANCELLED -> es.itram.basketmatch.domain.entity.MatchStatus.CANCELLED
        }
    }
    
    private suspend fun generateSimulatedMatches(teams: List<es.itram.basketmatch.data.datasource.local.entity.TeamEntity>) {
        if (teams.size < 2) return
        
        val matchDao = database.matchDao()
        val simulatedMatches = mutableListOf<es.itram.basketmatch.data.datasource.local.entity.MatchEntity>()
        
        // Fecha de inicio de la temporada 2025-26: 30 de septiembre de 2025
        val seasonStart = java.time.LocalDateTime.of(2025, 9, 30, 20, 0)
        
        // Generar enfrentamientos para los primeros 10 partidos de la temporada
        var matchCounter = 1
        var currentDate = seasonStart
        
        val teamList = teams.shuffled()
        
        // Crear enfrentamientos para la primera jornada
        for (i in 0 until minOf(10, teamList.size / 2 * 2)) {
            if (i + 1 < teamList.size) {
                val homeTeam = teamList[i]
                val awayTeam = teamList[i + 1]
                
                simulatedMatches.add(
                    es.itram.basketmatch.data.datasource.local.entity.MatchEntity(
                        id = "simulated_match_$matchCounter",
                        homeTeamId = homeTeam.id,
                        awayTeamId = awayTeam.id,
                        homeScore = null,
                        awayScore = null,
                        dateTime = currentDate,
                        status = es.itram.basketmatch.domain.entity.MatchStatus.SCHEDULED,
                        venue = "${homeTeam.city} Arena",
                        round = 1,
                        seasonType = es.itram.basketmatch.domain.entity.SeasonType.REGULAR
                    )
                )
                
                matchCounter++
                // Espaciar partidos: algunos el mismo día, otros días posteriores
                if (matchCounter % 3 == 0) {
                    currentDate = currentDate.plusDays(2).withHour(20).withMinute(0)
                } else {
                    currentDate = currentDate.plusHours(3)
                }
            }
        }
        
        matchDao.insertMatches(simulatedMatches)
        Log.d("DatabaseSeeder", "🎮 ${simulatedMatches.size} partidos simulados generados para la temporada 2025-26")
    }
}
