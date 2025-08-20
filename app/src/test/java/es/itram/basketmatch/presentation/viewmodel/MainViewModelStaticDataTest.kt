package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.data.sync.SmartSyncState
import es.itram.basketmatch.data.sync.UpdateCheckResult
import es.itram.basketmatch.domain.model.DataVersion
import es.itram.basketmatch.domain.usecase.GetMatchesByDateUseCase
import es.itram.basketmatch.domain.usecase.GetTeamsUseCase
import es.itram.basketmatch.domain.usecase.ManageStaticDataUseCase
import es.itram.basketmatch.domain.usecase.SyncDataUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class MainViewModelStaticDataTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var getMatchesByDateUseCase: GetMatchesByDateUseCase

    @MockK
    private lateinit var getTeamsUseCase: GetTeamsUseCase

    @MockK
    private lateinit var syncDataUseCase: SyncDataUseCase

    @MockK
    private lateinit var manageStaticDataUseCase: ManageStaticDataUseCase

    @MockK
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var viewModel: MainViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val initialSyncState = SmartSyncState(
        isInitializing = false,
        isSyncing = false,
        isActive = false,
        lastSyncSuccess = true,
        error = null,
        status = "Listo",
        progress = 0f
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        every { getMatchesByDateUseCase.invoke(any()) } returns MutableStateFlow(emptyList())
        every { getTeamsUseCase.invoke() } returns MutableStateFlow(emptyList())
        every { manageStaticDataUseCase.getSyncState() } returns MutableStateFlow(initialSyncState)
        every { manageStaticDataUseCase.getLastSyncTime() } returns MutableStateFlow(null)
        every { manageStaticDataUseCase.isSyncInProgress() } returns MutableStateFlow(false)
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit
        every { analyticsManager.logScreenView(any()) } returns Unit

        viewModel = MainViewModel(
            getMatchesByDateUseCase,
            getTeamsUseCase,
            syncDataUseCase,
            manageStaticDataUseCase,
            analyticsManager
        )
    }

    @Test
    fun `initializeApp calls initializeStaticData successfully`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.initializeStaticData() } returns Result.success(Unit)

        // When
        viewModel.initializeApp()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { manageStaticDataUseCase.initializeStaticData() }
    }

    @Test
    fun `initializeApp falls back to traditional sync when static data fails`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.initializeStaticData() } returns Result.failure(Exception("Static data failed"))
        coEvery { syncDataUseCase.invoke() } returns Result.success(Unit)

        // When
        viewModel.initializeApp()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { manageStaticDataUseCase.initializeStaticData() }
        coVerify { syncDataUseCase.invoke() }
    }

    @Test
    fun `performManualSync triggers dynamic data sync`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.syncDynamicData(true) } returns Result.success(Unit)

        // When
        viewModel.performManualSync()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { manageStaticDataUseCase.syncDynamicData(forceSync = true) }
    }

    @Test
    fun `performManualSync handles sync failure gracefully`() = runTest {
        // Given
        val exception = Exception("Sync failed")
        coEvery { manageStaticDataUseCase.syncDynamicData(true) } returns Result.failure(exception)

        // When
        viewModel.performManualSync()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { manageStaticDataUseCase.syncDynamicData(forceSync = true) }
        // Verify that the error doesn't crash the app
    }

    @Test
    fun `checkForUpdates returns correct update status`() = runTest {
        // Given
        val currentVersion = DataVersion("1.0", "2025-08-15T10:00:00Z", "Current")
        val newVersion = DataVersion("1.1", "2025-08-20T10:00:00Z", "New")
        val updateResult = UpdateCheckResult.UpdatesAvailable(currentVersion, newVersion)
        coEvery { manageStaticDataUseCase.checkForUpdates() } returns updateResult

        // When
        val result = viewModel.checkForUpdates()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Should return update result", updateResult, result)
        coVerify { manageStaticDataUseCase.checkForUpdates() }
    }

    @Test
    fun `checkForUpdates returns NoUpdatesAvailable when no updates`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.checkForUpdates() } returns UpdateCheckResult.NoUpdatesAvailable

        // When
        val result = viewModel.checkForUpdates()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Should return no updates", UpdateCheckResult.NoUpdatesAvailable, result)
        coVerify { manageStaticDataUseCase.checkForUpdates() }
    }

    @Test
    fun `smartSyncState flow is correctly exposed`() = runTest {
        // Given
        val syncingState = initialSyncState.copy(isSyncing = true, status = "Sincronizando...")
        val syncStateFlow = MutableStateFlow(syncingState)
        every { manageStaticDataUseCase.getSyncState() } returns syncStateFlow

        // Create new viewModel with updated mock
        val newViewModel = MainViewModel(
            getMatchesByDateUseCase,
            getTeamsUseCase,
            syncDataUseCase,
            manageStaticDataUseCase,
            analyticsManager
        )

        // When
        val currentState = newViewModel.smartSyncState.first()

        // Then
        assertEquals("Should expose sync state", syncingState, currentState)
        assertTrue("Should indicate syncing", currentState.isSyncing)
        assertEquals("Should have correct status", "Sincronizando...", currentState.status)
    }

    @Test
    fun `lastSyncTime flow is correctly exposed`() = runTest {
        // Given
        val syncTime = LocalDateTime.now()
        val syncTimeFlow = MutableStateFlow<LocalDateTime?>(syncTime)
        every { manageStaticDataUseCase.getLastSyncTime() } returns syncTimeFlow

        // Create new viewModel with updated mock
        val newViewModel = MainViewModel(
            getMatchesByDateUseCase,
            getTeamsUseCase,
            syncDataUseCase,
            manageStaticDataUseCase,
            analyticsManager
        )

        // When
        val currentTime = newViewModel.lastSyncTime.first()

        // Then
        assertEquals("Should expose last sync time", syncTime, currentTime)
    }

    @Test
    fun `isSyncInProgress correctly reflects sync status`() = runTest {
        // Given
        val syncInProgressFlow = MutableStateFlow(true)
        every { manageStaticDataUseCase.isSyncInProgress() } returns syncInProgressFlow

        // Create new viewModel with updated mock
        val newViewModel = MainViewModel(
            getMatchesByDateUseCase,
            getTeamsUseCase,
            syncDataUseCase,
            manageStaticDataUseCase,
            analyticsManager
        )

        // When
        val isSyncing = newViewModel.isSyncInProgress()

        // Then
        assertTrue("Should indicate sync in progress", isSyncing.first())
    }

    @Test
    fun `initialization sequence works correctly with static data`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.initializeStaticData() } returns Result.success(Unit)

        // When
        viewModel.initializeApp()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { manageStaticDataUseCase.initializeStaticData() }
        coVerify(exactly = 0) { syncDataUseCase.invoke() } // Should not call traditional sync
    }

    @Test
    fun `error states are handled properly during initialization`() = runTest {
        // Given
        val initException = RuntimeException("Initialization failed")
        coEvery { manageStaticDataUseCase.initializeStaticData() } returns Result.failure(initException)
        coEvery { syncDataUseCase.invoke() } returns Result.success(Unit)

        // When
        viewModel.initializeApp()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { manageStaticDataUseCase.initializeStaticData() }
        coVerify { syncDataUseCase.invoke() } // Should fallback to traditional sync
    }

    @Test
    fun `multiple manual syncs are handled correctly`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.syncDynamicData(true) } returns Result.success(Unit)

        // When
        viewModel.performManualSync()
        viewModel.performManualSync()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { manageStaticDataUseCase.syncDynamicData(forceSync = true) }
    }

    @Test
    fun `sync state updates are reflected in UI`() = runTest {
        // Given
        val syncStateFlow = MutableStateFlow(initialSyncState)
        every { manageStaticDataUseCase.getSyncState() } returns syncStateFlow

        val newViewModel = MainViewModel(
            getMatchesByDateUseCase,
            getTeamsUseCase,
            syncDataUseCase,
            manageStaticDataUseCase,
            analyticsManager
        )

        // When
        val initialState = newViewModel.smartSyncState.first()
        
        // Update the state
        val updatedState = initialSyncState.copy(isSyncing = true, status = "Actualizando...")
        syncStateFlow.value = updatedState
        
        val updatedUIState = newViewModel.smartSyncState.first()

        // Then
        assertEquals("Initial state should be correct", initialSyncState, initialState)
        assertEquals("Updated state should be reflected", updatedState, updatedUIState)
        assertTrue("Should show syncing", updatedUIState.isSyncing)
        assertEquals("Should show updated status", "Actualizando...", updatedUIState.status)
    }
}
