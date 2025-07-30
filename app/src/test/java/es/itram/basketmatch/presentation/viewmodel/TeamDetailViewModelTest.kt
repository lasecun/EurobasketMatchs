package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val teamRepository: TeamRepository = mockk()
    private lateinit var viewModel: TeamDetailViewModel

    private val testTeam = Team(
        teamId = "T001",
        teamName = "Real Madrid",
        teamTla = "MAD",
        imageUrl = "https://example.com/madrid.png",
        isFavorite = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTeamDetails should load team successfully`() = runTest {
        // Arrange
        val teamId = "T001"
        coEvery { teamRepository.getTeamById(teamId) } returns flowOf(Result.success(testTeam))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(teamId)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.team).isEqualTo(testTeam)
            assertThat(state.errorMessage).isNull()
        }

        coVerify { teamRepository.getTeamById(teamId) }
    }

    @Test
    fun `loadTeamDetails should handle error`() = runTest {
        // Arrange
        val teamId = "T001"
        val errorMessage = "Team not found"
        coEvery { teamRepository.getTeamById(teamId) } returns flowOf(Result.failure(Exception(errorMessage)))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(teamId)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.team).isNull()
            assertThat(state.errorMessage).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `loadTeamDetails should show loading state initially`() = runTest {
        // Arrange
        val teamId = "T001"
        coEvery { teamRepository.getTeamById(teamId) } returns flowOf(Result.success(testTeam))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)

        // Assert initial loading state
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            assertThat(initialState.team).isNull()
            assertThat(initialState.errorMessage).isNull()

            // Load team details
            viewModel.loadTeamDetails(teamId)

            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            assertThat(loadedState.team).isEqualTo(testTeam)
        }
    }

    @Test
    fun `toggleFavorite should update team favorite status`() = runTest {
        // Arrange
        val teamId = "T001"
        val favoriteTeam = testTeam.copy(isFavorite = true)
        
        coEvery { teamRepository.getTeamById(teamId) } returns flowOf(Result.success(testTeam))
        coEvery { teamRepository.toggleFavoriteTeam(teamId) } returns Result.success(favoriteTeam)

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(teamId)
        viewModel.toggleFavorite(teamId)

        // Assert
        viewModel.uiState.test {
            // Skip initial and loaded states
            awaitItem() // loading
            awaitItem() // loaded with original team
            
            val updatedState = awaitItem()
            assertThat(updatedState.team?.isFavorite).isTrue()
        }

        coVerify { teamRepository.toggleFavoriteTeam(teamId) }
    }

    @Test
    fun `toggleFavorite should handle error`() = runTest {
        // Arrange
        val teamId = "T001"
        val errorMessage = "Failed to update favorite"
        
        coEvery { teamRepository.getTeamById(teamId) } returns flowOf(Result.success(testTeam))
        coEvery { teamRepository.toggleFavoriteTeam(teamId) } returns Result.failure(Exception(errorMessage))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(teamId)
        viewModel.toggleFavorite(teamId)

        // Assert
        viewModel.uiState.test {
            // Skip initial and loaded states
            awaitItem() // loading
            awaitItem() // loaded
            
            val errorState = awaitItem()
            assertThat(errorState.errorMessage).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Arrange
        val teamId = "T001"
        val errorMessage = "Network error"
        coEvery { teamRepository.getTeamById(teamId) } returns flowOf(Result.failure(Exception(errorMessage)))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(teamId)

        // Assert
        viewModel.uiState.test {
            // Wait for error state
            val errorState = awaitItem()
            assertThat(errorState.errorMessage).isEqualTo(errorMessage)
            
            // Clear error
            viewModel.clearError()
            
            val clearedState = awaitItem()
            assertThat(clearedState.errorMessage).isNull()
        }
    }

    @Test
    fun `loadTeamDetails with empty teamId should handle gracefully`() = runTest {
        // Arrange
        val emptyTeamId = ""
        coEvery { teamRepository.getTeamById(emptyTeamId) } returns flowOf(Result.failure(Exception("Invalid team ID")))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(emptyTeamId)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.team).isNull()
            assertThat(state.errorMessage).isNotNull()
        }
    }

    @Test
    fun `multiple loadTeamDetails calls should handle correctly`() = runTest {
        // Arrange
        val teamId1 = "T001"
        val teamId2 = "T002"
        val team2 = testTeam.copy(teamId = "T002", teamName = "FC Barcelona")
        
        coEvery { teamRepository.getTeamById(teamId1) } returns flowOf(Result.success(testTeam))
        coEvery { teamRepository.getTeamById(teamId2) } returns flowOf(Result.success(team2))

        // Act
        viewModel = TeamDetailViewModel(teamRepository)
        viewModel.loadTeamDetails(teamId1)
        viewModel.loadTeamDetails(teamId2)

        // Assert
        viewModel.uiState.test {
            awaitItem() // initial loading
            awaitItem() // first team loaded
            
            val finalState = awaitItem()
            assertThat(finalState.team).isEqualTo(team2)
        }

        coVerify { teamRepository.getTeamById(teamId1) }
        coVerify { teamRepository.getTeamById(teamId2) }
    }
}
