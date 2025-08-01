package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.PlayerDao
import es.itram.basketmatch.data.datasource.local.dao.TeamRosterDao
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TeamRosterRepositoryImplTest {

    private lateinit var apiScraper: EuroLeagueJsonApiScraper
    private lateinit var teamRosterDao: TeamRosterDao
    private lateinit var playerDao: PlayerDao
    private lateinit var matchRepository: MatchRepository
    private lateinit var repository: TeamRosterRepositoryImpl

    @Before
    fun setup() {
        apiScraper = mockk()
        teamRosterDao = mockk()
        playerDao = mockk()
        matchRepository = mockk()
        
        // Configurar mocks por defecto
        coEvery { matchRepository.getAllMatches() } returns flowOf(emptyList())
        
        repository = TeamRosterRepositoryImpl(apiScraper, teamRosterDao, playerDao, matchRepository)
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

        // Mock que no hay cache válido
        coEvery { playerDao.getLatestPlayerByTeam("MAD") } returns null
        coEvery { teamRosterDao.getTeamRoster("MAD") } returns null
        coEvery { playerDao.getPlayersByTeamSync("MAD") } returns emptyList()
        
        // Mock API response
        coEvery { apiScraper.getTeamRoster("MAD", "E2025") } returns playersDto
        coEvery { apiScraper.getMatches() } returns emptyList() // Mock para obtener logo
        
        // Mock guardado en cache
        coEvery { teamRosterDao.insertTeamRoster(any()) } returns Unit
        coEvery { playerDao.insertPlayers(any()) } returns Unit

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
        // Mock que no hay cache válido
        coEvery { playerDao.getLatestPlayerByTeam("MAD") } returns null
        coEvery { teamRosterDao.getTeamRoster("MAD") } returns null
        coEvery { playerDao.getPlayersByTeamSync("MAD") } returns emptyList()
        
        // Mock API error
        coEvery { apiScraper.getTeamRoster("MAD", "E2025") } throws RuntimeException("Network error")
        coEvery { apiScraper.getMatches() } returns emptyList() // Mock para obtener logo

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
        
        // Mock primera llamada sin cache
        coEvery { playerDao.getLatestPlayerByTeam("MAD") } returns null
        coEvery { teamRosterDao.getTeamRoster("MAD") } returns null
        coEvery { playerDao.getPlayersByTeamSync("MAD") } returns emptyList()
        coEvery { apiScraper.getTeamRoster("MAD", "E2025") } returns playersDto
        coEvery { apiScraper.getMatches() } returns emptyList()
        coEvery { teamRosterDao.insertTeamRoster(any()) } returns Unit
        coEvery { playerDao.insertPlayers(any()) } returns Unit

        // When - First call to populate cache
        val firstResult = repository.getTeamRoster("MAD", "E2025")
        
        // Then
        assertTrue(firstResult.isSuccess)

        // Mock segunda llamada con cache válido
        val testPlayerEntity = TestDataFactory.createTestPlayerEntity(teamCode = "MAD")
        coEvery { playerDao.getLatestPlayerByTeam("MAD") } returns testPlayerEntity
        coEvery { teamRosterDao.getTeamRoster("MAD") } returns TestDataFactory.createTestTeamRosterEntity()
        coEvery { playerDao.getPlayersByTeamSync("MAD") } returns listOf(testPlayerEntity)

        // When - Second call should use cache
        val secondResult = repository.getTeamRoster("MAD", "E2025")

        // Then
        assertTrue(secondResult.isSuccess)
        assertEquals(firstResult.getOrNull()?.teamCode, secondResult.getOrNull()?.teamCode)
    }
}
