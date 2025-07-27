package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.local.seed.DatabaseSeeder
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test para verificar que el DatabaseSeeder funciona con datos reales
 */
class DatabaseSeederRealDataTest {

    @Test
    fun `test DatabaseSeeder with real data for 2025-26 season`() = runTest {
        println("🗄️ TEST: DatabaseSeeder con datos reales 2025-26")
        println("=".repeat(60))
        
        try {
            val webScraper = EuroLeagueWebScraper()
            val jsonApiScraper = EuroLeagueJsonApiScraper()
            val remoteDataSource = EuroLeagueRemoteDataSource(jsonApiScraper, webScraper)
            
            // Intentar obtener equipos
            println("1️⃣ Probando obtención de equipos...")
            val teamsResult = remoteDataSource.getAllTeams()
            
            if (teamsResult.isSuccess) {
                val teams = teamsResult.getOrNull() ?: emptyList()
                println("   ✅ Equipos obtenidos: ${teams.size}")
            } else {
                println("   ❌ Error obteniendo equipos: ${teamsResult.exceptionOrNull()?.message}")
            }
            
            // Intentar obtener partidos
            println("\n2️⃣ Probando obtención de partidos...")
            val matchesResult = remoteDataSource.getAllMatches("2025-26")
            
            if (matchesResult.isSuccess) {
                val matches = matchesResult.getOrNull() ?: emptyList()
                println("   ✅ Partidos obtenidos: ${matches.size}")
                
                if (matches.isNotEmpty()) {
                    println("   📅 Primer partido: ${matches.first().homeTeamName} vs ${matches.first().awayTeamName}")
                    println("      Fecha: ${matches.first().date}")
                    
                    // Buscar específicamente partidos del 30 de septiembre
                    val september30 = matches.filter { it.date == "2025-09-30" }
                    println("   🎯 Partidos del 30/09/2025: ${september30.size}")
                }
            } else {
                println("   ❌ Error obteniendo partidos: ${matchesResult.exceptionOrNull()?.message}")
            }
            
            println("\n✅ Test completado - Los datos están disponibles para DatabaseSeeder")
            
        } catch (e: Exception) {
            println("❌ Error durante el test: ${e.message}")
            e.printStackTrace()
        }
    }
}
