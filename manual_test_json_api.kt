package es.itram.basketmatch.test

import kotlinx.coroutines.runBlocking

/**
 * Test simple para verificar manualmente el funcionamiento del JSON API
 */
fun main() = runBlocking {
    println("🧪 === MANUAL TEST JSON API SCRAPER ===")
    
    val jsonApiScraper = es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper()
    
    try {
        println("🏀 Probando getTeams()...")
        val teams = jsonApiScraper.getTeams()
        println("✅ Equipos obtenidos: ${teams.size}")
        
        teams.take(3).forEach { team ->
            println("   📋 ${team.name} (${team.shortCode}) - ${team.logoUrl}")
        }
        
        println("\n⚽ Probando getMatches()...")
        val matches = jsonApiScraper.getMatches("2025-26")
        println("✅ Partidos obtenidos: ${matches.size}")
        
        matches.take(3).forEach { match ->
            println("   ⚽ ${match.homeTeamName} vs ${match.awayTeamName} - ${match.date}")
        }
        
        val september30Matches = matches.filter { it.date == "2025-09-30" }
        println("🎯 Partidos del 30/09/2025: ${september30Matches.size}")
        
        println("\n✅ === TEST COMPLETADO EXITOSAMENTE ===")
        
    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
    }
}
