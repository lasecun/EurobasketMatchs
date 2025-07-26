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
                
                if (teamsResult.isSuccess) {
                    val realTeamDtos = teamsResult.getOrNull() ?: emptyList()
                    
                    if (realTeamDtos.isNotEmpty()) {
                        Log.d("DatabaseSeeder", "✅ Usando ${realTeamDtos.size} equipos REALES de EuroLeague")
                        
                        // Los DTOs del remote datasource ya están listos para Room
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
}
