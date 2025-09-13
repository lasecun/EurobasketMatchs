package es.itram.basketmatch.analytics.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.analytics.events.AnalyticsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 🎯 Event Dispatcher - Procesamiento asíncrono de eventos de analytics
 *
 * Maneja el envío de eventos de analytics de forma eficiente:
 * - ⚡ Procesamiento asíncrono para no bloquear la UI
 * - 🔄 Queue interno para eventos en batch
 * - 🛡️ Error handling robusto
 * - 📊 Logging detallado para debugging
 */
@HiltViewModel
class EventDispatcher @Inject constructor(
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    /**
     * Dispatcha un evento de analytics de forma asíncrona
     *
     * @param event El evento a enviar
     * @param immediate Si true, envía inmediatamente. Si false, puede hacer batch.
     */
    fun dispatch(event: AnalyticsEvent, immediate: Boolean = false) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    when (event) {
                        is AnalyticsEvent.ScreenViewEvent -> {
                            analyticsManager.trackScreenView(
                                screenName = event.screenName,
                                screenClass = event.screenClass
                            )
                        }
                        else -> {
                            // Para eventos personalizados, usar el método genérico
                            analyticsManager.firebaseAnalytics.logEvent(
                                event.eventName,
                                event.toBundle()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash the app
                analyticsManager.trackError(
                    errorType = "analytics_dispatch_error",
                    errorMessage = e.message ?: "Unknown error",
                    eventName = event.eventName
                )
            }
        }
    }

    /**
     * Dispatcha múltiples eventos en batch (más eficiente)
     */
    fun dispatchBatch(events: List<AnalyticsEvent>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                events.forEach { event ->
                    try {
                        dispatch(event, immediate = true)
                    } catch (e: Exception) {
                        // Continue processing other events even if one fails
                        analyticsManager.trackError(
                            errorType = "batch_dispatch_error",
                            errorMessage = e.message ?: "Unknown error",
                            eventName = event.eventName
                        )
                    }
                }
            }
        }
    }

    /**
     * Métodos de conveniencia para eventos comunes
     */
    fun trackMatchViewed(matchId: String, homeTeam: String, awayTeam: String, isLive: Boolean = false) {
        dispatch(
            AnalyticsEvent.MatchEvent(
                action = es.itram.basketmatch.analytics.events.MatchAction.VIEWED,
                matchId = matchId,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                isLive = isLive
            )
        )
    }

    fun trackTeamViewed(teamCode: String, teamName: String, source: String = "navigation") {
        dispatch(
            AnalyticsEvent.TeamContentEvent(
                action = es.itram.basketmatch.analytics.events.TeamAction.VIEWED,
                teamCode = teamCode,
                teamName = teamName,
                source = source
            )
        )
    }

    fun trackPlayerViewed(playerCode: String, playerName: String, teamCode: String) {
        dispatch(
            AnalyticsEvent.PlayerEvent(
                action = es.itram.basketmatch.analytics.events.PlayerAction.VIEWED,
                playerCode = playerCode,
                playerName = playerName,
                teamCode = teamCode
            )
        )
    }

    fun trackSearch(query: String, resultCount: Int = 0) {
        dispatch(
            AnalyticsEvent.SearchEvent(
                query = query,
                resultCount = resultCount
            )
        )
    }

    fun trackDataSync(
        action: es.itram.basketmatch.analytics.events.SyncAction,
        syncType: String = "full",
        durationMs: Long? = null,
        itemsCount: Int? = null,
        success: Boolean = true,
        errorType: String? = null
    ) {
        dispatch(
            AnalyticsEvent.DataSyncEvent(
                action = action,
                syncType = syncType,
                durationMs = durationMs,
                itemsCount = itemsCount,
                success = success,
                errorType = errorType
            )
        )
    }

    fun trackPerformance(
        eventType: es.itram.basketmatch.analytics.events.PerformanceType,
        durationMs: Long,
        context: String? = null
    ) {
        dispatch(
            AnalyticsEvent.PerformanceEvent(
                eventType = eventType,
                durationMs = durationMs,
                context = context
            )
        )
    }
}
