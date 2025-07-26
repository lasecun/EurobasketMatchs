package es.itram.basketmatch.integration

import kotlinx.coroutines.test.runTest
import org.jsoup.Jsoup
import org.junit.Test
import java.io.File

/**
 * Test simple para verificar temporada 2025-26
 */
class Season2025Test {

    @Test
    fun `verificar datos temporada 2025-26`() = runTest {
        val output = StringBuilder()
        
        fun log(message: String) {
            println(message)
            output.appendLine(message)
        }
        
        log("🏀 VERIFICANDO TEMPORADA 2025-26")
        log("=".repeat(40))
        
        try {
            val url = "https://www.euroleaguebasketball.net/en/euroleague/game-center/"
            log("📡 Conectando a: $url")
            
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            log("✅ Conexión exitosa")
            
            // Buscar referencias a temporada 2025-26
            val pageContent = document.html()
            
            if (pageContent.contains("2025-26") || pageContent.contains("E2025")) {
                log("✅ Temporada 2025-26 encontrada en la página")
            } else {
                log("⚠️ Temporada 2025-26 no encontrada explícitamente")
            }
            
            // Buscar equipos conocidos
            val knownTeams = listOf("Real Madrid", "Barcelona", "Panathinaikos", "Fenerbahce")
            val foundTeams = knownTeams.filter { team -> 
                pageContent.contains(team, ignoreCase = true) 
            }
            
            log("🏀 Equipos encontrados: ${foundTeams.size}")
            foundTeams.forEach { team ->
                log("   - $team")
            }
            
            // Buscar partidos programados
            if (pageContent.contains("2025-09-30") || pageContent.contains("September") || pageContent.contains("Sept")) {
                log("📅 Partidos programados encontrados")
            } else {
                log("⚠️ No se encontraron partidos programados")
            }
            
            log("✅ Test completado")
            
        } catch (e: Exception) {
            log("❌ Error: ${e.message}")
        }
        
        // Guardar resultados
        try {
            val outputFile = File("/Users/juanjomarti/Projects/t/season_2025_test.txt")
            outputFile.writeText(output.toString())
            log("📁 Resultados guardados en: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            log("⚠️ No se pudo guardar: ${e.message}")
        }
    }
}
