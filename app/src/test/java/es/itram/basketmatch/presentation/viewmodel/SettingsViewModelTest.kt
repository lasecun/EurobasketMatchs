package es.itram.basketmatch.presentation.viewmodel

import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.data.sync.SmartSyncState
import es.itram.basketmatch.data.sync.UpdateCheckResult
import es.itram.basketmatch.domain.usecase.ManageStaticDataUseCase
import es.itram.basketmatch.testutil.MainDispatcherRule
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime

/**
 * Tests para SettingsViewModel
 * Verifica la funcionalidad de sincronización, verificación y gestión de configuración
 */
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SettingsViewModel
    private val manageStaticDataUseCase: ManageStaticDataUseCase = mockk()
    private val analyticsManager: AnalyticsManager = mockk()

    @Before
    fun setup() {
        // Mock analytics
        every { analyticsManager.trackScreenView(any(), any()) } returns Unit
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit
        
        // Mock static data use case flows
        every { manageStaticDataUseCase.syncState } returns MutableStateFlow(
            SmartSyncState()
        )
        every { manageStaticDataUseCase.lastSyncTime } returns MutableStateFlow(
            null
        )
        every { manageStaticDataUseCase.isSyncInProgress() } returns false
    }

    private fun createViewModel() {
        viewModel = SettingsViewModel(
            manageStaticDataUseCase = manageStaticDataUseCase,
            analyticsManager = analyticsManager
        )
    }

    @Test
    fun `initial state is correct`() = runTest {
        // When
        createViewModel()

        // Then
        assertThat(viewModel.isSyncing.value).isFalse()
        assertThat(viewModel.isVerifying.value).isFalse()
        assertThat(viewModel.syncMessage.value).isNull()
        assertThat(viewModel.verificationMessage.value).isNull()
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `tracks settings screen view on initialization`() = runTest {
        // When
        createViewModel()

        // Then
        verify {
            analyticsManager.trackScreenView(
                screenName = AnalyticsManager.SCREEN_SETTINGS,
                screenClass = "SettingsViewModel"
            )
        }
    }

    @Test
    fun `performManualSync executes successfully`() = runTest {
        // Given
        val mockResult = TestDataFactory.createMockGenerationResult()
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } returns Result.success(mockResult)
        createViewModel()

        // When
        viewModel.performManualSync()
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isSyncing.value).isFalse() // Should be false after completion
        verify {
            analyticsManager.logCustomEvent("manual_sync_started", any())
            analyticsManager.logCustomEvent("manual_sync_completed", any())
        }
    }

    @Test
    fun `performManualSync handles failure correctly`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } returns Result.failure(
            Exception(errorMessage)
        )
        createViewModel()

        // When
        viewModel.performManualSync()
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isSyncing.value).isFalse()
        assertThat(viewModel.error.value).contains(errorMessage)
        verify {
            analyticsManager.logCustomEvent("manual_sync_started", any())
            analyticsManager.logCustomEvent("manual_sync_failed", any())
        }
    }

    @Test
    fun `performManualSync ignores concurrent requests`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } coAnswers {
            kotlinx.coroutines.delay(1000) // Simulate long operation
            Result.success(TestDataFactory.createMockGenerationResult())
        }
        createViewModel()

        // When
        viewModel.performManualSync() // First call
        assertThat(viewModel.isSyncing.value).isTrue()
        
        viewModel.performManualSync() // Second call should be ignored
        testScheduler.advanceUntilIdle()

        // Then - Only one sync should have been started
        verify(exactly = 1) {
            analyticsManager.logCustomEvent("manual_sync_started", any())
        }
    }

    @Test
    fun `performVerification executes successfully`() = runTest {
        // Given
        val updateResult = UpdateCheckResult(
            hasStaticUpdates = false,
            hasDynamicUpdates = true,
            message = "All data is up to date"
        )
        coEvery { manageStaticDataUseCase.checkForUpdates() } returns Result.success(updateResult)
        createViewModel()

        // When
        viewModel.performVerification()
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isVerifying.value).isFalse()
        verify {
            analyticsManager.logCustomEvent("data_verification_started", any())
            analyticsManager.logCustomEvent("data_verification_completed", any())
        }
    }

    @Test
    fun `performVerification handles failure correctly`() = runTest {
        // Given
        val errorMessage = "Verification failed"
        coEvery { manageStaticDataUseCase.checkForUpdates() } returns Result.failure(
            Exception(errorMessage)
        )
        createViewModel()

        // When
        viewModel.performVerification()
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isVerifying.value).isFalse()
        assertThat(viewModel.error.value).contains(errorMessage)
        verify {
            analyticsManager.logCustomEvent("data_verification_started", any())
            analyticsManager.logCustomEvent("data_verification_failed", any())
        }
    }

    @Test
    fun `performVerification ignores concurrent requests`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.checkForUpdates() } coAnswers {
            kotlinx.coroutines.delay(1000) // Simulate long operation
            Result.success(UpdateCheckResult(false, false, "Test"))
        }
        createViewModel()

        // When
        viewModel.performVerification() // First call
        assertThat(viewModel.isVerifying.value).isTrue()
        
        viewModel.performVerification() // Second call should be ignored
        testScheduler.advanceUntilIdle()

        // Then - Only one verification should have been started
        verify(exactly = 1) {
            analyticsManager.logCustomEvent("data_verification_started", any())
        }
    }

    @Test
    fun `clearError removes error message`() = runTest {
        // Given
        createViewModel()
        // Set an error manually through private field access simulation
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } returns Result.failure(
            Exception("Test error")
        )
        viewModel.performManualSync()
        testScheduler.advanceUntilIdle()
        
        // Verify error is set
        assertThat(viewModel.error.value).isNotNull()

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `clearSyncMessage removes sync message`() = runTest {
        // Given
        createViewModel()
        
        // When
        viewModel.clearSyncMessage()

        // Then
        assertThat(viewModel.syncMessage.value).isNull()
    }

    @Test
    fun `clearVerificationMessage removes verification message`() = runTest {
        // Given
        createViewModel()
        
        // When
        viewModel.clearVerificationMessage()

        // Then
        assertThat(viewModel.verificationMessage.value).isNull()
    }

    @Test
    fun `isAnyOperationInProgress returns true when syncing`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(TestDataFactory.createMockGenerationResult())
        }
        createViewModel()

        // When
        viewModel.performManualSync()

        // Then
        assertThat(viewModel.isAnyOperationInProgress()).isTrue()
    }

    @Test
    fun `isAnyOperationInProgress returns true when verifying`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.checkForUpdates() } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(UpdateCheckResult(false, false, "Test"))
        }
        createViewModel()

        // When
        viewModel.performVerification()

        // Then
        assertThat(viewModel.isAnyOperationInProgress()).isTrue()
    }

    @Test
    fun `isAnyOperationInProgress returns true when use case is syncing`() = runTest {
        // Given
        every { manageStaticDataUseCase.isSyncInProgress() } returns true
        createViewModel()

        // When & Then
        assertThat(viewModel.isAnyOperationInProgress()).isTrue()
    }

    @Test
    fun `isAnyOperationInProgress returns false when no operations`() = runTest {
        // Given
        createViewModel()

        // When & Then
        assertThat(viewModel.isAnyOperationInProgress()).isFalse()
    }

    @Test
    fun `getSyncStatusInfo returns correct status`() = runTest {
        // Given
        createViewModel()

        // When & Then
        assertThat(viewModel.getSyncStatusInfo()).isEqualTo("Listo")
    }

    @Test
    fun `getSyncStatusInfo returns syncing status when syncing`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(TestDataFactory.createMockGenerationResult())
        }
        createViewModel()

        // When
        viewModel.performManualSync()

        // Then
        assertThat(viewModel.getSyncStatusInfo()).isEqualTo("Sincronizando datos...")
    }

    @Test
    fun `getSyncStatusInfo returns verifying status when verifying`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.checkForUpdates() } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(UpdateCheckResult(false, false, "Test"))
        }
        createViewModel()

        // When
        viewModel.performVerification()

        // Then
        assertThat(viewModel.getSyncStatusInfo()).isEqualTo("Verificando actualizaciones...")
    }

    @Test
    fun `trackSyncSettingsAccess logs analytics event`() = runTest {
        // Given
        createViewModel()

        // When
        viewModel.trackSyncSettingsAccess()

        // Then
        verify {
            analyticsManager.logCustomEvent("sync_settings_accessed", any())
        }
    }

    @Test
    fun `handles exception during sync gracefully`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.refreshStaticDataFromApi() } throws RuntimeException("Unexpected error")
        createViewModel()

        // When
        viewModel.performManualSync()
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isSyncing.value).isFalse()
        assertThat(viewModel.error.value).contains("Unexpected error")
        verify {
            analyticsManager.logCustomEvent("manual_sync_exception", any())
        }
    }

    @Test
    fun `handles exception during verification gracefully`() = runTest {
        // Given
        coEvery { manageStaticDataUseCase.checkForUpdates() } throws RuntimeException("Verification error")
        createViewModel()

        // When
        viewModel.performVerification()
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isVerifying.value).isFalse()
        assertThat(viewModel.error.value).contains("Verification error")
        verify {
            analyticsManager.logCustomEvent("data_verification_exception", any())
        }
    }
}
