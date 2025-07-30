package es.itram.basketmatch.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import es.itram.basketmatch.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests para el sistema de progreso unificado del TeamRosterViewModel
 */
@ExperimentalCoroutinesApi
class TeamRosterViewModelProgressTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTeamRosterUseCase = mockk<GetTeamRosterUseCase>()
    private lateinit var viewModel: TeamRosterViewModel

    @Before
    fun setup() {
        viewModel = TeamRosterViewModel(getTeamRosterUseCase)
    }

    @Test
    fun `loadTeamRoster should clear progress after successful load`() = runTest {
        // Given
        val teamTla = "MAD"
        val players = listOf(
            createTestPlayer("P001", "Sergio", "Llull"),
            createTestPlayer("P002", "Facundo", "Campazzo"),
            createTestPlayer("P003", "Edy", "Tavares")
        )
        
        val teamRoster = TeamRoster(
            teamCode = teamTla,
            teamName = "Real Madrid",
            season = "E2025",
            players = players,
            coaches = emptyList(),
            logoUrl = "https://example.com/logo.png"
        )
        
        coEvery { getTeamRosterUseCase(teamTla) } returns Result.success(teamRoster)
        
        // When
        viewModel.loadTeamRoster(teamTla)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
        assertThat(uiState.teamRoster).isEqualTo(teamRoster)
        assertThat(uiState.loadingProgress).isNull() // Progress should be cleared
        assertThat(uiState.successMessage).isNull() // No success messages anymore
    }

    @Test
    fun `refreshTeamRoster should clear progress after successful refresh`() = runTest {
        // Given
        val teamTla = "FCB"
        val players = listOf(
            createTestPlayer("P001", "Nikola", "Mirotic"),
            createTestPlayer("P002", "Cory", "Higgins"),
            createTestPlayer("P003", "Jan", "Vesely"),
            createTestPlayer("P004", "Kevin", "Punter"),
            createTestPlayer("P005", "Willy", "Hernangómez")
        )
        
        val teamRoster = TeamRoster(
            teamCode = teamTla,
            teamName = "FC Barcelona",
            season = "E2025", 
            players = players,
            coaches = emptyList(),
            logoUrl = "https://example.com/fcb-logo.png"
        )
        
        coEvery { getTeamRosterUseCase.refresh(teamTla) } returns Result.success(teamRoster)
        
        // When
        viewModel.refreshTeamRoster(teamTla)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isRefreshing).isFalse()
        assertThat(uiState.error).isNull()
        assertThat(uiState.teamRoster).isEqualTo(teamRoster)
        assertThat(uiState.loadingProgress).isNull() // Progress should be cleared
        assertThat(uiState.successMessage).isNull() // No success messages anymore
    }

    @Test
    fun `loadTeamRoster should handle empty roster correctly`() = runTest {
        // Given
        val teamTla = "EMPTY"
        val teamRoster = TeamRoster(
            teamCode = teamTla,
            teamName = "Empty Team",
            season = "E2025",
            players = emptyList(),
            coaches = emptyList(),
            logoUrl = null
        )
        
        coEvery { getTeamRosterUseCase(teamTla) } returns Result.success(teamRoster)
        
        // When
        viewModel.loadTeamRoster(teamTla)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
        assertThat(uiState.teamRoster).isEqualTo(teamRoster)
        assertThat(uiState.loadingProgress).isNull() // Progress should be cleared
        assertThat(uiState.successMessage).isNull() // No success messages anymore
    }

    @Test
    fun `loadTeamRoster should clear progress on failure`() = runTest {
        // Given
        val teamTla = "ERR"
        val errorMessage = "Network error"
        
        coEvery { getTeamRosterUseCase(teamTla) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.loadTeamRoster(teamTla)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isEqualTo(errorMessage)
        assertThat(uiState.teamRoster).isNull()
        assertThat(uiState.loadingProgress).isNull() // Progress should be cleared on error
        assertThat(uiState.successMessage).isNull()
    }

    @Test
    fun `resetMessages should clear error and success messages but preserve roster data`() = runTest {
        // Given - Load a roster and then trigger an error
        val teamTla = "MAD"
        val teamRoster = TeamRoster(
            teamCode = teamTla,
            teamName = "Real Madrid",
            season = "E2025",
            players = listOf(createTestPlayer("P001", "Test", "Player")),
            coaches = emptyList(),
            logoUrl = null
        )
        
        coEvery { getTeamRosterUseCase(teamTla) } returns Result.success(teamRoster)
        viewModel.loadTeamRoster(teamTla)
        
        // Simulate an error afterwards
        coEvery { getTeamRosterUseCase("INVALID") } returns Result.failure(Exception("Error"))
        viewModel.loadTeamRoster("INVALID")
        
        // Verify there's an error
        assertThat(viewModel.uiState.value.error).isNotNull()
        
        // When
        viewModel.resetMessages()
        
        // Then
        assertThat(viewModel.uiState.value.error).isNull()
        assertThat(viewModel.uiState.value.successMessage).isNull()
        // Previous roster data should still be preserved
        assertThat(viewModel.uiState.value.teamRoster).isNotNull()
    }

    private fun createTestPlayer(
        code: String,
        name: String,
        surname: String,
        jersey: Int = 1
    ): Player {
        return Player(
            code = code,
            name = name,
            surname = surname,
            fullName = "$name $surname",
            jersey = jersey,
            position = PlayerPosition.GUARD,
            height = "185cm",
            weight = "80kg",
            dateOfBirth = "1990-01-01T00:00:00",
            placeOfBirth = "Madrid",
            nationality = "Spain",
            experience = 5,
            profileImageUrl = "https://example.com/player.jpg",
            isActive = true,
            isStarter = false,
            isCaptain = false
        )
    }
}
