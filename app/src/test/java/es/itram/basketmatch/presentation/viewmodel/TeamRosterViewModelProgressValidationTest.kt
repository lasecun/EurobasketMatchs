package es.itram.basketmatch.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.testutil.MainDispatcherRule
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test simple para validar que el sistema de progreso unificado funciona correctamente
 */
@ExperimentalCoroutinesApi
class TeamRosterViewModelProgressValidationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTeamRosterUseCase = mockk<GetTeamRosterUseCase>()
    private lateinit var viewModel: TeamRosterViewModel

    @Before
    fun setup() {
        viewModel = TeamRosterViewModel(getTeamRosterUseCase, mockk(relaxed = true))
    }

    @Test
    fun `progress should be null after successful load`() = runTest {
        // Given
        val teamRoster = TestDataFactory.createTestTeamRoster()
        coEvery { getTeamRosterUseCase("MAD") } returns Result.success(teamRoster)

        // When
        viewModel.loadTeamRoster("MAD")

        // Then
        assertThat(viewModel.uiState.value.loadingProgress).isNull()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.teamRoster).isNotNull()
    }

    @Test
    fun `progress should be null after error`() = runTest {
        // Given
        coEvery { getTeamRosterUseCase("ERR") } returns Result.failure(Exception("Error"))

        // When
        viewModel.loadTeamRoster("ERR")

        // Then
        assertThat(viewModel.uiState.value.loadingProgress).isNull()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `progress should be null after refresh`() = runTest {
        // Given
        val teamRoster = TestDataFactory.createTestTeamRoster()
        coEvery { getTeamRosterUseCase.refresh("MAD") } returns Result.success(teamRoster)

        // When
        viewModel.refreshTeamRoster("MAD")

        // Then
        assertThat(viewModel.uiState.value.loadingProgress).isNull()
        assertThat(viewModel.uiState.value.isRefreshing).isFalse()
        assertThat(viewModel.uiState.value.teamRoster).isNotNull()
    }

    @Test
    fun `resetMessages should work correctly`() = runTest {
        // Given - crear un estado con error
        coEvery { getTeamRosterUseCase("ERR") } returns Result.failure(Exception("Test error"))
        viewModel.loadTeamRoster("ERR")
        assertThat(viewModel.uiState.value.error).isNotNull()

        // When
        viewModel.resetMessages()

        // Then
        assertThat(viewModel.uiState.value.error).isNull()
        assertThat(viewModel.uiState.value.successMessage).isNull()
    }
}
