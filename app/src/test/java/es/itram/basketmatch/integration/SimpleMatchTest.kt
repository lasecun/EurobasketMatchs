package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test simple para verificar qué partidos se obtienen realmente
 */
class SimpleMatchTest {

    @Test
    fun `simple match scraping test`() = runTest {
        println("🔍 TEST SIMPLE: Verificando partidos obtenidos")
        println("=".repeat(60))
        
        try {
            val webScraper = EuroLeagueWebScraper()
            val matches = webScraper.getMatches("2025-26")
            
            println("📊 Total partidos obtenidos: ${matches.size}")
            
            if (matches.isEmpty()) {
                println("❌ NO SE ENCONTRARON PARTIDOS")
                return@runTest
            }
            
            // Mostrar primeros 5 partidos
            println("\n🏀 Primeros 5 partidos:")
            matches.take(5).forEachIndexed { index, match ->
                println("${index + 1}. ${match.homeTeamName} vs ${match.awayTeamName}")
                println("   📅 ${match.date} ⏰ ${match.time}")
            }
            
            // Buscar partidos de septiembre 2025
            val septemberMatches = matches.filter { it.date.startsWith("2025-09") }
            println("\n📅 Partidos de septiembre 2025: ${septemberMatches.size}")
            
            // Buscar específicamente el 30 de septiembre
            val september30 = matches.filter { it.date == "2025-09-30" }
            println("🎯 Partidos del 30/09/2025: ${september30.size}")
            
            if (september30.isNotEmpty()) {
                println("✅ ENCONTRADOS partidos del 30/09:")
                september30.forEach { match ->
                    println("   🏀 ${match.homeTeamName} vs ${match.awayTeamName}")
                    println("      ⏰ ${match.time}")
                }
            }
            
            // Mostrar todas las fechas únicas
            val uniqueDates = matches.map { it.date }.distinct().sorted()
            println("\n📊 Total fechas únicas: ${uniqueDates.size}")
            if (uniqueDates.isNotEmpty()) {
                println("Primera: ${uniqueDates.first()}")
                println("Última: ${uniqueDates.last()}")
            }
            
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
        }
    }
}
