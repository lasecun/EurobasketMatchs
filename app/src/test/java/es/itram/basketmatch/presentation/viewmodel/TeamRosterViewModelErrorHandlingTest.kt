package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests adicionales para TeamRosterViewModel - Error handling y analytics
 */
@ExperimentalCoroutinesApi
class TeamRosterViewModelErrorHandlingTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var getTeamRosterUseCase: GetTeamRosterUseCase

    @MockK
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var viewModel: TeamRosterViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Unconfined)
        
        justRun { analyticsManager.trackScreenView(any(), any()) }
        justRun { analyticsManager.trackPlayerViewed(any(), any(), any()) }
        
        viewModel = TeamRosterViewModel(getTeamRosterUseCase, analyticsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clearError clears error state`() = runTest {
        // Given - trigger an error first
        val teamCode = "INVALID"
        coEvery { getTeamRosterUseCase(teamCode) } throws RuntimeException("Network error")
        viewModel.loadTeamRoster(teamCode)

        // Verify error exists
        val errorBefore = viewModel.uiState.first().error
        assertNotNull("Should have error before clearing", errorBefore)

        // When
        viewModel.clearError()

        // Then
        val errorAfter = viewModel.uiState.first().error
        assertNull("Error should be cleared", errorAfter)
    }

    @Test
    fun `clearSuccessMessage clears success message`() = runTest {
        // When
        viewModel.clearSuccessMessage()

        // Then
        val successMessage = viewModel.uiState.first().successMessage
        assertNull("Success message should be cleared", successMessage)
    }

    @Test
    fun `getPlayerById returns correct player when exists`() = runTest {
        // Given
        val teamRoster = createSampleTeamRoster()
        coEvery { getTeamRosterUseCase("MAD") } returns Result.success(teamRoster)
        viewModel.loadTeamRoster("MAD")

        // When
        val player = viewModel.getPlayerById("P001")

        // Then
        assertNotNull("Should find player", player)
        assertEquals("Should return correct player", "P001", player?.code)
        assertEquals("Should return correct player name", "Luka Doncic", player?.fullName)
    }

    @Test
    fun `getPlayerById returns null when player not found`() = runTest {
        // Given
        val teamRoster = createSampleTeamRoster()
        coEvery { getTeamRosterUseCase("MAD") } returns Result.success(teamRoster)
        viewModel.loadTeamRoster("MAD")

        // When
        val player = viewModel.getPlayerById("INVALID")

        // Then
        assertNull("Should not find non-existent player", player)
    }

    @Test
    fun `getPlayerById returns null when no roster loaded`() = runTest {
        // Given - no roster loaded

        // When
        val player = viewModel.getPlayerById("P001")

        // Then
        assertNull("Should return null when no roster loaded", player)
    }

    @Test
    fun `selectPlayer sets selected player and tracks analytics`() = runTest {
        // Given
        val player = createSamplePlayer()

        // When
        viewModel.selectPlayer(player)

        // Then
        val selectedPlayer = viewModel.getSelectedPlayer()
        assertEquals("Should set selected player", player, selectedPlayer)
        
        verify {
            analyticsManager.trackPlayerViewed(
                playerCode = "P001",
                playerName = "Luka",
                teamCode = ""
            )
        }
    }

    @Test
    fun `selectPlayer with roster loaded includes team code in analytics`() = runTest {
        // Given
        val teamRoster = createSampleTeamRoster()
        coEvery { getTeamRosterUseCase("MAD") } returns Result.success(teamRoster)
        viewModel.loadTeamRoster("MAD")
        
        val player = createSamplePlayer()

        // When
        viewModel.selectPlayer(player)

        // Then
        verify {
            analyticsManager.trackPlayerViewed(
                playerCode = "P001",
                playerName = "Luka",
                teamCode = "MAD"
            )
        }
    }

    @Test
    fun `trackScreenView calls analytics manager`() {
        // When
        viewModel.trackScreenView()

        // Then
        verify {
            analyticsManager.trackScreenView(
                AnalyticsManager.SCREEN_TEAM_ROSTER,
                "TeamRosterScreen"
            )
        }
    }

    @Test
    fun `loadTeamRoster with exception sets error and clears loading`() = runTest {
        // Given
        val teamCode = "MAD"
        val exception = RuntimeException("Network connection failed")
        coEvery { getTeamRosterUseCase(teamCode) } throws exception

        // When
        viewModel.loadTeamRoster(teamCode)

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse("Should not be loading", uiState.isLoading)
        assertEquals(
            "Should have error message",
            "Error al cargar roster: Network connection failed",
            uiState.error
        )
        assertNull("TeamRoster should be null", uiState.teamRoster)
    }

    @Test
    fun `loadTeamRoster with success loads roster correctly`() = runTest {
        // Given
        val teamCode = "MAD"
        val teamRoster = createSampleTeamRoster()
        coEvery { getTeamRosterUseCase(teamCode) } returns Result.success(teamRoster)

        // When
        viewModel.loadTeamRoster(teamCode)

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse("Should not be loading", uiState.isLoading)
        assertNull("Error should be null", uiState.error)
        assertEquals("Should have team roster", teamRoster, uiState.teamRoster)
    }

    @Test
    fun `loadTeamRoster with failure result sets error`() = runTest {
        // Given
        val teamCode = "MAD"
        val errorMessage = "Team not found"
        coEvery { getTeamRosterUseCase(teamCode) } returns Result.failure(RuntimeException(errorMessage))

        // When
        viewModel.loadTeamRoster(teamCode)

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse("Should not be loading", uiState.isLoading)
        assertEquals(
            "Should have error message",
            "Error al cargar roster: $errorMessage",
            uiState.error
        )
        assertNull("TeamRoster should be null", uiState.teamRoster)
    }

    private fun createSampleTeamRoster(): TeamRoster {
        return TeamRoster(
            teamCode = "MAD",
            teamName = "Real Madrid",
            season = "2024-25",
            players = listOf(createSamplePlayer()),
            coaches = emptyList()
        )
    }

    private fun createSamplePlayer(): Player {
        return Player(
            code = "P001",
            name = "Luka",
            surname = "Doncic",
            fullName = "Luka Doncic",
            jersey = 7,
            position = PlayerPosition.POINT_GUARD,
            height = "201cm",
            weight = "104kg",
            dateOfBirth = "1999-02-28",
            placeOfBirth = "Ljubljana, Slovenia",
            nationality = "Slovenia",
            experience = 5,
            profileImageUrl = null,
            isActive = true,
            isStarter = false,
            isCaptain = false
        )
    }
}
