package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Test para debuggear el scraping de partidos específicamente para el 30 de septiembre de 2025
 */
class MatchScrapingDebugTest {

    @Test
    fun `debug match scraping for September 30 2025`() = runTest {
        println("🔍 DEBUG: Verificando scraping de partidos para el 30 de septiembre de 2025")
        println("=".repeat(70))
        
        val webScraper = EuroLeagueWebScraper()
        
        try {
            // 1. Probar scraping de partidos para temporada 2025-26
            println("1️⃣ Intentando obtener partidos de temporada 2025-26...")
            val matches = webScraper.getMatches("2025-26")
            
            println("   ✅ Scraping exitoso!")
            println("   📊 Total de partidos obtenidos: ${matches.size}")
            
            if (matches.isEmpty()) {
                println("   ⚠️ NO SE ENCONTRARON PARTIDOS!")
                return@runTest
            }
            
            // 2. Filtrar partidos del 30 de septiembre de 2025
            val targetDate = LocalDate.of(2025, 9, 30)
            val matchesOnDate = matches.filter { match ->
                try {
                    // Parsear la fecha del partido
                    val matchDate = LocalDate.parse(match.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    matchDate == targetDate
                } catch (e: Exception) {
                    println("   ⚠️ Error parseando fecha '${match.date}': ${e.message}")
                    false
                }
            }
            
            println("\n2️⃣ Partidos encontrados para el 30 de septiembre de 2025:")
            println("   📅 Partidos en esa fecha: ${matchesOnDate.size}")
            
            if (matchesOnDate.isNotEmpty()) {
                matchesOnDate.forEach { match ->
                    println("   🏀 ${match.homeTeamName} vs ${match.awayTeamName}")
                    println("      📅 Fecha: ${match.date}")
                    println("      ⏰ Hora: ${match.time}")
                    println("      📊 Estado: ${match.status}")
                    println("      🏟️ Local ID: ${match.homeTeamId}, Visitante ID: ${match.awayTeamId}")
                    println()
                }
            } else {
                println("   ❌ No hay partidos programados para el 30 de septiembre de 2025")
            }
            
            // 3. Mostrar algunos partidos de ejemplo para verificar el formato de fechas
            println("3️⃣ Primeros 10 partidos obtenidos (para verificar formato):")
            matches.take(10).forEach { match ->
                println("   🏀 ${match.homeTeamName} vs ${match.awayTeamName}")
                println("      📅 Fecha: ${match.date} | ⏰ Hora: ${match.time} | 📊 Estado: ${match.status}")
            }
            
            // 4. Analizar rango de fechas
            println("\n4️⃣ Análisis de fechas:")
            val dates = matches.mapNotNull { match ->
                try {
                    LocalDate.parse(match.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    null
                }
            }.sorted()
            
            if (dates.isNotEmpty()) {
                println("   📅 Primera fecha: ${dates.first()}")
                println("   📅 Última fecha: ${dates.last()}")
                println("   📊 Total fechas únicas: ${dates.distinct().size}")
                
                // Verificar si hay partidos en septiembre 2025
                val septemberMatches = dates.filter { it.year == 2025 && it.monthValue == 9 }
                println("   🗓️ Partidos en septiembre 2025: ${septemberMatches.size}")
                if (septemberMatches.isNotEmpty()) {
                    println("       Días con partidos en septiembre: ${septemberMatches.distinct().sorted()}")
                }
            }
            
        } catch (e: Exception) {
            println("❌ Error durante el scraping: ${e.message}")
            e.printStackTrace()
        }
    }
}
