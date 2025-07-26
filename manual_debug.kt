package es.itram.basketmatch.integration

import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup

/**
 * Test manual para verificar el scraping de EuroLeague
 */
fun main() {
    runBlocking {
        println("🔍 MANUAL DEBUG: Verificando Game Center de EuroLeague")
        println("=".repeat(70))
        
        try {
            val url = "https://www.euroleaguebasketball.net/en/euroleague/game-center/"
            println("🌐 Conectando a: $url")
            
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(15000)
                .get()
            
            println("✅ Página cargada correctamente")
            
            // Buscar el script que contiene los datos JSON
            val scripts = document.select("script")
            println("📄 Total de scripts encontrados: ${scripts.size}")
            
            var jsonDataFound = false
            scripts.forEachIndexed { index, script ->
                val scriptContent = script.html()
                if (scriptContent.contains("window.__INITIAL_STATE__") || 
                    scriptContent.contains("window.__NUXT__") ||
                    scriptContent.contains("gameData") ||
                    scriptContent.contains("matches") ||
                    scriptContent.contains("schedule")) {
                    
                    println("\n🎯 Script #$index contiene datos relevantes:")
                    println("   📏 Tamaño: ${scriptContent.length} caracteres")
                    
                    // Buscar indicios de datos de partidos
                    val searchTerms = listOf("2025", "match", "game", "schedule", "September", "season")
                    searchTerms.forEach { term ->
                        if (scriptContent.contains(term, ignoreCase = true)) {
                            println("   ✅ Contiene '$term'")
                        }
                    }
                    
                    // Mostrar una muestra del contenido
                    if (scriptContent.length > 200) {
                        println("   📝 Muestra del contenido:")
                        println("   ${scriptContent.substring(0, 200)}...")
                    }
                    
                    jsonDataFound = true
                }
            }
            
            if (!jsonDataFound) {
                println("⚠️ No se encontraron scripts con datos JSON relevantes")
                
                // Intentar buscar elementos HTML con información de partidos
                println("\n🔍 Buscando elementos HTML con información de partidos...")
                val gameElements = document.select("[class*=game], [class*=match], [class*=schedule]")
                println("📊 Elementos relacionados con partidos: ${gameElements.size}")
                
                gameElements.take(5).forEachIndexed { index, element ->
                    println("   #$index: ${element.tagName()} - class: ${element.className()}")
                    println("        texto: ${element.text().take(100)}...")
                }
            }
            
            // Verificar si la página está cargando contenido dinámicamente
            val bodyText = document.body().text()
            if (bodyText.contains("loading", ignoreCase = true) || 
                bodyText.contains("wait", ignoreCase = true) ||
                bodyText.length < 500) {
                println("⚠️ La página parece estar cargando contenido dinámicamente")
                println("   Texto del body: ${bodyText.take(200)}...")
            }
            
        } catch (e: Exception) {
            println("❌ Error durante el scraping: ${e.message}")
            e.printStackTrace()
        }
    }
}
