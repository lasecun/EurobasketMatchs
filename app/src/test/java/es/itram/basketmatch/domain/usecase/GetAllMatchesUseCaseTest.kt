package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllMatchesUseCaseTest {

    private lateinit var repository: MatchRepository
    private lateinit var useCase: GetAllMatchesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAllMatchesUseCase(repository)
    }

    @Test
    fun `invoke should return all matches from repository`() = runTest {
        // Given
        val matches = listOf(
            TestDataFactory.createTestMatch(
                id = "1",
                homeTeamName = "Real Madrid",
                awayTeamName = "FC Barcelona"
            ),
            TestDataFactory.createTestMatch(
                id = "2", 
                homeTeamName = "Valencia",
                awayTeamName = "Baskonia"
            )
        )

        every { repository.getAllMatches() } returns flowOf(matches)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(2, result.first().size)
        assertEquals("Real Madrid", result.first()[0].homeTeamName)
        assertEquals("Valencia", result.first()[1].homeTeamName)
    }

    @Test
    fun `invoke should return empty list when no matches available`() = runTest {
        // Given
        every { repository.getAllMatches() } returns flowOf(emptyList())

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(0, result.first().size)
    }
}
