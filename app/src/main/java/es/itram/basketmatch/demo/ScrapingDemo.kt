package es.itram.basketmatch.demo

import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import kotlinx.coroutines.runBlocking

/**
 * Demo simple para obtener datos reales de EuroLeague
 * Para ejecutar: kotlinc -cp ... -script demo_scraper.kt
 */
fun main() {
    println("🚀 DEMO: Obteniendo datos reales de EuroLeague")
    println("=".repeat(60))
    
    val webScraper = EuroLeagueWebScraper()
    
    runBlocking {
        try {
            println("🏀 Scraping equipos...")
            val teams = webScraper.getTeams()
            
            println("✅ EQUIPOS OBTENIDOS: ${teams.size}")
            teams.forEach { team ->
                println("   🏀 ${team.name} (${team.shortCode}) - ${team.country}")
            }
            
            println("\n⚽ Scraping partidos...")
            val matches = webScraper.getMatches()
            
            println("✅ PARTIDOS OBTENIDOS: ${matches.size}")
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
                
                println("   $statusEmoji ${match.homeTeamName} vs ${match.awayTeamName}$scoreStr")
                println("      📅 ${match.date} ${match.time ?: ""} - ${match.venue ?: "TBD"}")
            }
            
            if (matches.size > 10) {
                println("   ... y ${matches.size - 10} partidos más")
            }
            
            println("\n📊 RESUMEN:")
            println("   🏀 Total equipos: ${teams.size}")
            println("   ⚽ Total partidos: ${matches.size}")
            println("   🌍 Países: ${teams.mapNotNull { it.country }.toSet().size}")
            println("   🔴 En vivo: ${matches.count { it.status.name.contains("LIVE") }}")
            println("   ✅ Finalizados: ${matches.count { it.status.name.contains("FINISHED") }}")
            println("   📅 Programados: ${matches.count { it.status.name.contains("SCHEDULED") }}")
            
            println("\n🎉 ¡DATOS REALES OBTENIDOS EXITOSAMENTE!")
            
        } catch (e: Exception) {
            println("❌ Error obteniendo datos: ${e.message}")
            println("📝 Tipo de error: ${e.javaClass.simpleName}")
            e.printStackTrace()
        }
    }
    
    println("=".repeat(60))
}
