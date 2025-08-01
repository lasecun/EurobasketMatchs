package es.itram.basketmatch.analytics.tracking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.itram.basketmatch.analytics.AnalyticsManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📱 Screen Tracker - Sistema automático de tracking de pantallas
 * 
 * Proporciona tracking automático de:
 * - Screen views y tiempo de permanencia
 * - User journey y navigation patterns
 * - Session analytics y engagement metrics
 * - Performance de pantallas individuales
 * 
 * Optimizado para SEO móvil y analytics de UX.
 */
@Singleton
class ScreenTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    
    private var currentScreen: String? = null
    private var screenStartTime: Long = 0L
    private val screenSessions = mutableMapOf<String, ScreenSession>()
    
    /**
     * 📊 Composable para tracking automático de screen views
     * Uso: TrackScreen("team_detail") { /* contenido de la pantalla */ }
     */
    @Composable
    fun TrackScreen(
        screenName: String,
        screenClass: String? = null,
        lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
        content: @Composable () -> Unit
    ) {
        val currentOnStart by rememberUpdatedState(newValue = {
            onScreenStart(screenName, screenClass)
        })
        
        val currentOnStop by rememberUpdatedState(newValue = {
            onScreenStop(screenName)
        })
        
        DisposableEffect(screenName, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> currentOnStart()
                    Lifecycle.Event.ON_STOP -> currentOnStop()
                    else -> {}
                }
            }
            
            lifecycleOwner.lifecycle.addObserver(observer)
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                onScreenStop(screenName)
            }
        }
        
        content()
    }
    
    /**
     * 🎯 Manual screen tracking para casos específicos
     */
    fun trackScreenView(
        screenName: String,
        screenClass: String? = null,
        previousScreen: String? = currentScreen
    ) {
        analyticsManager.trackScreenView(screenName, screenClass)
        
        // Trackear tiempo en pantalla anterior si existe
        if (previousScreen != null && screenStartTime > 0) {
            val timeSpent = System.currentTimeMillis() - screenStartTime
            trackScreenTimeSpent(previousScreen, timeSpent)
        }
        
        currentScreen = screenName
        screenStartTime = System.currentTimeMillis()
    }
    
    private fun onScreenStart(screenName: String, screenClass: String?) {
        val previousScreen = currentScreen
        currentScreen = screenName
        screenStartTime = System.currentTimeMillis()
        
        // Inicializar o actualizar sesión de pantalla
        val session = screenSessions.getOrPut(screenName) { ScreenSession(screenName) }
        session.startSession()
        
        analyticsManager.trackScreenView(screenName, screenClass)
        
        // Log para debugging (solo en desarrollo)
        analyticsManager.logMessage("Screen started: $screenName")
    }
    
    private fun onScreenStop(screenName: String) {
        if (currentScreen == screenName && screenStartTime > 0) {
            val timeSpent = System.currentTimeMillis() - screenStartTime
            trackScreenTimeSpent(screenName, timeSpent)
            
            // Actualizar sesión de pantalla
            screenSessions[screenName]?.endSession(timeSpent)
        }
    }
    
    private fun trackScreenTimeSpent(screenName: String, timeSpentMs: Long) {
        if (timeSpentMs > 0) {
            analyticsManager.firebaseAnalytics.logEvent("screen_time_spent") {
                param("screen_name", screenName)
                param("time_spent_ms", timeSpentMs)
                param("time_spent_seconds", timeSpentMs / 1000)
            }
        }
    }
    
    /**
     * 📊 Obtener métricas de sesiones de pantalla
     */
    fun getScreenMetrics(): Map<String, ScreenMetrics> {
        return screenSessions.mapValues { (_, session) ->
            session.getMetrics()
        }
    }
    
    /**
     * 🔄 Reset tracking (útil para testing)
     */
    fun reset() {
        currentScreen = null
        screenStartTime = 0L
        screenSessions.clear()
    }
}

/**
 * 📱 Sesión de pantalla individual
 */
private class ScreenSession(val screenName: String) {
    private var sessionCount = 0
    private var totalTimeMs = 0L
    private var lastStartTime = 0L
    private var maxTimeMs = 0L
    private var minTimeMs = Long.MAX_VALUE
    
    fun startSession() {
        sessionCount++
        lastStartTime = System.currentTimeMillis()
    }
    
    fun endSession(durationMs: Long) {
        if (durationMs > 0) {
            totalTimeMs += durationMs
            maxTimeMs = maxOf(maxTimeMs, durationMs)
            minTimeMs = minOf(minTimeMs, durationMs)
        }
    }
    
    fun getMetrics(): ScreenMetrics {
        return ScreenMetrics(
            screenName = screenName,
            sessionCount = sessionCount,
            totalTimeMs = totalTimeMs,
            averageTimeMs = if (sessionCount > 0) totalTimeMs / sessionCount else 0L,
            maxTimeMs = if (maxTimeMs > 0) maxTimeMs else 0L,
            minTimeMs = if (minTimeMs != Long.MAX_VALUE) minTimeMs else 0L
        )
    }
}

/**
 * 📊 Métricas de pantalla
 */
data class ScreenMetrics(
    val screenName: String,
    val sessionCount: Int,
    val totalTimeMs: Long,
    val averageTimeMs: Long,
    val maxTimeMs: Long,
    val minTimeMs: Long
)

/**
 * 🎯 Extension function para logging de eventos con parámetros
 */
private inline fun com.google.firebase.analytics.FirebaseAnalytics.logEvent(
    name: String,
    block: android.os.Bundle.() -> Unit
) {
    val bundle = android.os.Bundle().apply(block)
    logEvent(name, bundle)
}

private fun android.os.Bundle.param(key: String, value: String) {
    putString(key, value)
}

private fun android.os.Bundle.param(key: String, value: Long) {
    putLong(key, value)
}

private fun android.os.Bundle.param(key: String, value: Int) {
    putInt(key, value)
}

private fun android.os.Bundle.param(key: String, value: Boolean) {
    putBoolean(key, value)
}
