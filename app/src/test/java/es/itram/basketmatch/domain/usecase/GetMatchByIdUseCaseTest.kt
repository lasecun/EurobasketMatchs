package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetMatchByIdUseCaseTest {

    private lateinit var repository: MatchRepository
    private lateinit var useCase: GetMatchByIdUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetMatchByIdUseCase(repository)
    }

    @Test
    fun `invoke should return match when found`() = runTest {
        // Given
        val match = TestDataFactory.createTestMatch(
            id = "1",
            homeTeamName = "Real Madrid",
            awayTeamName = "FC Barcelona"
        )

        every { repository.getMatchById("1") } returns flowOf(match)

        // When
        val result = useCase("1").toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(match, result.first())
        assertEquals("Real Madrid", result.first()?.homeTeamName)
    }

    @Test
    fun `invoke should return null when match not found`() = runTest {
        // Given
        every { repository.getMatchById("999") } returns flowOf(null)

        // When
        val result = useCase("999").toList()

        // Then
        assertEquals(1, result.size)
        assertNull(result.first())
    }
}
