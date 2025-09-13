package es.itram.basketmatch.notification.manager

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para gestionar notificaciones y tokens FCM
 */
@Singleton
class NotificationManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val TAG = "NotificationManager"
        private const val FCM_TOKEN_KEY = "fcm_token"
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
        private const val MATCH_REMINDERS_ENABLED_KEY = "match_reminders_enabled"
        private const val RESULT_NOTIFICATIONS_ENABLED_KEY = "result_notifications_enabled"
        private const val TEAM_NEWS_ENABLED_KEY = "team_news_enabled"
        private const val REMINDER_TIME_KEY = "reminder_time_minutes"
        
        // Valores por defecto
        private const val DEFAULT_REMINDER_TIME = 30 // 30 minutos antes del partido
    }

    /**
     * Inicializa las notificaciones y obtiene el token FCM
     */
    suspend fun initializeNotifications() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "🔑 Token FCM obtenido: $token")
            updateFCMToken(token)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo token FCM", e)
        }
    }

    /**
     * Actualiza el token FCM almacenado
     */
    fun updateFCMToken(token: String) {
        sharedPreferences.edit()
            .putString(FCM_TOKEN_KEY, token)
            .apply()
        
        Log.d(TAG, "💾 Token FCM almacenado")
        
        // Aquí podrías enviar el token a tu servidor
        // sendTokenToServer(token)
    }

    /**
     * Obtiene el token FCM actual
     */
    fun getCurrentFCMToken(): String? {
        return sharedPreferences.getString(FCM_TOKEN_KEY, null)
    }

    /**
     * Habilita o deshabilita todas las notificaciones
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled)
            .apply()
        
        if (enabled) {
            subscribeToTopics()
        } else {
            unsubscribeFromTopics()
        }
        
        Log.d(TAG, "🔔 Notificaciones ${if (enabled) "habilitadas" else "deshabilitadas"}")
    }

    /**
     * Verifica si las notificaciones están habilitadas
     */
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
    }

    /**
     * Configuración para recordatorios de partidos
     */
    fun setMatchRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(MATCH_REMINDERS_ENABLED_KEY, enabled)
            .apply()
    }

    fun areMatchRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean(MATCH_REMINDERS_ENABLED_KEY, true)
    }

    /**
     * Configuración para notificaciones de resultados
     */
    fun setResultNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(RESULT_NOTIFICATIONS_ENABLED_KEY, enabled)
            .apply()
    }

    fun areResultNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(RESULT_NOTIFICATIONS_ENABLED_KEY, true)
    }

    /**
     * Configuración para noticias de equipos
     */
    fun setTeamNewsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(TEAM_NEWS_ENABLED_KEY, enabled)
            .apply()
    }

    fun isTeamNewsEnabled(): Boolean {
        return sharedPreferences.getBoolean(TEAM_NEWS_ENABLED_KEY, false)
    }

    /**
     * Configuración del tiempo de recordatorio (en minutos)
     */
    fun setReminderTime(minutes: Int) {
        sharedPreferences.edit()
            .putInt(REMINDER_TIME_KEY, minutes)
            .apply()
    }

    fun getReminderTime(): Int {
        return sharedPreferences.getInt(REMINDER_TIME_KEY, DEFAULT_REMINDER_TIME)
    }

    /**
     * Suscribe a topics de FCM según configuraciones
     */
    private fun subscribeToTopics() {
        val messaging = FirebaseMessaging.getInstance()
        
        // Topic general de la EuroLeague
        messaging.subscribeToTopic("euroleague")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "✅ Suscrito a topic euroleague"
                } else {
                    "❌ Error suscribiéndose a topic euroleague"
                }
                Log.d(TAG, msg)
            }

        // Suscribirse a topics específicos según configuración
        if (areMatchRemindersEnabled()) {
            messaging.subscribeToTopic("match_reminders")
        }
        
        if (areResultNotificationsEnabled()) {
            messaging.subscribeToTopic("match_results")
        }
        
        if (isTeamNewsEnabled()) {
            messaging.subscribeToTopic("team_news")
        }
    }

    /**
     * Desuscribe de todos los topics
     */
    private fun unsubscribeFromTopics() {
        val messaging = FirebaseMessaging.getInstance()
        
        val topics = listOf("euroleague", "match_reminders", "match_results", "team_news")
        
        topics.forEach { topic ->
            messaging.unsubscribeFromTopic(topic)
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) {
                        "✅ Desuscrito de topic $topic"
                    } else {
                        "❌ Error desuscribiéndose de topic $topic"
                    }
                    Log.d(TAG, msg)
                }
        }
    }

    /**
     * Suscribe a notificaciones de un equipo específico
     */
    fun subscribeToTeam(teamCode: String) {
        if (!areNotificationsEnabled()) return
        
        FirebaseMessaging.getInstance()
            .subscribeToTopic("team_$teamCode")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "✅ Suscrito a notifications del equipo $teamCode"
                } else {
                    "❌ Error suscribiéndose al equipo $teamCode"
                }
                Log.d(TAG, msg)
            }
    }

    /**
     * Desuscribe de notificaciones de un equipo específico
     */
    fun unsubscribeFromTeam(teamCode: String) {
        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic("team_$teamCode")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "✅ Desuscrito de notifications del equipo $teamCode"
                } else {
                    "❌ Error desuscribiéndose del equipo $teamCode"
                }
                Log.d(TAG, msg)
            }
    }

    /**
     * Obtiene todas las configuraciones de notificaciones
     */
    fun getNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            notificationsEnabled = areNotificationsEnabled(),
            matchRemindersEnabled = areMatchRemindersEnabled(),
            resultNotificationsEnabled = areResultNotificationsEnabled(),
            teamNewsEnabled = isTeamNewsEnabled(),
            reminderTimeMinutes = getReminderTime()
        )
    }
}

/**
 * Data class para configuraciones de notificaciones
 */
data class NotificationSettings(
    val notificationsEnabled: Boolean,
    val matchRemindersEnabled: Boolean,
    val resultNotificationsEnabled: Boolean,
    val teamNewsEnabled: Boolean,
    val reminderTimeMinutes: Int
)
