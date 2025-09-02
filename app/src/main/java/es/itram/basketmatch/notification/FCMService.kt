package es.itram.basketmatch.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import es.itram.basketmatch.MainActivity
import es.itram.basketmatch.R
import es.itram.basketmatch.notification.manager.NotificationManager as AppNotificationManager
import javax.inject.Inject

/**
 * Servicio de Firebase Cloud Messaging para manejar notificaciones push
 */
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: AppNotificationManager

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "euroleague_matches"
        private const val CHANNEL_NAME = "Partidos EuroLeague"
        private const val CHANNEL_DESCRIPTION = "Notificaciones sobre partidos de la EuroLeague"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Llamado cuando se recibe un nuevo token FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 Nuevo token FCM recibido: $token")
        
        // Almacenar el token y enviarlo al servidor si es necesario
        notificationManager.updateFCMToken(token)
    }

    /**
     * Llamado cuando se recibe una notificación push
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📱 Mensaje recibido de: ${remoteMessage.from}")

        // Procesar datos del mensaje
        remoteMessage.data.let { data ->
            Log.d(TAG, "📊 Datos del mensaje: $data")
            handleDataPayload(data)
        }

        // Procesar notificación
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "🔔 Notificación recibida: ${notification.title}")
            showNotification(
                title = notification.title ?: "EuroLeague",
                body = notification.body ?: "Nueva actualización",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Maneja los datos personalizados de la notificación
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val notificationType = data["type"] ?: return
        
        when (notificationType) {
            "match_reminder" -> {
                val matchId = data["match_id"]
                val teamName = data["team_name"]
                val matchTime = data["match_time"]
                
                showMatchReminderNotification(matchId, teamName, matchTime)
            }
            "match_result" -> {
                val matchId = data["match_id"]
                val result = data["result"]
                
                showMatchResultNotification(matchId, result)
            }
            "team_news" -> {
                val teamName = data["team_name"]
                val news = data["news"]
                
                showTeamNewsNotification(teamName, news)
            }
        }
    }

    /**
     * Muestra una notificación general
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>? = null) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "⚠️ Sin permisos de notificación")
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Añadir datos extra si es necesario
            data?.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_basketball)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Muestra notificación de recordatorio de partido
     */
    private fun showMatchReminderNotification(matchId: String?, teamName: String?, matchTime: String?) {
        val title = "🏀 Partido próximo"
        val body = if (teamName != null && matchTime != null) {
            "$teamName juega en $matchTime"
        } else {
            "Tienes un partido favorito próximo"
        }
        
        showNotification(title, body, mapOf(
            "type" to "match_reminder",
            "match_id" to (matchId ?: ""),
            "action" to "open_match"
        ))
    }

    /**
     * Muestra notificación de resultado de partido
     */
    private fun showMatchResultNotification(matchId: String?, result: String?) {
        val title = "📊 Resultado disponible"
        val body = result ?: "El partido ha terminado"
        
        showNotification(title, body, mapOf(
            "type" to "match_result",
            "match_id" to (matchId ?: ""),
            "action" to "open_match"
        ))
    }

    /**
     * Muestra notificación de noticias del equipo
     */
    private fun showTeamNewsNotification(teamName: String?, news: String?) {
        val title = "🗞️ ${teamName ?: "Noticias"}"
        val body = news ?: "Hay noticias nuevas disponibles"
        
        showNotification(title, body, mapOf(
            "type" to "team_news",
            "team_name" to (teamName ?: ""),
            "action" to "open_team"
        ))
    }

    /**
     * Crea el canal de notificaciones para Android 8+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "✅ Canal de notificación creado: $CHANNEL_NAME")
        }
    }

    /**
     * Verifica si la app tiene permisos de notificación
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }
}
