package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.data.sync.SmartSyncManager
import es.itram.basketmatch.data.sync.SmartSyncState
import es.itram.basketmatch.data.sync.UpdateCheckResult
import es.itram.basketmatch.domain.model.DataVersion
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class ManageStaticDataUseCaseTest {

    @MockK
    private lateinit var smartSyncManager: SmartSyncManager

    private lateinit var manageStaticDataUseCase: ManageStaticDataUseCase

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
        manageStaticDataUseCase = ManageStaticDataUseCase(smartSyncManager)
    }

    @Test
    fun `initializeStaticData delegates to SmartSyncManager`() = runTest {
        // Given
        coEvery { smartSyncManager.initializeStaticData() } returns Result.success(Unit)

        // When
        val result = manageStaticDataUseCase.initializeStaticData()

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { smartSyncManager.initializeStaticData() }
    }

    @Test
    fun `initializeStaticData propagates failure from SmartSyncManager`() = runTest {
        // Given
        val exception = Exception("Initialization failed")
        coEvery { smartSyncManager.initializeStaticData() } returns Result.failure(exception)

        // When
        val result = manageStaticDataUseCase.initializeStaticData()

        // Then
        assertTrue("Should return failure", result.isFailure)
        assertEquals("Should propagate same exception", exception, result.exceptionOrNull())
        coVerify { smartSyncManager.initializeStaticData() }
    }

    @Test
    fun `syncDynamicData with default parameters calls SmartSyncManager correctly`() = runTest {
        // Given
        coEvery { smartSyncManager.syncDynamicData(false) } returns Result.success(Unit)

        // When
        val result = manageStaticDataUseCase.syncDynamicData()

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { smartSyncManager.syncDynamicData(false) }
    }

    @Test
    fun `syncDynamicData with forceSync=true passes parameter correctly`() = runTest {
        // Given
        coEvery { smartSyncManager.syncDynamicData(true) } returns Result.success(Unit)

        // When
        val result = manageStaticDataUseCase.syncDynamicData(forceSync = true)

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { smartSyncManager.syncDynamicData(true) }
    }

    @Test
    fun `checkForUpdates delegates to SmartSyncManager`() = runTest {
        // Given
        val updateResult = UpdateCheckResult.NoUpdatesAvailable
        coEvery { smartSyncManager.checkForUpdates() } returns updateResult

        // When
        val result = manageStaticDataUseCase.checkForUpdates()

        // Then
        assertEquals("Should return same result", updateResult, result)
        coVerify { smartSyncManager.checkForUpdates() }
    }

    @Test
    fun `checkForUpdates returns UpdatesAvailable when new version exists`() = runTest {
        // Given
        val currentVersion = DataVersion("1.0", "2025-08-15T10:00:00Z", "Current")
        val newVersion = DataVersion("1.1", "2025-08-20T10:00:00Z", "New")
        val updateResult = UpdateCheckResult.UpdatesAvailable(currentVersion, newVersion)
        coEvery { smartSyncManager.checkForUpdates() } returns updateResult

        // When
        val result = manageStaticDataUseCase.checkForUpdates()

        // Then
        assertTrue("Should be UpdatesAvailable", result is UpdateCheckResult.UpdatesAvailable)
        val updatesResult = result as UpdateCheckResult.UpdatesAvailable
        assertEquals("1.0", updatesResult.currentVersion.version)
        assertEquals("1.1", updatesResult.newVersion.version)
        coVerify { smartSyncManager.checkForUpdates() }
    }

    @Test
    fun `getSyncState returns SmartSyncManager's sync state flow`() = runTest {
        // Given
        val syncStateFlow = MutableStateFlow(initialSyncState)
        every { smartSyncManager.syncState } returns syncStateFlow

        // When
        val result = manageStaticDataUseCase.getSyncState()

        // Then
        assertSame("Should return same StateFlow", syncStateFlow, result)
        assertEquals("Should have same initial state", initialSyncState, result.first())
    }

    @Test
    fun `getLastSyncTime returns SmartSyncManager's last sync time flow`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val lastSyncTimeFlow = MutableStateFlow<LocalDateTime?>(now)
        every { smartSyncManager.lastSyncTime } returns lastSyncTimeFlow

        // When
        val result = manageStaticDataUseCase.getLastSyncTime()

        // Then
        assertSame("Should return same StateFlow", lastSyncTimeFlow, result)
        assertEquals("Should have same time", now, result.first())
    }

    @Test
    fun `isSyncInProgress correctly reflects sync state`() = runTest {
        // Given
        val syncingState = initialSyncState.copy(isSyncing = true)
        val syncStateFlow = MutableStateFlow(syncingState)
        every { smartSyncManager.syncState } returns syncStateFlow

        // When
        val result = manageStaticDataUseCase.isSyncInProgress()

        // Then
        assertTrue("Should indicate sync in progress", result.first())
    }

    @Test
    fun `isSyncInProgress returns false when not syncing`() = runTest {
        // Given
        val notSyncingState = initialSyncState.copy(isSyncing = false)
        val syncStateFlow = MutableStateFlow(notSyncingState)
        every { smartSyncManager.syncState } returns syncStateFlow

        // When
        val result = manageStaticDataUseCase.isSyncInProgress()

        // Then
        assertFalse("Should indicate sync not in progress", result.first())
    }

    @Test
    fun `multiple operations work correctly in sequence`() = runTest {
        // Given
        coEvery { smartSyncManager.initializeStaticData() } returns Result.success(Unit)
        coEvery { smartSyncManager.syncDynamicData(any()) } returns Result.success(Unit)
        coEvery { smartSyncManager.checkForUpdates() } returns UpdateCheckResult.NoUpdatesAvailable

        // When
        val initResult = manageStaticDataUseCase.initializeStaticData()
        val syncResult = manageStaticDataUseCase.syncDynamicData()
        val updateResult = manageStaticDataUseCase.checkForUpdates()

        // Then
        assertTrue("Init should succeed", initResult.isSuccess)
        assertTrue("Sync should succeed", syncResult.isSuccess)
        assertEquals("Updates should not be available", UpdateCheckResult.NoUpdatesAvailable, updateResult)
        
        coVerify { smartSyncManager.initializeStaticData() }
        coVerify { smartSyncManager.syncDynamicData(false) }
        coVerify { smartSyncManager.checkForUpdates() }
    }

    @Test
    fun `error handling preserves original exception details`() = runTest {
        // Given
        val originalException = RuntimeException("Network timeout after 30 seconds")
        coEvery { smartSyncManager.syncDynamicData(any()) } returns Result.failure(originalException)

        // When
        val result = manageStaticDataUseCase.syncDynamicData()

        // Then
        assertTrue("Should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertEquals("Should preserve original message", "Network timeout after 30 seconds", exception!!.message)
        assertTrue("Should be same exception type", exception is RuntimeException)
    }
}
