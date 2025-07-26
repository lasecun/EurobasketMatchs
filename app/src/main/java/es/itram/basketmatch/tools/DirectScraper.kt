package es.itram.basketmatch.tools

import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Direct scraping tool to obtain real EuroLeague data without test framework
 */
object DirectScraper {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("🏀 EUROLEAGUE DIRECT SCRAPER - Obteniendo datos reales...")
        
        val scraper = EuroLeagueWebScraper()
        val outputFile = File("euroleague_data.txt")
        
        try {
            runBlocking {
                val teams = scraper.getTeams()
                
                outputFile.writeText("=== DATOS REALES DE EUROLEAGUE ===\n\n")
                
                if (teams.isNotEmpty()) {
                    outputFile.appendText("✅ EQUIPOS OBTENIDOS (${teams.size}):\n")
                    teams.forEachIndexed { index, team ->
                        val info = "${index + 1}. ${team.name} (${team.fullName}) - País: ${team.country ?: "N/A"}"
                        println(info)
                        outputFile.appendText("$info\n")
                    }
                    
                    outputFile.appendText("\n=== DETALLES TÉCNICOS ===\n")
                    outputFile.appendText("Fecha obtención: ${System.currentTimeMillis()}\n")
                    outputFile.appendText("Método: Web scraping real de euroleaguebasketball.net\n")
                    outputFile.appendText("Estado: DATOS REALES OBTENIDOS CON ÉXITO\n")
                    
                    println("\n✅ Datos guardados en: ${outputFile.absolutePath}")
                    println("📊 Total equipos: ${teams.size}")
                    
                } else {
                    val error = "❌ No se obtuvieron datos reales"
                    println(error)
                    outputFile.appendText("$error\n")
                    outputFile.appendText("Posibles causas: conectividad, cambios en la web\n")
                }
            }
        } catch (e: Exception) {
            val error = "❌ Error durante scraping: ${e.message}"
            println(error)
            outputFile.appendText("$error\n")
            e.printStackTrace()
        }
    }
}
