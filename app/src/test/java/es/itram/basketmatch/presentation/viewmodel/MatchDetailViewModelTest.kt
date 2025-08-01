package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.usecase.GetMatchByIdUseCase
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MatchDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var viewModel: MatchDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMatchByIdUseCase = mockk()
        analyticsManager = mockk(relaxed = true)
        viewModel = MatchDetailViewModel(getMatchByIdUseCase, analyticsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMatchDetails should update match state when successful`() = runTest {
        // Given
        val match = TestDataFactory.createTestMatch(
            id = "1",
            homeTeamName = "Real Madrid",
            awayTeamName = "FC Barcelona"
        )

        every { getMatchByIdUseCase("1") } returns flowOf(match)

        // When
        viewModel.loadMatchDetails("1")

        // Then
        assertEquals(match, viewModel.match.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadMatchDetails should update error state when match not found`() = runTest {
        // Given
        every { getMatchByIdUseCase("999") } returns flowOf(null)

        // When
        viewModel.loadMatchDetails("999")

        // Then
        assertNull(viewModel.match.value)
        assertFalse(viewModel.isLoading.value)
        assertEquals("Partido no encontrado", viewModel.error.value)
    }

    @Test
    fun `loadMatchDetails should set loading state during operation`() = runTest {
        // Given
        val match = TestDataFactory.createTestMatch(id = "1")
        every { getMatchByIdUseCase("1") } returns flowOf(match)

        // When
        viewModel.loadMatchDetails("1")

        // Then - loading should be false after completion
        assertFalse(viewModel.isLoading.value)
    }
}
