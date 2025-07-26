package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import es.itram.basketmatch.testutil.MainDispatcherRule
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks
    private val getAllTeamsUseCase: GetAllTeamsUseCase = mockk()
    private val getAllMatchesUseCase: GetAllMatchesUseCase = mockk()

    // System under test
    private lateinit var viewModel: MainViewModel

    // Test data
    private val testTeams = TestDataFactory.createTestTeamList()
    private val testMatches = TestDataFactory.createTestMatchList()

    @Before
    fun setup() {
        // Setup default mock behavior
        coEvery { getAllTeamsUseCase() } returns flowOf(testTeams)
        coEvery { getAllMatchesUseCase() } returns flowOf(testMatches)
    }

    @Test
    fun `when ViewModel is created, then loads teams and matches`() = runTest {
        // Given - mocks are already set up

        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)

        // Then
        viewModel.teams.test {
            val teams = awaitItem()
            assertThat(teams).hasSize(2)
            assertThat(teams.values.first().name).isEqualTo("Real Madrid")
            assertThat(teams.values.elementAt(1).name).isEqualTo("FC Barcelona")
        }

        viewModel.isLoading.test {
            val isLoading = awaitItem()
            assertThat(isLoading).isFalse() // Should be false after loading completes
        }
    }

    @Test
    fun `when loadData succeeds, then error is null`() = runTest {
        // Given
        coEvery { getAllTeamsUseCase() } returns flowOf(testTeams)
        coEvery { getAllMatchesUseCase() } returns flowOf(testMatches)

        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)

        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertThat(error).isNull()
        }
    }

    @Test
    fun `when loadData fails, then error is set`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getAllTeamsUseCase() } throws RuntimeException(errorMessage)

        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)

        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertThat(error).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `when selectDate is called, then selectedDate is updated`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        val newDate = LocalDate.of(2024, 12, 25)

        // When
        viewModel.selectDate(newDate)

        // Then
        viewModel.selectedDate.test {
            val selectedDate = awaitItem()
            assertThat(selectedDate).isEqualTo(newDate)
        }
    }

    @Test
    fun `when goToPreviousDay is called, then selectedDate goes back one day`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        val initialDate = LocalDate.now()
        
        // When
        viewModel.goToPreviousDay()

        // Then
        viewModel.selectedDate.test {
            val selectedDate = awaitItem()
            assertThat(selectedDate).isEqualTo(initialDate.minusDays(1))
        }
    }

    @Test
    fun `when goToNextDay is called, then selectedDate goes forward one day`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        val initialDate = LocalDate.now()

        // When
        viewModel.goToNextDay()

        // Then
        viewModel.selectedDate.test {
            val selectedDate = awaitItem()
            assertThat(selectedDate).isEqualTo(initialDate.plusDays(1))
        }
    }

    @Test
    fun `when goToToday is called, then selectedDate is set to today`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        viewModel.selectDate(LocalDate.of(2020, 1, 1)) // Set to a different date

        // When
        viewModel.goToToday()

        // Then
        viewModel.selectedDate.test {
            val selectedDate = awaitItem()
            assertThat(selectedDate).isEqualTo(LocalDate.now())
        }
    }

    @Test
    fun `when getTeamById is called with valid id, then returns correct team`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)

        // Wait for teams to load
        viewModel.teams.test {
            awaitItem() // Wait for teams to be loaded

            // When
            val team = viewModel.getTeamById("1")

            // Then
            assertThat(team?.name).isEqualTo("Real Madrid")
        }
    }

    @Test
    fun `when getTeamById is called with invalid id, then returns null`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)

        // Wait for teams to load
        viewModel.teams.test {
            awaitItem() // Wait for teams to be loaded

            // When
            val team = viewModel.getTeamById("invalid_id")

            // Then
            assertThat(team).isNull()
        }
    }

    @Test
    fun `when clearError is called, then error is set to null`() = runTest {
        // Given
        coEvery { getAllTeamsUseCase() } throws RuntimeException("Test error")
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)

        // When
        viewModel.clearError()

        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertThat(error).isNull()
        }
    }

    @Test
    fun `when getFormattedSelectedDate is called, then returns formatted date string`() = runTest {
        // Given
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        val testDate = LocalDate.of(2024, 12, 25)
        viewModel.selectDate(testDate)

        // When
        val formattedDate = viewModel.getFormattedSelectedDate()

        // Then
        assertThat(formattedDate).isNotEmpty()
        assertThat(formattedDate).contains("25")
        assertThat(formattedDate).contains("2024")
    }
}
