package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.data.sync.SmartSyncManager
import es.itram.basketmatch.data.sync.SmartSyncState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

/**
 * Test básico para ManageStaticDataUseCase
 */
class ManageStaticDataUseCaseBasicTest {

    @MockK
    private lateinit var smartSyncManager: SmartSyncManager

    private lateinit var manageStaticDataUseCase: ManageStaticDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        manageStaticDataUseCase = ManageStaticDataUseCase(smartSyncManager)
    }

    @Test
    fun `ManageStaticDataUseCase can be instantiated`() {
        // Given
        val useCase = ManageStaticDataUseCase(smartSyncManager)
        
        // Then
        assertNotNull("UseCase should be created", useCase)
    }

    @Test
    fun `initializeStaticData delegates to SmartSyncManager successfully`() = runTest {
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
    fun `syncDynamicData with default parameters works`() = runTest {
        // Given
        coEvery { smartSyncManager.syncDynamicData(false) } returns Result.success(Unit)

        // When
        val result = manageStaticDataUseCase.syncDynamicData()

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { smartSyncManager.syncDynamicData(false) }
    }

    @Test
    fun `syncDynamicData with forceSync parameter works`() = runTest {
        // Given
        coEvery { smartSyncManager.syncDynamicData(true) } returns Result.success(Unit)

        // When
        val result = manageStaticDataUseCase.syncDynamicData(forceSync = true)

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { smartSyncManager.syncDynamicData(true) }
    }

    @Test
    fun `getSyncState returns flow from SmartSyncManager`() {
        // Given
        val mockState = SmartSyncState(
            isInitializing = false,
            isSyncing = false,
            lastSyncSuccess = true,
            error = null,
            status = "Test"
        )
        val syncStateFlow = MutableStateFlow(mockState)
        every { smartSyncManager.syncState } returns syncStateFlow

        // When
        val result = manageStaticDataUseCase.getSyncState()

        // Then
        assertSame("Should return same StateFlow", syncStateFlow, result)
    }

    @Test
    fun `getLastSyncTime returns flow from SmartSyncManager`() {
        // Given
        val now = LocalDateTime.now()
        val lastSyncTimeFlow = MutableStateFlow<LocalDateTime?>(now)
        every { smartSyncManager.lastSyncTime } returns lastSyncTimeFlow

        // When
        val result = manageStaticDataUseCase.getLastSyncTime()

        // Then
        assertSame("Should return same StateFlow", lastSyncTimeFlow, result)
    }

    @Test
    fun `error handling preserves original exception details`() = runTest {
        // Given
        val originalException = RuntimeException("Network timeout")
        coEvery { smartSyncManager.syncDynamicData(any()) } returns Result.failure(originalException)

        // When
        val result = manageStaticDataUseCase.syncDynamicData()

        // Then
        assertTrue("Should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertEquals("Should preserve original message", "Network timeout", exception!!.message)
        assertTrue("Should be same exception type", exception is RuntimeException)
    }
}
