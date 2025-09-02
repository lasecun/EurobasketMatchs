package es.itram.basketmatch.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

/**
 * Servicio para actualizar automáticamente el widget cada hora
 */
@AndroidEntryPoint
class WidgetUpdateService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "WidgetUpdateService"
        private const val UPDATE_INTERVAL_MS = 60 * 60 * 1000L // 1 hora
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Servicio de widget iniciado")
        startPeriodicUpdates()
    }

    private fun startPeriodicUpdates() {
        serviceScope.launch {
            while (isActive) {
                try {
                    Log.d(TAG, "⏰ Actualizando widgets automáticamente")
                    updateAllWidgets()

                    // Esperar 1 hora antes de la próxima actualización
                    delay(UPDATE_INTERVAL_MS)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error en actualización automática", e)
                    // En caso de error, esperar un poco menos antes de reintentar
                    delay(15 * 60 * 1000L) // 15 minutos
                }
            }
        }
    }

    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, TodayMatchesWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            Log.d(TAG, "🔄 Actualizando ${appWidgetIds.size} widgets")

            val updateIntent = Intent(this, TodayMatchesWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }

            sendBroadcast(updateIntent)
        } else {
            Log.d(TAG, "📱 No hay widgets para actualizar")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "🛑 Servicio de widget detenido")
    }
}
