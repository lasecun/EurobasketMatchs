package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.notification.manager.NotificationManager
import es.itram.basketmatch.notification.manager.NotificationSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationSettingsVM"
    }

    private val _notificationSettings = MutableStateFlow(
        NotificationSettings(
            notificationsEnabled = true,
            matchRemindersEnabled = true,
            resultNotificationsEnabled = true,
            teamNewsEnabled = false,
            reminderTimeMinutes = 30
        )
    )
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    init {
        loadNotificationSettings()
    }

    /**
     * Carga la configuración actual de notificaciones
     */
    private fun loadNotificationSettings() {
        try {
            val settings = notificationManager.getNotificationSettings()
            _notificationSettings.value = settings
            Log.d(TAG, "✅ Configuración de notificaciones cargada: $settings")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cargando configuración de notificaciones", e)
        }
    }

    /**
     * Habilita o deshabilita todas las notificaciones
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationManager.setNotificationsEnabled(enabled)
                _notificationSettings.value = _notificationSettings.value.copy(
                    notificationsEnabled = enabled
                )
                Log.d(TAG, "🔔 Notificaciones ${if (enabled) "habilitadas" else "deshabilitadas"}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error configurando notificaciones", e)
            }
        }
    }

    /**
     * Configura los recordatorios de partidos
     */
    fun setMatchRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationManager.setMatchRemindersEnabled(enabled)
                _notificationSettings.value = _notificationSettings.value.copy(
                    matchRemindersEnabled = enabled
                )
                Log.d(TAG, "⏰ Recordatorios de partidos ${if (enabled) "habilitados" else "deshabilitados"}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error configurando recordatorios de partidos", e)
            }
        }
    }

    /**
     * Configura las notificaciones de resultados
     */
    fun setResultNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationManager.setResultNotificationsEnabled(enabled)
                _notificationSettings.value = _notificationSettings.value.copy(
                    resultNotificationsEnabled = enabled
                )
                Log.d(TAG, "📊 Notificaciones de resultados ${if (enabled) "habilitadas" else "deshabilitadas"}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error configurando notificaciones de resultados", e)
            }
        }
    }

    /**
     * Configura las noticias de equipos
     */
    fun setTeamNewsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationManager.setTeamNewsEnabled(enabled)
                _notificationSettings.value = _notificationSettings.value.copy(
                    teamNewsEnabled = enabled
                )
                Log.d(TAG, "🗞️ Noticias de equipos ${if (enabled) "habilitadas" else "deshabilitadas"}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error configurando noticias de equipos", e)
            }
        }
    }

    /**
     * Configura el tiempo de recordatorio
     */
    fun setReminderTime(minutes: Int) {
        viewModelScope.launch {
            try {
                notificationManager.setReminderTime(minutes)
                _notificationSettings.value = _notificationSettings.value.copy(
                    reminderTimeMinutes = minutes
                )
                Log.d(TAG, "⏱️ Tiempo de recordatorio configurado: $minutes minutos")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error configurando tiempo de recordatorio", e)
            }
        }
    }

    /**
     * Suscribe a notificaciones de un equipo específico
     */
    fun subscribeToTeam(teamCode: String) {
        viewModelScope.launch {
            try {
                notificationManager.subscribeToTeam(teamCode)
                Log.d(TAG, "✅ Suscrito a notificaciones del equipo: $teamCode")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error suscribiéndose al equipo: $teamCode", e)
            }
        }
    }

    /**
     * Desuscribe de notificaciones de un equipo específico
     */
    fun unsubscribeFromTeam(teamCode: String) {
        viewModelScope.launch {
            try {
                notificationManager.unsubscribeFromTeam(teamCode)
                Log.d(TAG, "✅ Desuscrito de notificaciones del equipo: $teamCode")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error desuscribiéndose del equipo: $teamCode", e)
            }
        }
    }

    /**
     * Obtiene el token FCM actual
     */
    fun getCurrentFCMToken(): String? {
        return try {
            val token = notificationManager.getCurrentFCMToken()
            Log.d(TAG, "🔑 Token FCM actual: ${token?.take(20)}...")
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo token FCM", e)
            null
        }
    }

    /**
     * Recarga la configuración de notificaciones
     */
    fun refreshSettings() {
        loadNotificationSettings()
    }
}
