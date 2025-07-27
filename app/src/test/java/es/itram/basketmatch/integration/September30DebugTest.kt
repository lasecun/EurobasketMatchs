package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Test específico para debuggear los partidos del 30 de septiembre de 2025
 */
class September30DebugTest {

    @Test
    fun `debug specific date September 30 2025`() = runTest {
        println("🔍 DEBUG ESPECÍFICO: 30 de septiembre de 2025")
        println("=".repeat(70))
        
        try {
            val webScraper = EuroLeagueWebScraper()
            val jsonApiScraper = EuroLeagueJsonApiScraper()
            val remoteDataSource = EuroLeagueRemoteDataSource(jsonApiScraper, webScraper)
            
            println("1️⃣ Obteniendo partidos para temporada 2025-26...")
            val matchesResult = remoteDataSource.getAllMatches("2025-26")
            
            if (matchesResult.isSuccess) {
                val matches = matchesResult.getOrNull() ?: emptyList()
                println("   ✅ Resultado exitoso: ${matches.size} partidos obtenidos")
                
                if (matches.isEmpty()) {
                    println("   ❌ NO SE ENCONTRARON PARTIDOS")
                    return@runTest
                }
                
                // Filtrar partidos del 30 de septiembre de 2025
                println("\n2️⃣ Buscando partidos del 30 de septiembre de 2025...")
                val targetDateStr = "2025-09-30"
                val matchesOnSeptember30 = matches.filter { match ->
                    match.date == targetDateStr
                }
                
                println("   📅 Partidos encontrados para el 30/09/2025: ${matchesOnSeptember30.size}")
                
                if (matchesOnSeptember30.isNotEmpty()) {
                    println("\n🏀 PARTIDOS DEL 30 DE SEPTIEMBRE 2025:")
                    matchesOnSeptember30.forEachIndexed { index, match ->
                        println("   ${index + 1}. ${match.homeTeamName} vs ${match.awayTeamName}")
                        println("      📅 Fecha: ${match.date}")
                        println("      ⏰ Hora: ${match.time}")
                        println("      🏟️ Venue: ${match.venue}")
                        println("      📊 Estado: ${match.status}")
                        println("      🆔 ID: ${match.id}")
                        println("      🔢 Round: ${match.round}")
                        println()
                    }
                } else {
                    println("   ❌ No hay partidos para el 30 de septiembre de 2025")
                    
                    // Mostrar algunos partidos de ejemplo para debug
                    println("\n🔍 Primeros 10 partidos obtenidos para referencia:")
                    matches.take(10).forEachIndexed { index, match ->
                        println("   ${index + 1}. ${match.homeTeamName} vs ${match.awayTeamName}")
                        println("      📅 Fecha: ${match.date} | ⏰ Hora: ${match.time}")
                    }
                }
                
                // Análisis de fechas
                println("\n3️⃣ Análisis de fechas obtenidas:")
                val allDates = matches.map { it.date }.distinct().sorted()
                println("   📊 Fechas únicas encontradas: ${allDates.size}")
                if (allDates.isNotEmpty()) {
                    println("   📅 Primera fecha: ${allDates.first()}")
                    println("   📅 Última fecha: ${allDates.last()}")
                    
                    val septemberDates = allDates.filter { it.startsWith("2025-09") }
                    println("   🗓️ Fechas de septiembre 2025: ${septemberDates.size}")
                    if (septemberDates.isNotEmpty()) {
                        println("      ${septemberDates}")
                    }
                }
                
            } else {
                println("   ❌ Error obteniendo partidos: ${matchesResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("❌ Error durante el test: ${e.message}")
            e.printStackTrace()
        }
    }
}
