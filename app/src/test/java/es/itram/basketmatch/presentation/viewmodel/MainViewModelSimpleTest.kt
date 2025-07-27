package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.domain.service.DataSyncService
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import es.itram.basketmatch.testutil.MainDispatcherRule
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelSimpleTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test basic ViewModel creation`() = runTest {
        // Given
        // Mock Android Log with updated messages
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d("MainViewModel", "🚀 Inicializando MainViewModel con sincronización automática...") } returns 0
        every { android.util.Log.d("MainViewModel", "✅ Datos actuales - cargando desde base de datos local") } returns 0
        every { android.util.Log.d("MainViewModel", "📱 Cargando datos desde base de datos local...") } returns 0
        every { android.util.Log.d("MainViewModel", match { it.contains("✅ Equipos cargados:") }) } returns 0
        every { android.util.Log.d("MainViewModel", match { it.contains("✅ Partidos cargados:") }) } returns 0
        
        val getAllTeamsUseCase: GetAllTeamsUseCase = mockk(relaxed = true)
        val getAllMatchesUseCase: GetAllMatchesUseCase = mockk(relaxed = true)
        val dataSyncService: DataSyncService = mockk(relaxed = true)
        
        val testTeams = TestDataFactory.createTestTeamList()
        val testMatches = TestDataFactory.createTestMatchList()
        
        every { getAllTeamsUseCase() } returns flowOf(testTeams)
        every { getAllMatchesUseCase() } returns flowOf(testMatches)
        coEvery { dataSyncService.isSyncNeeded() } returns false
        
        // When - This should not throw an exception
        val viewModel = MainViewModel(getAllMatchesUseCase, getAllTeamsUseCase, dataSyncService)
        
        // Then - Basic assertion
        assert(viewModel != null)
    }
}
