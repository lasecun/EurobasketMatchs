package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.repository.TeamRepository
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para GetAllTeamsUseCase
 */
class GetAllTeamsUseCaseTest {

    private val teamRepository: TeamRepository = mockk()
    private lateinit var useCase: GetAllTeamsUseCase

    private val testTeams = TestDataFactory.createTestTeamList()

    @Before
    fun setup() {
        useCase = GetAllTeamsUseCase(teamRepository)
    }

    @Test
    fun `when invoke is called, should return all teams from repository`() = runTest {
        // Given
        every { teamRepository.getAllTeams() } returns flowOf(testTeams)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(testTeams, result.first())
        verify { teamRepository.getAllTeams() }
    }

    @Test
    fun `when repository returns empty list, should return empty list`() = runTest {
        // Given
        every { teamRepository.getAllTeams() } returns flowOf(emptyList())

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList<Any>(), result.first())
        verify { teamRepository.getAllTeams() }
    }

    @Test
    fun `when repository returns teams, should maintain order`() = runTest {
        // Given
        val orderedTeams = testTeams.sortedBy { it.name }
        every { teamRepository.getAllTeams() } returns flowOf(orderedTeams)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(orderedTeams, result.first())
        assertEquals("Team 1", result.first().first().name) // Primer equipo en orden alfabético
        verify { teamRepository.getAllTeams() }
    }

    @Test
    fun `when repository throws exception, should propagate exception`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")
        every { teamRepository.getAllTeams() } throws exception

        // When & Then
        try {
            useCase().toList()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Repository error", e.message)
        }
    }

    @Test
    fun `when repository returns teams with different properties, should return all`() = runTest {
        // Given
        val diverseTeams = listOf(
            TestDataFactory.createTestTeam("MAD", "Real Madrid", isFavorite = true),
            TestDataFactory.createTestTeam("BAR", "FC Barcelona", isFavorite = false),
            TestDataFactory.createTestTeam("MIL", "EA7 Emporio Armani Milan", country = "ITA")
        )
        every { teamRepository.getAllTeams() } returns flowOf(diverseTeams)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(diverseTeams, result.first())
        assertEquals(3, result.first().size)
        assertEquals(true, result.first()[0].isFavorite)
        assertEquals(false, result.first()[1].isFavorite)
        assertEquals("ITA", result.first()[2].country)
    }
}
