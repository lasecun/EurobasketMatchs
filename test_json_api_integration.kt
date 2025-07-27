import kotlinx.coroutines.runBlocking
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.datasource.remote.EuroLeagueWebScraper
import es.itram.basketmatch.data.datasource.remote.EuroLeagueJsonApiScraper

/**
 * Test para validar la integración completa del nuevo sistema JSON API + HTML fallback
 */
fun main() = runBlocking {
    println("🧪 === TESTING EUROLEAGUE JSON API INTEGRATION ===")
    
    // Crear las instancias
    val webScraper = EuroLeagueWebScraper()
    val jsonApiScraper = EuroLeagueJsonApiScraper()
    val remoteDataSource = EuroLeagueRemoteDataSource(webScraper, jsonApiScraper)
    
    try {
        println("\n1️⃣ === TESTING TEAMS ===")
        val teamsResult = remoteDataSource.getAllTeams()
        
        if (teamsResult.isSuccess) {
            val teams = teamsResult.getOrNull() ?: emptyList()
            println("✅ Equipos obtenidos: ${teams.size}")
            
            // Mostrar algunos equipos para validar
            teams.take(3).forEach { team ->
                println("   📋 ${team.name} (${team.code}) - Logo: ${team.logoUrl}")
            }
        } else {
            println("❌ Error obteniendo equipos: ${teamsResult.exceptionOrNull()?.message}")
        }
        
        println("\n2️⃣ === TESTING MATCHES ===")
        val matchesResult = remoteDataSource.getAllMatches("2025-26")
        
        if (matchesResult.isSuccess) {
            val matches = matchesResult.getOrNull() ?: emptyList()
            println("✅ Partidos obtenidos: ${matches.size}")
            
            // Buscar partidos específicos para validar
            val september30Matches = matches.filter { it.date == "2025-09-30" }
            println("🎯 Partidos del 30/09/2025: ${september30Matches.size}")
            
            // Mostrar algunos partidos para validar
            matches.take(5).forEach { match ->
                println("   ⚽ ${match.homeTeam} vs ${match.awayTeam} - ${match.date} (${match.competition})")
            }
            
            // Validar estructura de datos
            if (matches.isNotEmpty()) {
                val firstMatch = matches.first()
                println("\n📊 Estructura del primer partido:")
                println("   - Fecha: ${firstMatch.date}")
                println("   - Hora: ${firstMatch.time}")
                println("   - Local: ${firstMatch.homeTeam}")
                println("   - Visitante: ${firstMatch.awayTeam}")
                println("   - Competición: ${firstMatch.competition}")
                println("   - URL: ${firstMatch.matchUrl}")
            }
        } else {
            println("❌ Error obteniendo partidos: ${matchesResult.exceptionOrNull()?.message}")
        }
        
        println("\n3️⃣ === TESTING JSON API DIRECTLY ===")
        try {
            val directTeams = jsonApiScraper.getTeams()
            val directMatches = jsonApiScraper.getMatches("2025-26")
            
            println("✅ API JSON directa - Equipos: ${directTeams.size}, Partidos: ${directMatches.size}")
            
            // Comparar calidad de datos
            if (directMatches.isNotEmpty() && matchesResult.isSuccess) {
                val totalMatches = matchesResult.getOrNull()?.size ?: 0
                println("📈 Comparación: JSON API (${directMatches.size}) vs Total obtenido (${totalMatches})")
            }
        } catch (e: Exception) {
            println("⚠️ Error en test directo de JSON API: ${e.message}")
        }
        
        println("\n✅ === TEST COMPLETADO ===")
        
    } catch (e: Exception) {
        println("❌ Error general en el test: ${e.message}")
        e.printStackTrace()
    }
}
