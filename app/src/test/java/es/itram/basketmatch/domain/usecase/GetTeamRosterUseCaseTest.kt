package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.repository.TeamRosterRepository
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTeamRosterUseCaseTest {

    private lateinit var repository: TeamRosterRepository
    private lateinit var useCase: GetTeamRosterUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTeamRosterUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        // Given
        val teamRoster = TestDataFactory.createTestTeamRoster(
            teamCode = "MAD",
            teamName = "Real Madrid"
        )

        coEvery { repository.getTeamRoster("MAD", "2025-26") } returns Result.success(teamRoster)

        // When
        val result = useCase("MAD")

        // Then
        assertTrue(result.isSuccess)
        val roster = result.getOrNull()!!
        assertEquals("MAD", roster.teamCode)
        assertEquals("Real Madrid", roster.teamName)
        assertEquals(3, roster.players.size)
    }

    @Test
    fun `invoke should return failure when repository returns failure`() = runTest {
        // Given
        coEvery { 
            repository.getTeamRoster("INVALID", "2025-26") 
        } returns Result.failure(RuntimeException("Team not found"))

        // When
        val result = useCase("INVALID")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }

    @Test
    fun `refresh should call refreshTeamRoster on repository`() = runTest {
        // Given
        val teamRoster = TestDataFactory.createTestTeamRoster()
        coEvery { repository.refreshTeamRoster("MAD", "2025-26") } returns Result.success(teamRoster)

        // When
        val result = useCase.refresh("MAD")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(teamRoster, result.getOrNull())
    }
}
