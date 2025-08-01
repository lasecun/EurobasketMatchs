package es.itram.basketmatch.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import es.itram.basketmatch.domain.service.DataSyncService
import es.itram.basketmatch.testutil.MainDispatcherRule
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tests específicos para la nueva funcionalidad de "no hay partidos hoy"
 */
@ExperimentalCoroutinesApi
class MainViewModelNoMatchesTodayTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAllMatchesUseCase = mockk<GetAllMatchesUseCase>()
    private val getAllTeamsUseCase = mockk<GetAllTeamsUseCase>()
    private val dataSyncService = mockk<DataSyncService>()
    private val analyticsManager = mockk<AnalyticsManager>(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        // Mock básico del DataSyncService
        coEvery { dataSyncService.isSyncNeeded() } returns false
        every { dataSyncService.syncProgress } returns MutableStateFlow(
            DataSyncService.SyncProgress(false, 0, 0, "")
        )
        
        // Mock básico de equipos (vacío para simplificar)
        every { getAllTeamsUseCase() } returns flowOf(emptyList())
    }

    @Test
    fun `findNextMatchDay should return next available match date`() = runTest {
        // Given - Partidos en fechas específicas
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val dayAfterTomorrow = today.plusDays(2)
        val nextWeek = today.plusDays(7)
        
        val matches = listOf(
            createMatch("1", tomorrow.atTime(20, 0)),      // Mañana
            createMatch("2", dayAfterTomorrow.atTime(18, 0)), // Pasado mañana
            createMatch("3", nextWeek.atTime(21, 0))       // Próxima semana
        )
        
        every { getAllMatchesUseCase() } returns flowOf(matches)
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        // Simular estar en "hoy" (sin partidos)
        viewModel.selectDate(today)
        
        // Then
        val nextMatchDay = viewModel.findNextMatchDay()
        assertThat(nextMatchDay).isEqualTo(tomorrow)
    }

    @Test
    fun `findNextMatchDay should return null when no future matches exist`() = runTest {
        // Given - Solo partidos del pasado
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        val matches = listOf(
            createMatch("1", yesterday.atTime(20, 0))  // Ayer
        )
        
        every { getAllMatchesUseCase() } returns flowOf(matches)
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        viewModel.selectDate(today)
        
        // Then
        val nextMatchDay = viewModel.findNextMatchDay()
        assertThat(nextMatchDay).isNull()
    }

    @Test
    fun `goToNextAvailableMatchDay should navigate to next match day when available`() = runTest {
        // Given
        val today = LocalDate.now()
        val nextMatchDay = today.plusDays(3)
        
        val matches = listOf(
            createMatch("1", nextMatchDay.atTime(20, 0))
        )
        
        every { getAllMatchesUseCase() } returns flowOf(matches)
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        viewModel.selectDate(today) // Empezar en hoy
        
        viewModel.goToNextAvailableMatchDay()
        
        // Then
        assertThat(viewModel.selectedDate.value).isEqualTo(nextMatchDay)
        assertThat(viewModel.matches.value).hasSize(1)
        assertThat(viewModel.matches.value.first().id).isEqualTo("1")
    }

    @Test
    fun `goToNextAvailableMatchDay should not change date when no future matches exist`() = runTest {
        // Given - Sin partidos futuros
        val today = LocalDate.now()
        
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        viewModel.selectDate(today)
        
        val originalDate = viewModel.selectedDate.value
        
        viewModel.goToNextAvailableMatchDay()
        
        // Then - La fecha no debe cambiar
        assertThat(viewModel.selectedDate.value).isEqualTo(originalDate)
    }

    @Test
    fun `isSelectedDateToday should return true when selected date is today`() = runTest {
        // Given
        val today = LocalDate.now()
        
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        viewModel.selectDate(today)
        
        // Then
        assertThat(viewModel.isSelectedDateToday()).isTrue()
    }

    @Test
    fun `isSelectedDateToday should return false when selected date is not today`() = runTest {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        
        every { getAllMatchesUseCase() } returns flowOf(emptyList())
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        viewModel.selectDate(tomorrow)
        
        // Then
        assertThat(viewModel.isSelectedDateToday()).isFalse()
    }

    @Test
    fun `findNextMatchDay should skip dates with no matches and find the correct next date`() = runTest {
        // Given - Partidos dispersos en el tiempo
        val today = LocalDate.now()
        val inFiveDays = today.plusDays(5)
        val inTenDays = today.plusDays(10)
        
        val matches = listOf(
            createMatch("1", inFiveDays.atTime(20, 0)),
            createMatch("2", inTenDays.atTime(18, 0))
        )
        
        every { getAllMatchesUseCase() } returns flowOf(matches)
        
        // When
        viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService, analyticsManager, savedStateHandle)
        
        viewModel.selectDate(today)
        
        // Then - Debe encontrar el primer día con partidos (día 5)
        val nextMatchDay = viewModel.findNextMatchDay()
        assertThat(nextMatchDay).isEqualTo(inFiveDays)
    }

    private fun createMatch(
        id: String,
        dateTime: LocalDateTime,
        homeTeamName: String = "Real Madrid",
        awayTeamName: String = "FC Barcelona"
    ): Match {
        return Match(
            id = id,
            homeTeamId = "MAD",
            homeTeamName = homeTeamName,
            homeTeamLogo = "https://example.com/madrid.png",
            awayTeamId = "BAR",
            awayTeamName = awayTeamName,
            awayTeamLogo = "https://example.com/barca.png",
            dateTime = dateTime,
            venue = "WiZink Center",
            round = 1,
            status = MatchStatus.SCHEDULED,
            homeScore = null,
            awayScore = null,
            seasonType = SeasonType.REGULAR
        )
    }
}
