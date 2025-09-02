package es.itram.basketmatch

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import es.itram.basketmatch.notification.manager.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class EuroLeagueApplication : Application() {

    @Inject
    lateinit var notificationManager: NotificationManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d("EuroLeagueApp", "🚀 Iniciando aplicación EuroLeague...")
        
        // Inicializar notificaciones push
        initializeNotifications()
    }

    /**
     * Inicializa el sistema de notificaciones push
     */
    private fun initializeNotifications() {
        applicationScope.launch {
            try {
                notificationManager.initializeNotifications()
                Log.d("EuroLeagueApp", "✅ Sistema de notificaciones inicializado")
            } catch (e: Exception) {
                Log.e("EuroLeagueApp", "❌ Error inicializando notificaciones", e)
            }
        }
    }
}
