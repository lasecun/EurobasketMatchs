package es.itram.basketmatch.integration

import kotlinx.coroutines.test.runTest
import org.jsoup.Jsoup
import org.junit.Test
import java.io.File

/**
 * Test específico para el Game Center de EuroLeague
 */
class GameCenterTest {

    @Test
    fun `conectar al game center y analizar estructura HTML`() = runTest {
        val output = StringBuilder()
        
        fun log(message: String) {
            println(message)
            output.appendLine(message)
        }
        
        log("🔍 ANALIZANDO GAME CENTER DE EUROLEAGUE")
        log("=".repeat(50))
        
        try {
            val url = "https://www.euroleaguebasketball.net/en/euroleague/game-center/"
            log("📡 Conectando a: $url")
            
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(15000)
                .get()
            
            log("✅ Conexión exitosa")
            log("📄 Título de la página: ${document.title()}")
            
            // Analizar estructura
            val allDivs = document.select("div")
            log("📊 Total de divs: ${allDivs.size}")
            
            // Buscar clases que podrían contener partidos
            val classNames = allDivs.map { it.className() }.filter { it.isNotBlank() }.toSet()
            log("🔍 Clases CSS encontradas (${classNames.size} únicas):")
            classNames.take(20).forEach { className ->
                log("   - $className")
            }
            
            // Buscar texto que contenga "vs" o nombres de equipos conocidos
            val possibleMatches = allDivs.filter { div ->
                val text = div.text()
                text.length < 200 && (
                    text.contains("vs", ignoreCase = true) ||
                    text.contains("Real Madrid", ignoreCase = true) ||
                    text.contains("Barcelona", ignoreCase = true) ||
                    text.contains("Panathinaikos", ignoreCase = true) ||
                    text.contains("Olympiacos", ignoreCase = true) ||
                    text.contains("Fenerbahce", ignoreCase = true)
                )
            }
            
            log("\n🏀 Posibles elementos de partidos encontrados: ${possibleMatches.size}")
            possibleMatches.take(10).forEach { match ->
                log("   📋 ${match.text().take(100)}")
                log("       Clase: ${match.className()}")
            }
            
            // Analizar scripts que podrían contener datos JSON
            val scripts = document.select("script")
            val scriptsWithData = scripts.filter { 
                val scriptContent = it.html()
                scriptContent.contains("game", ignoreCase = true) ||
                scriptContent.contains("match", ignoreCase = true) ||
                scriptContent.contains("schedule", ignoreCase = true)
            }
            
            log("\n📜 Scripts con posibles datos de partidos: ${scriptsWithData.size}")
            
            log("\n✅ Análisis completado")
            
        } catch (e: Exception) {
            log("❌ Error: ${e.message}")
            log("📝 Tipo: ${e.javaClass.simpleName}")
            e.printStackTrace()
        }
        
        // Guardar resultados
        try {
            val outputFile = File("/Users/juanjomarti/Projects/t/game_center_analysis.txt")
            outputFile.writeText(output.toString())
            log("📁 Análisis guardado en: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            log("⚠️ No se pudo guardar: ${e.message}")
        }
    }
}
