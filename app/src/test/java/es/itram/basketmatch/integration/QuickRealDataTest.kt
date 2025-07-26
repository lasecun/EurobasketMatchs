package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

/**
 * Test simple para obtener datos reales de EuroLeague
 */
class QuickRealDataTest {

    @Test
    fun `obtener datos reales de EuroLeague ahora mismo`() = runTest {
        val timestamp = System.currentTimeMillis()
        val output = StringBuilder()
        
        fun log(message: String) {
            println(message) // Aún intentamos mostrar en consola
            output.appendLine(message)
        }
        
        log("\n🚀 OBTENIENDO DATOS REALES DE EUROLEAGUE - $timestamp")
        log("=".repeat(60))
        
        val webScraper = EuroLeagueWebScraper()
        
        try {
            log("🏀 Scraping equipos de EuroLeague...")
            val teams = webScraper.getTeams()
            
            log("✅ EQUIPOS OBTENIDOS: ${teams.size}")
            teams.forEach { team ->
                log("   🏀 ${team.name} (${team.shortCode}) - ${team.country}")
            }
            
            log("\n⚽ Scraping partidos de EuroLeague...")
            log("📅 NOTA: Temporada 2025-26 comienza el 30 de septiembre de 2025")
            log("🔍 URL utilizada: https://www.euroleaguebasketball.net/en/euroleague/game-center/")
            
            val matches = webScraper.getMatches("2025-26")
            
            log("✅ PARTIDOS OBTENIDOS: ${matches.size}")
            
            if (matches.isEmpty()) {
                log("⚠️ NO SE ENCONTRARON PARTIDOS")
                log("💡 Esto es normal porque la temporada 2024-25 aún no ha comenzado")
                log("📅 Primera jornada programada: 30 de septiembre de 2024")
                log("🌐 Verificar manualmente: https://www.euroleaguebasketball.net/euroleague/schedule/")
            } else {
                matches.take(10).forEach { match ->
                    val statusEmoji = when {
                        match.status.name.contains("LIVE") -> "🔴"
                        match.status.name.contains("FINISHED") -> "✅"
                        match.status.name.contains("SCHEDULED") -> "📅"
                        else -> "⏸️"
                    }
                    
                    val scoreStr = if (match.homeScore != null && match.awayScore != null) {
                        " (${match.homeScore}-${match.awayScore})"
                    } else ""
                    
                    log("   $statusEmoji ${match.homeTeamName} vs ${match.awayTeamName}$scoreStr")
                    log("      📅 ${match.date} ${match.time ?: ""} - ${match.venue ?: "TBD"}")
                }
                
                if (matches.size > 10) {
                    log("   ... y ${matches.size - 10} partidos más")
                }
            }
            
            log("\n📊 RESUMEN:")
            log("   🏀 Total equipos: ${teams.size}")
            log("   ⚽ Total partidos: ${matches.size}")
            log("   🌍 Países: ${teams.mapNotNull { it.country }.toSet().size}")
            log("   🔴 En vivo: ${matches.count { it.status.name.contains("LIVE") }}")
            log("   ✅ Finalizados: ${matches.count { it.status.name.contains("FINISHED") }}")
            log("   📅 Programados: ${matches.count { it.status.name.contains("SCHEDULED") }}")
            
            log("\n🎉 DATOS REALES OBTENIDOS EXITOSAMENTE!")
            
        } catch (e: Exception) {
            log("❌ Error obteniendo datos: ${e.message}")
            log("📝 Tipo de error: ${e.javaClass.simpleName}")
            log("⚠️ Esto puede ser normal si no hay conexión a internet")
            
            // Stack trace para debug
            e.printStackTrace()
        }
        
        log("=".repeat(60))
        
        // Escribir al archivo para poder ver los resultados
        try {
            val outputFile = File("/Users/juanjomarti/Projects/t/euroleague_data.txt")
            outputFile.writeText(output.toString())
            log("📁 Resultados guardados en: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            log("⚠️ No se pudo guardar el archivo: ${e.message}")
        }
    }
}
