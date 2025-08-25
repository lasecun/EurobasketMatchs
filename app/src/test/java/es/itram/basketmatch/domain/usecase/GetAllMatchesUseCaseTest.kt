package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.domain.repository.MatchRepository
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
 * Tests unitarios para GetAllMatchesUseCase
 */
class GetAllMatchesUseCaseTest {

    private val matchRepository: MatchRepository = mockk()
    private lateinit var useCase: GetAllMatchesUseCase

    private val testMatches = TestDataFactory.createTestMatchList()

    @Before
    fun setup() {
        useCase = GetAllMatchesUseCase(matchRepository)
    }

    @Test
    fun `when invoke is called, should return all matches from repository`() = runTest {
        // Given
        every { matchRepository.getAllMatches() } returns flowOf(testMatches)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(testMatches, result.first())
        verify { matchRepository.getAllMatches() }
    }

    @Test
    fun `when repository returns empty list, should return empty list`() = runTest {
        // Given
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList<Any>(), result.first())
        verify { matchRepository.getAllMatches() }
    }

    @Test
    fun `when repository throws exception, should propagate exception`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")
        every { matchRepository.getAllMatches() } throws exception

        // When & Then
        try {
            useCase().toList()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Repository error", e.message)
        }
    }
}
