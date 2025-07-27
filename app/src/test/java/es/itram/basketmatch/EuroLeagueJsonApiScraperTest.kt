package es.itram.basketmatch.test

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper

/**
 * Test unitario para verificar que EuroLeagueJsonApiScraper funciona correctamente
 */
class EuroLeagueJsonApiScraperTest {
    
    @Test
    fun `test getTeams should return non-empty list`() = runBlocking {
        // Given
        val scraper = EuroLeagueJsonApiScraper()
        
        // When
        val teams = scraper.getTeams()
        
        // Then
        assertNotNull("Teams list should not be null", teams)
        assertTrue("Teams list should not be empty", teams.isNotEmpty())
        println("✅ Test getTeams passed: ${teams.size} teams found")
        
        // Verificar estructura de los equipos
        teams.take(3).forEach { team ->
            assertNotNull("Team name should not be null", team.name)
            assertNotNull("Team logo URL should not be null", team.logoUrl)
            println("📋 ${team.name} - Logo: ${team.logoUrl}")
        }
    }
    
    @Test
    fun `test getMatches should return non-empty list`() = runBlocking {
        // Given
        val scraper = EuroLeagueJsonApiScraper()
        
        // When
        val matches = scraper.getMatches("2025-26")
        
        // Then
        assertNotNull("Matches list should not be null", matches)
        assertTrue("Matches list should not be empty", matches.isNotEmpty())
        println("✅ Test getMatches passed: ${matches.size} matches found")
        
        // Verificar estructura de los partidos
        matches.take(3).forEach { match ->
            assertNotNull("Home team name should not be null", match.homeTeamName)
            assertNotNull("Away team name should not be null", match.awayTeamName)
            assertNotNull("Match date should not be null", match.date)
            println("⚽ ${match.homeTeamName} vs ${match.awayTeamName} - ${match.date}")
        }
        
        // Verificar si hay partidos del 30 de septiembre
        val september30Matches = matches.filter { it.date == "2025-09-30" }
        println("🎯 Partidos del 30/09/2025: ${september30Matches.size}")
    }
    
    @Test
    fun `test teams and matches integration`() = runBlocking {
        // Given
        val scraper = EuroLeagueJsonApiScraper()
        
        // When
        val teams = scraper.getTeams()
        val matches = scraper.getMatches("2025-26")
        
        // Then
        assertTrue("Should have teams", teams.isNotEmpty())
        assertTrue("Should have matches", matches.isNotEmpty())
        
        // Verificar que los equipos en los partidos están en la lista de equipos
        val teamNames = teams.map { it.name.lowercase() }.toSet()
        val matchTeamNames = matches.flatMap { 
            listOf(it.homeTeamName.lowercase(), it.awayTeamName.lowercase()) 
        }.toSet()
        
        println("✅ Integration test passed")
        println("📊 Teams: ${teams.size}, Matches: ${matches.size}")
        println("🔗 Team names overlap: ${teamNames.intersect(matchTeamNames).size}")
    }
}
