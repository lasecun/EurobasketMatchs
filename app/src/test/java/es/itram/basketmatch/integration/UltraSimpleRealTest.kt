package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File

/**
 * Ultra Simple Real Data Test - should actually run
 */
class UltraSimpleRealTest {

    @Test
    fun obtener_datos_reales_euroleague() {
        println("🚀 INICIANDO OBTENCIÓN DE DATOS REALES...")
        
        runBlocking {
            try {
                // Usar directamente el scraper
                val scraper = EuroLeagueWebScraper()
                val teams = scraper.getTeams()
                
                val outputFile = File("/Users/juanjomarti/Projects/t/euroleague_real_data.txt")
                
                outputFile.writeText("=== DATOS REALES DE EUROLEAGUE ===\n")
                outputFile.appendText("Fecha: ${System.currentTimeMillis()}\n\n")
                
                if (teams.isNotEmpty()) {
                    println("✅ Obtenidos ${teams.size} equipos reales")
                    outputFile.appendText("✅ EQUIPOS REALES OBTENIDOS: ${teams.size}\n\n")
                    
                    teams.forEachIndexed { index, team ->
                        val info = "${index + 1}. ${team.name} - ${team.fullName} (${team.country})"
                        println(info)
                        outputFile.appendText("$info\n")
                    }
                    
                    outputFile.appendText("\n=== DETALLES TÉCNICOS ===\n")
                    outputFile.appendText("URL base: https://www.euroleaguebasketball.net\n")
                    outputFile.appendText("Método: Web scraping con JSoup\n")
                    outputFile.appendText("Estado: ✅ DATOS REALES OBTENIDOS\n")
                    
                    println("📄 Datos guardados en: ${outputFile.absolutePath}")
                    
                } else {
                    println("❌ No se obtuvieron datos")
                    outputFile.appendText("❌ ERROR: No se obtuvieron datos\n")
                }
                
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                e.printStackTrace()
                
                val outputFile = File("/Users/juanjomarti/Projects/t/euroleague_error.txt")
                outputFile.writeText("ERROR al obtener datos reales:\n${e.message}\n\n${e.stackTrace.joinToString("\n")}")
            }
        }
    }
}
