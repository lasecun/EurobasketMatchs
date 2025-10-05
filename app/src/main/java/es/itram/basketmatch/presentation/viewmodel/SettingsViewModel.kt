package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.usecase.ManageStaticDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel para la gestión de configuración y sincronización
 * Maneja toda la lógica relacionada con settings, sync y verificaciones
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageStaticDataUseCase: ManageStaticDataUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    // Estados específicos para settings
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Datos del sistema de sincronización
    val smartSyncState = manageStaticDataUseCase.syncState
    val lastSyncTime = manageStaticDataUseCase.lastSyncTime

    init {
        Log.d(TAG, "🛠️ Inicializando SettingsViewModel")
        trackSettingsAccess()
    }

    /**
     * 📊 Analytics: Track settings screen access
     */
    private fun trackSettingsAccess() {
        analyticsManager.trackScreenView(
            screenName = AnalyticsManager.SCREEN_SETTINGS,
            screenClass = "SettingsViewModel"
        )
    }

    /**
     * 📊 Analytics: Track sync settings screen access
     */
    fun trackSyncSettingsAccess() {
        analyticsManager.logCustomEvent("sync_settings_accessed", android.os.Bundle().apply {
            putString("source", "settings_screen")
            putLong("timestamp", System.currentTimeMillis())
        })
    }

    /**
     * Ejecuta sincronización manual de datos
     */
    fun performManualSync() {
        if (_isSyncing.value) {
            Log.d(TAG, "⚠️ Sincronización ya en progreso, ignorando solicitud")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            _syncMessage.value = "Sincronizando datos desde API EuroLeague..."

            try {
                Log.d(TAG, "🔄 Iniciando sincronización manual...")

                // 📊 Analytics: Track manual sync start
                analyticsManager.logCustomEvent("manual_sync_started", android.os.Bundle().apply {
                    putString("source", "sync_settings")
                    putLong("timestamp", System.currentTimeMillis())
                })

                val result = manageStaticDataUseCase.refreshStaticDataFromApi()

                if (result.isSuccess) {
                    val generationResult = result.getOrNull()!!
                    Log.d(TAG, "✅ Sincronización manual exitosa: ${generationResult.teamsGenerated} equipos, ${generationResult.matchesGenerated} partidos")
                    
                    _syncMessage.value = "✅ Sincronización completada: ${generationResult.teamsGenerated} equipos, ${generationResult.matchesGenerated} partidos"

                    // 📊 Analytics: Track successful manual sync
                    analyticsManager.logCustomEvent("manual_sync_completed", android.os.Bundle().apply {
                        putString("result", "success")
                        putString("source", "sync_settings")
                        putInt("teams_synced", generationResult.teamsGenerated)
                        putInt("matches_synced", generationResult.matchesGenerated)
                    })

                    // Limpiar mensaje después de un tiempo
                    kotlinx.coroutines.delay(3000)
                    _syncMessage.value = null

                } else {
                    Log.e(TAG, "❌ Error en sincronización manual: ${result.exceptionOrNull()?.message}")
                    _error.value = "Error al sincronizar: ${result.exceptionOrNull()?.message}"

                    // 📊 Analytics: Track failed manual sync
                    analyticsManager.logCustomEvent("manual_sync_failed", android.os.Bundle().apply {
                        putString("error", result.exceptionOrNull()?.message ?: "Unknown error")
                        putString("source", "sync_settings")
                    })
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en sincronización manual", e)
                _error.value = "Error inesperado: ${e.message}"

                // 📊 Analytics: Track sync exception
                analyticsManager.logCustomEvent("manual_sync_exception", android.os.Bundle().apply {
                    putString("exception", e.message ?: "Unknown exception")
                    putString("source", "sync_settings")
                })

            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Ejecuta verificación de actualizaciones
     */
    fun performVerification() {
        if (_isVerifying.value) {
            Log.d(TAG, "⚠️ Verificación ya en progreso, ignorando solicitud")
            return
        }

        viewModelScope.launch {
            _isVerifying.value = true
            _error.value = null
            _verificationMessage.value = "Verificando actualizaciones disponibles..."

            try {
                Log.d(TAG, "🔍 Iniciando verificación de actualizaciones...")

                // 📊 Analytics: Track verification start
                analyticsManager.logCustomEvent("data_verification_started", android.os.Bundle().apply {
                    putString("source", "sync_settings")
                    putLong("timestamp", System.currentTimeMillis())
                })

                val result = manageStaticDataUseCase.checkForUpdates()

                if (result.isSuccess) {
                    val updateResult = result.getOrNull()!!
                    Log.d(TAG, "✅ Verificación completada: ${updateResult.message}")
                    
                    _verificationMessage.value = "✅ ${updateResult.message}"

                    // 📊 Analytics: Track verification result
                    analyticsManager.logCustomEvent("data_verification_completed", android.os.Bundle().apply {
                        putString("result", "success")
                        putString("message", updateResult.message)
                        putBoolean("static_updates_available", updateResult.hasStaticUpdates)
                        putBoolean("dynamic_updates_available", updateResult.hasDynamicUpdates)
                        putString("source", "sync_settings")
                    })

                    // Limpiar mensaje después de un tiempo
                    kotlinx.coroutines.delay(3000)
                    _verificationMessage.value = null

                } else {
                    Log.e(TAG, "❌ Error en verificación: ${result.exceptionOrNull()?.message}")
                    _error.value = "Error al verificar: ${result.exceptionOrNull()?.message}"

                    // 📊 Analytics: Track failed verification
                    analyticsManager.logCustomEvent("data_verification_failed", android.os.Bundle().apply {
                        putString("error", result.exceptionOrNull()?.message ?: "Unknown error")
                        putString("source", "sync_settings")
                    })
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en verificación", e)
                _error.value = "Error inesperado: ${e.message}"

                // 📊 Analytics: Track verification exception
                analyticsManager.logCustomEvent("data_verification_exception", android.os.Bundle().apply {
                    putString("exception", e.message ?: "Unknown exception")
                    putString("source", "sync_settings")
                })

            } finally {
                _isVerifying.value = false
            }
        }
    }

    /**
     * Limpia mensajes de error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Limpia mensajes de sincronización
     */
    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    /**
     * Limpia mensajes de verificación
     */
    fun clearVerificationMessage() {
        _verificationMessage.value = null
    }

    /**
     * Verifica si hay alguna operación en progreso
     */
    fun isAnyOperationInProgress(): Boolean {
        return _isSyncing.value || _isVerifying.value || manageStaticDataUseCase.isSyncInProgress()
    }

    /**
     * Obtiene información del estado actual de sincronización
     */
    fun getSyncStatusInfo(): String {
        return when {
            _isSyncing.value -> "Sincronizando datos..."
            _isVerifying.value -> "Verificando actualizaciones..."
            manageStaticDataUseCase.isSyncInProgress() -> "Operación de sincronización en progreso..."
            else -> "Listo"
        }
    }
}
