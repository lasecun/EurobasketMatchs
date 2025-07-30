package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import io.mockk.coEvery
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val getAllMatchesUseCase: GetAllMatchesUseCase = mockk()
    private lateinit var viewModel: CalendarViewModel

    private val testTeamHome = Team(
        teamId = "T1",
        teamName = "Team A",
        teamTla = "TA",
        imageUrl = "https://example.com/teamA.png",
        isFavorite = false
    )

    private val testTeamAway = Team(
        teamId = "T2", 
        teamName = "Team B",
        teamTla = "TB",
        imageUrl = "https://example.com/teamB.png",
        isFavorite = false
    )

    private val testMatch = Match(
        matchId = "M001",
        gameCode = "GC001",
        homeTeam = testTeamHome,
        awayTeam = testTeamAway,
        homeScore = 85,
        awayScore = 78,
        dateTime = "2024-03-15T20:00:00.000Z",
        roundNumber = 1,
        phaseTypeCode = "RS",
        seasonCode = "E2023",
        liveScore = "85-78",
        matchStatus = "FINISHED"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CalendarViewModel(getAllMatchesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load matches successfully`() = runTest {
        // Arrange
        val matches = listOf(testMatch)
        coEvery { getAllMatchesUseCase() } returns flowOf(Result.success(matches))

        // Act
        val newViewModel = CalendarViewModel(getAllMatchesUseCase)

        // Assert
        newViewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isFalse()
            assertThat(initialState.matches).isEqualTo(matches)
            assertThat(initialState.errorMessage).isNull()
        }
    }

    @Test
    fun `init should handle error loading matches`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        coEvery { getAllMatchesUseCase() } returns flowOf(Result.failure(Exception(errorMessage)))

        // Act
        val newViewModel = CalendarViewModel(getAllMatchesUseCase)

        // Assert
        newViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.matches).isEmpty()
            assertThat(state.errorMessage).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `filterMatchesByDate should filter matches correctly`() = runTest {
        // Arrange
        val date1 = LocalDate.of(2024, 3, 15)
        val date2 = LocalDate.of(2024, 3, 16)
        
        val match1 = testMatch.copy(
            matchId = "M001",
            dateTime = "2024-03-15T20:00:00.000Z"
        )
        val match2 = testMatch.copy(
            matchId = "M002", 
            dateTime = "2024-03-16T18:30:00.000Z"
        )
        
        val matches = listOf(match1, match2)
        coEvery { getAllMatchesUseCase() } returns flowOf(Result.success(matches))

        // Act
        val newViewModel = CalendarViewModel(getAllMatchesUseCase)

        // Assert
        newViewModel.uiState.test {
            val state = awaitItem()
            
            // Test filtering by first date
            val filteredMatches1 = newViewModel.filterMatchesByDate(state.matches, date1)
            assertThat(filteredMatches1).hasSize(1)
            assertThat(filteredMatches1.first().matchId).isEqualTo("M001")
            
            // Test filtering by second date
            val filteredMatches2 = newViewModel.filterMatchesByDate(state.matches, date2)
            assertThat(filteredMatches2).hasSize(1)
            assertThat(filteredMatches2.first().matchId).isEqualTo("M002")
        }
    }

    @Test
    fun `filterMatchesByDate should return empty list for date with no matches`() = runTest {
        // Arrange
        val matches = listOf(testMatch.copy(dateTime = "2024-03-15T20:00:00.000Z"))
        val filterDate = LocalDate.of(2024, 3, 20) // Different date
        coEvery { getAllMatchesUseCase() } returns flowOf(Result.success(matches))

        // Act
        val newViewModel = CalendarViewModel(getAllMatchesUseCase)

        // Assert
        newViewModel.uiState.test {
            val state = awaitItem()
            val filteredMatches = newViewModel.filterMatchesByDate(state.matches, filterDate)
            assertThat(filteredMatches).isEmpty()
        }
    }

    @Test
    fun `filterMatchesByDate should handle invalid date format gracefully`() = runTest {
        // Arrange
        val matches = listOf(testMatch.copy(dateTime = "invalid-date-format"))
        val filterDate = LocalDate.of(2024, 3, 15)
        coEvery { getAllMatchesUseCase() } returns flowOf(Result.success(matches))

        // Act
        val newViewModel = CalendarViewModel(getAllMatchesUseCase)

        // Assert
        newViewModel.uiState.test {
            val state = awaitItem()
            val filteredMatches = newViewModel.filterMatchesByDate(state.matches, filterDate)
            assertThat(filteredMatches).isEmpty()
        }
    }

    @Test
    fun `refreshMatches should reload matches`() = runTest {
        // Arrange
        val initialMatches = listOf(testMatch)
        val updatedMatches = listOf(
            testMatch,
            testMatch.copy(matchId = "M002", homeScore = 90, awayScore = 85)
        )
        
        coEvery { getAllMatchesUseCase() } returnsMany listOf(
            flowOf(Result.success(initialMatches)),
            flowOf(Result.success(updatedMatches))
        )

        // Act
        val newViewModel = CalendarViewModel(getAllMatchesUseCase)

        // Verify initial state
        newViewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.matches).hasSize(1)
            
            // Act - refresh
            newViewModel.refreshMatches()
            
            val refreshedState = awaitItem()
            assertThat(refreshedState.matches).hasSize(2)
        }
    }
}
