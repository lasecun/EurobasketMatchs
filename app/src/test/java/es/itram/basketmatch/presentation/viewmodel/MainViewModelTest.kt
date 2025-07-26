package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import es.itram.basketmatch.testutil.MainDispatcherRule
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.After
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
    private val getAllTeamsUseCase: GetAllTeamsUseCase = mockk(relaxed = true)
    private val getAllMatchesUseCase: GetAllMatchesUseCase = mockk(relaxed = true)

    // System under test
    private lateinit var viewModel: MainViewModel

    // Test data
    private val testTeams = TestDataFactory.createTestTeamList()
    private val testMatches = TestDataFactory.createTestMatchList()

    @Before
    fun setup() = runTest {
        // Mock Android Log with literal strings
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d("MainViewModel", "Inicializando MainViewModel...") } returns 0
        every { android.util.Log.d("MainViewModel", "Iniciando carga de datos...") } returns 0
        every { android.util.Log.d("MainViewModel", "Cargando equipos...") } returns 0
        every { android.util.Log.d("MainViewModel", match { it.contains("Equipos cargados:") }) } returns 0
        every { android.util.Log.d("MainViewModel", "Cargando partidos...") } returns 0
        every { android.util.Log.d("MainViewModel", match { it.contains("Partidos cargados:") }) } returns 0
        every { android.util.Log.d("MainViewModel", "Carga de datos completada") } returns 0
        every { android.util.Log.e("MainViewModel", match { it.contains("Error") }) } returns 0
        
        // Setup default mock behavior
        every { getAllTeamsUseCase() } returns flowOf(testTeams)
        every { getAllMatchesUseCase() } returns flowOf(testMatches)
        
        // Create ViewModel after mocks are set up
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        
        // Allow initialization to complete
        delay(100)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when ViewModel is created, then loads teams and matches`() = runTest {
        // Given - ViewModel is created in setup with mocks configured

        // Then - Wait for data to load and verify
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
        // Given - ViewModel is created in setup with successful mocks

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
        // Re-setup Android Log mocking for this specific test
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d("MainViewModel", "Inicializando MainViewModel...") } returns 0
        every { android.util.Log.d("MainViewModel", "Iniciando carga de datos...") } returns 0
        every { android.util.Log.d("MainViewModel", "Cargando equipos...") } returns 0
        every { android.util.Log.e("MainViewModel", match { it.contains("Error cargando datos:") }, any()) } returns 0
        
        // Clear previous mocks and set new ones
        clearMocks(getAllTeamsUseCase)
        every { getAllTeamsUseCase() } returns flow { throw RuntimeException(errorMessage) }
        every { getAllMatchesUseCase() } returns flowOf(testMatches)

        // When - Create a new ViewModel with the error mock
        val failingViewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        
        // Allow some time for the error to be set
        delay(200)

        // Then
        failingViewModel.error.test {
            val error = awaitItem()
            assertThat(error).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `when selectDate is called, then selectedDate is updated`() = runTest {
        // Given - ViewModel is created in setup
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
        // Given - ViewModel is created in setup
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
        // Given - ViewModel is created in setup
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
        // Given - ViewModel is created in setup
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
        // Given - ViewModel is created in setup

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
        // Given - ViewModel is created in setup

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
        // Given - Create ViewModel with error condition
        // Re-setup Android Log mocking for this specific test
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d("MainViewModel", "Inicializando MainViewModel...") } returns 0
        every { android.util.Log.d("MainViewModel", "Iniciando carga de datos...") } returns 0
        every { android.util.Log.d("MainViewModel", "Cargando equipos...") } returns 0
        every { android.util.Log.e("MainViewModel", match { it.contains("Error cargando datos:") }, any()) } returns 0
        
        clearMocks(getAllTeamsUseCase)
        every { getAllTeamsUseCase() } returns flow { throw RuntimeException("Test error") }
        val errorViewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase)
        
        // Allow time for error to be set first
        delay(200)

        // When
        errorViewModel.clearError()

        // Then
        errorViewModel.error.test {
            val error = awaitItem()
            assertThat(error).isNull()
        }
    }

    @Test
    fun `when getFormattedSelectedDate is called, then returns formatted date string`() = runTest {
        // Given - ViewModel is created in setup
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
