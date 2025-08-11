package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TeamRosterViewModelFavoritesTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getTeamRosterUseCase: GetTeamRosterUseCase
    private lateinit var teamRepository: TeamRepository
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var viewModel: TeamRosterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTeamRosterUseCase = mockk()
        teamRepository = mockk(relaxed = true)
        analyticsManager = mockk(relaxed = true)
        viewModel = TeamRosterViewModel(getTeamRosterUseCase, teamRepository, analyticsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFavoriteStatusForTeam should update isFavorite to true when team is favorite`() = runTest {
        // Given
        val teamCode = "MAD"
        val favoriteTeam = Team(
            id = "real-madrid",
            name = "Real Madrid",
            shortName = "MAD",
            code = teamCode,
            city = "Madrid",
            country = "Spain",
            logoUrl = "https://example.com/madrid.png",
            founded = 1902,
            coach = "Chus Mateo",
            isFavorite = true
        )

        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(favoriteTeam)

        // When
        viewModel.loadFavoriteStatusForTeam(teamCode)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isFavorite)
        coVerify { teamRepository.getTeamByCode(teamCode) }
    }

    @Test
    fun `loadFavoriteStatusForTeam should update isFavorite to false when team is not favorite`() = runTest {
        // Given
        val teamCode = "FCB"
        val nonFavoriteTeam = Team(
            id = "fc-barcelona",
            name = "FC Barcelona",
            shortName = "FCB",
            code = teamCode,
            city = "Barcelona",
            country = "Spain",
            logoUrl = "https://example.com/barcelona.png",
            founded = 1899,
            coach = "Roger Grimau",
            isFavorite = false
        )

        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(nonFavoriteTeam)

        // When
        viewModel.loadFavoriteStatusForTeam(teamCode)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isFavorite)
        coVerify { teamRepository.getTeamByCode(teamCode) }
    }

    @Test
    fun `loadFavoriteStatusForTeam should handle null team gracefully`() = runTest {
        // Given
        val teamCode = "UNKNOWN"
        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(null)

        // When
        viewModel.loadFavoriteStatusForTeam(teamCode)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isFavorite)
        coVerify { teamRepository.getTeamByCode(teamCode) }
    }

    @Test
    fun `toggleFavorite should change from false to true and track analytics`() = runTest {
        // Given
        val teamCode = "MAD"
        val teamRoster = TestDataFactory.createTestTeamRoster(
            teamCode = teamCode,
            teamName = "Real Madrid"
        )

        // Load team roster first
        coEvery { getTeamRosterUseCase(teamCode) } returns Result.success(teamRoster)
        viewModel.loadTeamRoster(teamCode)
        advanceUntilIdle()

        // Set initial favorite status to false
        val nonFavoriteTeam = Team(
            id = "real-madrid",
            name = "Real Madrid",
            shortName = "MAD",
            code = teamCode,
            city = "Madrid",
            country = "Spain",
            logoUrl = "https://example.com/madrid.png",
            founded = 1902,
            coach = "Chus Mateo",
            isFavorite = false
        )
        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(nonFavoriteTeam)
        viewModel.loadFavoriteStatusForTeam(teamCode)
        advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isFavorite)
        coVerify { teamRepository.updateFavoriteStatusByCode(teamCode, true) }
        verify { 
            analyticsManager.trackFavoriteAdded(
                contentType = "team",
                contentId = teamCode
            ) 
        }
    }

    @Test
    fun `toggleFavorite should change from true to false and track analytics`() = runTest {
        // Given
        val teamCode = "FCB"
        val teamRoster = TestDataFactory.createTestTeamRoster(
            teamCode = teamCode,
            teamName = "FC Barcelona"
        )

        // Load team roster first
        coEvery { getTeamRosterUseCase(teamCode) } returns Result.success(teamRoster)
        viewModel.loadTeamRoster(teamCode)
        advanceUntilIdle()

        // Set initial favorite status to true
        val favoriteTeam = Team(
            id = "fc-barcelona",
            name = "FC Barcelona",
            shortName = "FCB",
            code = teamCode,
            city = "Barcelona",
            country = "Spain",
            logoUrl = "https://example.com/barcelona.png",
            founded = 1899,
            coach = "Roger Grimau",
            isFavorite = true
        )
        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(favoriteTeam)
        viewModel.loadFavoriteStatusForTeam(teamCode)
        advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isFavorite)
        coVerify { teamRepository.updateFavoriteStatusByCode(teamCode, false) }
        verify { 
            analyticsManager.trackFavoriteAdded(
                contentType = "team",
                contentId = teamCode
            ) 
        }
    }

    @Test
    fun `toggleFavorite should handle case when no team roster is loaded`() = runTest {
        // Given - no team roster loaded (uiState.teamRoster is null)

        // When
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isFavorite)
        // Verify no repository or analytics calls were made
        coVerify(exactly = 0) { teamRepository.updateFavoriteStatusByCode(any(), any()) }
        verify(exactly = 0) { 
            analyticsManager.trackFavoriteAdded(any(), any()) 
        }
    }

    @Test
    fun `favorites state should persist correctly across multiple operations`() = runTest {
        // Given
        val teamCode = "PAM"
        val teamRoster = TestDataFactory.createTestTeamRoster(
            teamCode = teamCode,
            teamName = "Panathinaikos Athens"
        )

        // Load team roster
        coEvery { getTeamRosterUseCase(teamCode) } returns Result.success(teamRoster)
        viewModel.loadTeamRoster(teamCode)
        advanceUntilIdle()

        // Initially not favorite
        val initialTeam = Team(
            id = "panathinaikos",
            name = "Panathinaikos Athens",
            shortName = "PAM",
            code = teamCode,
            city = "Athens",
            country = "Greece",
            logoUrl = "https://example.com/pam.png",
            founded = 1908,
            coach = "Ergin Ataman",
            isFavorite = false
        )
        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(initialTeam)
        viewModel.loadFavoriteStatusForTeam(teamCode)
        advanceUntilIdle()

        // When - First toggle (false -> true)
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isFavorite)

        // When - Second toggle (true -> false)
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isFavorite)

        // When - Third toggle (false -> true)
        viewModel.toggleFavorite()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isFavorite)

        // Verify repository calls were made
        coVerify(atLeast = 1) { teamRepository.updateFavoriteStatusByCode(teamCode, false) }
        coVerify(atLeast = 1) { teamRepository.updateFavoriteStatusByCode(teamCode, true) }
        
        // Verify analytics tracking for all toggles
        verify(exactly = 3) { 
            analyticsManager.trackFavoriteAdded(
                contentType = "team",
                contentId = teamCode
            ) 
        }
    }

    @Test
    fun `favorite status should be loaded automatically when team roster is loaded`() = runTest {
        // Given
        val teamCode = "UNI"
        val teamRoster = TestDataFactory.createTestTeamRoster(
            teamCode = teamCode,
            teamName = "UNICS Kazan"
        )
        val favoriteTeam = Team(
            id = "unics-kazan",
            name = "UNICS Kazan",
            shortName = "UNI",
            code = teamCode,
            city = "Kazan",
            country = "Russia",
            logoUrl = "https://example.com/unics.png",
            founded = 1991,
            coach = "Velimir Perasovic",
            isFavorite = true
        )

        coEvery { getTeamRosterUseCase(teamCode) } returns Result.success(teamRoster)
        coEvery { teamRepository.getTeamByCode(teamCode) } returns flowOf(favoriteTeam)

        // When
        viewModel.loadTeamRoster(teamCode)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.teamRoster)
        assertTrue(viewModel.uiState.value.isFavorite)
        assertEquals(teamCode, viewModel.uiState.value.teamRoster?.teamCode)
        
        // Verify that favorite status was loaded
        coVerify { teamRepository.getTeamByCode(teamCode) }
    }
}
