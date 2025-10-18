package es.itram.basketmatch

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import es.itram.basketmatch.notification.manager.NotificationManager
import es.itram.basketmatch.domain.service.DataSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🏀 Aplicación EuroLeague - Temporada 2025-2026 (E2025)
 *
 * Al arrancar:
 * ✅ Inicializa notificaciones
 * ✅ Descarga equipos de E2025 (temporada 2025-2026) si no existen
 * ✅ Descarga las 38 jornadas de E2025 si no existen
 */
@HiltAndroidApp
class EuroLeagueApplication : Application() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var dataSyncService: DataSyncService

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "EuroLeagueApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Iniciando aplicación EuroLeague temporada 2025-2026 (E2025)...")

        // Inicializar notificaciones push
        initializeNotifications()

        // Inicializar datos de la aplicación
        initializeAppData()
    }

    /**
     * Inicializa el sistema de notificaciones push
     */
    private fun initializeNotifications() {
        applicationScope.launch {
            try {
                notificationManager.initializeNotifications()
                Log.d(TAG, "✅ Sistema de notificaciones inicializado")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inicializando notificaciones", e)
            }
        }
    }

    /**
     * Inicializa los datos de la aplicación al arrancar
     * - Descarga equipos de E2025 si no existen
     * - Descarga partidos de E2025 si no existen
     */
    private fun initializeAppData() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Verificando datos locales de E2025...")

                dataSyncService.initializeAppData()

                Log.d(TAG, "✅ Datos de E2025 listos")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inicializando datos: ${e.message}", e)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "🧹 EuroLeague terminada")
    }
}
