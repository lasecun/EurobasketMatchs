package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TeamRosterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getTeamRosterUseCase: GetTeamRosterUseCase
    private lateinit var viewModel: TeamRosterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamRosterUseCase = mockk()
        viewModel = TeamRosterViewModel(getTeamRosterUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTeamRoster should update state when successful`() = runTest {
        // Given
        val teamRoster = TestDataFactory.createTestTeamRoster(
            teamCode = "MAD",
            teamName = "Real Madrid"
        )

        coEvery { getTeamRosterUseCase("MAD") } returns Result.success(teamRoster)

        // When
        viewModel.loadTeamRoster("MAD")

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.teamRoster)
        assertEquals("MAD", viewModel.uiState.value.teamRoster?.teamCode)
        assertEquals(3, viewModel.uiState.value.teamRoster?.players?.size)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.loadingProgress) // Progress should be cleared after loading
    }

    @Test
    fun `loadTeamRoster should update error state when failure`() = runTest {
        // Given
        coEvery { 
            getTeamRosterUseCase("INVALID") 
        } returns Result.failure(RuntimeException("Team not found"))

        // When
        viewModel.loadTeamRoster("INVALID")

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.teamRoster)
        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Team not found", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.loadingProgress) // Progress should be cleared on error
    }

    @Test
    fun `refreshTeamRoster should update refresh state`() = runTest {
        // Given
        val teamRoster = TestDataFactory.createTestTeamRoster()
        coEvery { getTeamRosterUseCase.refresh("MAD", "2025-26") } returns Result.success(teamRoster)

        // When
        viewModel.refreshTeamRoster("MAD")

        // Then
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertNotNull(viewModel.uiState.value.teamRoster)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.loadingProgress) // Progress should be cleared after refresh
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Given - Set error state first
        coEvery { 
            getTeamRosterUseCase("INVALID") 
        } returns Result.failure(RuntimeException("Error"))
        viewModel.loadTeamRoster("INVALID")

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetMessages should clear error and success messages`() = runTest {
        // Given - Set error state first
        coEvery { 
            getTeamRosterUseCase("INVALID") 
        } returns Result.failure(RuntimeException("Error"))
        viewModel.loadTeamRoster("INVALID")

        // When
        viewModel.resetMessages()

        // Then
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
