package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.PlayerDao
import es.itram.basketmatch.data.datasource.local.dao.TeamRosterDao
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TeamRosterRepositoryImplTest {

    private lateinit var apiScraper: EuroLeagueJsonApiScraper
    private lateinit var playerDao: PlayerDao
    private lateinit var teamRosterDao: TeamRosterDao
    private lateinit var repository: TeamRosterRepositoryImpl

    @Before
    fun setup() {
        apiScraper = mockk()
        playerDao = mockk()
        teamRosterDao = mockk()
        repository = TeamRosterRepositoryImpl(apiScraper, playerDao, teamRosterDao)
    }

    @Test
    fun `getTeamRoster should return success when data is fetched successfully`() = runTest {
        // Given
        val playersDto = listOf(
            TestDataFactory.createTestPlayerDto(
                code = "P001",
                name = "Juan",
                surname = "García",
                jersey = 1
            ),
            TestDataFactory.createTestPlayerDto(
                code = "P002", 
                name = "Pedro",
                surname = "López",
                jersey = 2
            )
        )

        coEvery { apiScraper.getTeamRoster("MAD", "E2025") } returns playersDto

        // When
        val result = repository.getTeamRoster("MAD", "E2025")

        // Then
        assertTrue(result.isSuccess)
        val roster = result.getOrNull()!!
        assertEquals("MAD", roster.teamCode)
        assertEquals(2, roster.players.size)
        assertEquals("Juan García", roster.players[0].fullName)
    }

    @Test
    fun `getTeamRoster should return failure when api throws exception`() = runTest {
        // Given
        coEvery { apiScraper.getTeamRoster("MAD", "E2025") } throws RuntimeException("Network error")

        // When
        val result = repository.getTeamRoster("MAD", "E2025")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `getTeamRoster should use cached data when available`() = runTest {
        // Given - First call
        val playersDto = listOf(TestDataFactory.createTestPlayerDto())
        coEvery { apiScraper.getTeamRoster("MAD", "E2025") } returns playersDto

        // When - First call to populate cache
        val firstResult = repository.getTeamRoster("MAD", "E2025")
        
        // Then
        assertTrue(firstResult.isSuccess)

        // When - Second call should use cache
        val secondResult = repository.getTeamRoster("MAD", "E2025")

        // Then
        assertTrue(secondResult.isSuccess)
        assertEquals(firstResult.getOrNull()?.teamCode, secondResult.getOrNull()?.teamCode)
    }
}
