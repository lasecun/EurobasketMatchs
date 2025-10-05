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
 * 🏀 Aplicación principal de EuroLeague
 *
 * Funcionalidades al arrancar:
 * ✅ Inicializar Hilt
 * ✅ Inicializar sistema de notificaciones
 * ✅ Verificar datos locales (equipos y calendario)
 * ✅ Obtener de API oficial si no existen datos
 * ✅ Enriquecer partidos en segundo plano
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
        Log.d(TAG, "🚀 Iniciando aplicación EuroLeague...")

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
     */
    private fun initializeAppData() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Inicializando datos de la aplicación...")

                // El DataSyncService se encarga de:
                // 1. Verificar equipos en base de datos local
                // 2. Obtener equipos de API oficial si no existen
                // 3. Verificar calendario en base de datos local
                // 4. Obtener calendario de API oficial si no existe
                // 5. Enriquecer partidos con estadísticas en segundo plano
                dataSyncService.initializeAppData()

                Log.d(TAG, "✅ Inicialización de datos completada")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inicializando datos: ${e.message}", e)
                // La app seguirá funcionando con datos de emergencia si están disponibles
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        // Limpiar recursos
        dataSyncService.cleanup()
        Log.d(TAG, "🧹 EuroLeague terminada - recursos liberados")
    }
}
