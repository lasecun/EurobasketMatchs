package es.itram.basketmatch.analytics.tracking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.analytics.events.AnalyticsEvent
import javax.inject.Inject

/**
 * 📱 Screen Tracker - Tracking automático de pantallas en Compose
 *
 * Facilita el tracking de screen views en aplicaciones Jetpack Compose:
 * - 🎯 Tracking automático al entrar/salir de pantallas
 * - ⏱️ Medición de tiempo en pantalla para engagement
 * - 🔄 Lifecycle-aware para precisión
 * - 📊 Integración seamless con AnalyticsManager
 */
@HiltViewModel
class ScreenTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val screenStartTimes = mutableMapOf<String, Long>()

    /**
     * Composable que trackea automáticamente una pantalla
     *
     * Uso:
     * ```kotlin
     * @Composable
     * fun TeamDetailScreen(teamCode: String) {
     *     val screenTracker = hiltViewModel<ScreenTracker>()
     *
     *     screenTracker.TrackScreen(
     *         screenName = AnalyticsManager.SCREEN_TEAM_DETAIL,
     *         screenClass = "TeamDetailScreen"
     *     ) {
     *         // Contenido de la pantalla
     *         TeamDetailContent(teamCode = teamCode)
     *     }
     * }
     * ```
     */
    @Composable
    fun TrackScreen(
        screenName: String,
        screenClass: String? = null,
        content: @Composable () -> Unit
    ) {
        DisposableEffect(screenName) {
            // Registrar entrada a la pantalla
            onScreenEnter(screenName, screenClass)

            onDispose {
                // Registrar salida de la pantalla
                onScreenExit(screenName)
            }
        }
        
        // Renderizar el contenido
        content()
    }

    /**
     * Método manual para trackear entrada a pantalla
     * Útil para casos donde no se puede usar el Composable automático
     */
    fun trackScreenEnter(screenName: String, screenClass: String? = null) {
        onScreenEnter(screenName, screenClass)
    }

    /**
     * Método manual para trackear salida de pantalla
     */
    fun trackScreenExit(screenName: String) {
        onScreenExit(screenName)
    }

    private fun onScreenEnter(screenName: String, screenClass: String? = null) {
        // Registrar tiempo de inicio
        screenStartTimes[screenName] = System.currentTimeMillis()

        // Enviar evento de screen view
        analyticsManager.trackScreenView(screenName, screenClass)
        
        // También trackear con el sistema de eventos tipados
        analyticsManager.firebaseAnalytics.logEvent(
            AnalyticsEvent.ScreenViewEvent(screenName, screenClass).eventName,
            AnalyticsEvent.ScreenViewEvent(screenName, screenClass).toBundle()
        )
    }

    private fun onScreenExit(screenName: String) {
        // Calcular tiempo en pantalla
        screenStartTimes[screenName]?.let { startTime ->
            val timeSpent = System.currentTimeMillis() - startTime

            // Trackear tiempo en pantalla (solo si es significativo > 1 segundo)
            if (timeSpent > 1000) {
                trackScreenEngagement(screenName, timeSpent)
            }

            // Limpiar el tiempo de inicio
            screenStartTimes.remove(screenName)
        }
    }

    private fun trackScreenEngagement(screenName: String, timeSpentMs: Long) {
        val bundle = android.os.Bundle().apply {
            putString("screen_name", screenName)
            putLong("time_spent_ms", timeSpentMs)
            putLong("time_spent_seconds", timeSpentMs / 1000)
        }

        analyticsManager.firebaseAnalytics.logEvent("screen_engagement", bundle)
    }

    /**
     * Métodos de conveniencia para pantallas específicas
     */
    @Composable
    fun TrackHomeScreen(content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_HOME,
            screenClass = "HomeScreen",
            content = content
        )
    }

    @Composable
    fun TrackMatchDetailScreen(matchId: String, content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_MATCH_DETAIL,
            screenClass = "MatchDetailScreen",
            content = content
        )
    }

    @Composable
    fun TrackTeamDetailScreen(teamCode: String, content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_TEAM_DETAIL,
            screenClass = "TeamDetailScreen",
            content = content
        )
    }

    @Composable
    fun TrackPlayerDetailScreen(playerCode: String, content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_PLAYER_DETAIL,
            screenClass = "PlayerDetailScreen",
            content = content
        )
    }

    @Composable
    fun TrackMatchesScreen(content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_MATCHES,
            screenClass = "MatchesScreen",
            content = content
        )
    }

    @Composable
    fun TrackTeamsScreen(content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_TEAMS,
            screenClass = "TeamsScreen",
            content = content
        )
    }

    @Composable
    fun TrackStandingsScreen(content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_STANDINGS,
            screenClass = "StandingsScreen",
            content = content
        )
    }

    @Composable
    fun TrackSettingsScreen(content: @Composable () -> Unit) {
        TrackScreen(
            screenName = AnalyticsManager.SCREEN_SETTINGS,
            screenClass = "SettingsScreen",
            content = content
        )
    }
}
