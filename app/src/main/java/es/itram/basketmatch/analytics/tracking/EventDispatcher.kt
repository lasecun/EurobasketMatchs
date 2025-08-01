package es.itram.basketmatch.analytics.tracking

import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.analytics.events.AnalyticsEvent
import es.itram.basketmatch.analytics.events.DataAction
import es.itram.basketmatch.analytics.events.ErrorSeverity
import es.itram.basketmatch.analytics.events.FavoriteAction
import es.itram.basketmatch.analytics.events.MatchAction
import es.itram.basketmatch.analytics.events.PerformanceType
import es.itram.basketmatch.analytics.events.PlayerAction
import es.itram.basketmatch.analytics.events.TeamAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🚀 Event Dispatcher - Sistema centralizado para dispatch de eventos de analytics
 * 
 * Maneja el envío de eventos de manera:
 * - ⚡ Asíncrona para no bloquear UI
 * - 🔒 Thread-safe 
 * - 📊 Con logging automático para debugging
 * - 🎯 Con transformación de eventos a formato Firebase
 * 
 * Proporciona una API limpia y type-safe para tracking de eventos.
 */
@Singleton
class EventDispatcher @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 🎯 Dispatch principal de eventos
     */
    fun dispatch(event: AnalyticsEvent) {
        scope.launch {
            try {
                when (event) {
                    is AnalyticsEvent.ScreenViewed -> handleScreenEvent(event)
                    is AnalyticsEvent.MatchContentEvent -> handleMatchEvent(event)
                    is AnalyticsEvent.TeamContentEvent -> handleTeamEvent(event)
                    is AnalyticsEvent.PlayerContentEvent -> handlePlayerEvent(event)
                    is AnalyticsEvent.DataSyncEvent -> handleDataEvent(event)
                    is AnalyticsEvent.SearchEvent -> handleSearchEvent(event)
                    is AnalyticsEvent.FilterEvent -> handleFilterEvent(event)
                    is AnalyticsEvent.FavoriteEvent -> handleFavoriteEvent(event)
                    is AnalyticsEvent.ShareEvent -> handleShareEvent(event)
                    is AnalyticsEvent.PerformanceEvent -> handlePerformanceEvent(event)
                    is AnalyticsEvent.UserInteractionEvent -> handleUserInteractionEvent(event)
                    is AnalyticsEvent.ErrorEvent -> handleErrorEvent(event)
                }
            } catch (e: Exception) {
                // Evitar loops infinitos en caso de error en analytics
                analyticsManager.recordException(e, mapOf("context" to "EventDispatcher"))
            }
        }
    }
    
    private fun handleScreenEvent(event: AnalyticsEvent.ScreenViewed) {
        analyticsManager.trackScreenView(event.screenName, event.screenClass)
    }
    
    private fun handleMatchEvent(event: AnalyticsEvent.MatchContentEvent) {
        when (event.action) {
            MatchAction.VIEWED -> {
                analyticsManager.trackMatchViewed(
                    matchId = event.matchId,
                    homeTeam = event.homeTeam,
                    awayTeam = event.awayTeam,
                    isLive = event.isLive
                )
            }
            MatchAction.LIVE_SCORE_CHECKED -> {
                analyticsManager.firebaseAnalytics.logEvent(AnalyticsManager.EVENT_LIVE_SCORE_VIEWED) {
                    putString(AnalyticsManager.PARAM_MATCH_ID, event.matchId)
                    putString("home_team", event.homeTeam)
                    putString("away_team", event.awayTeam)
                    putString("match_status", event.matchStatus)
                    putString(AnalyticsManager.PARAM_SOURCE, event.source)
                }
            }
            MatchAction.FAVORITED -> {
                analyticsManager.trackFavoriteAdded("match", event.matchId)
            }
            MatchAction.SHARED -> {
                analyticsManager.trackContentShared("match", event.matchId, "native_share")
            }
            else -> {
                // Eventos genéricos de match
                analyticsManager.firebaseAnalytics.logEvent("match_${event.action.name.lowercase()}") {
                    putString(AnalyticsManager.PARAM_MATCH_ID, event.matchId)
                    putString(AnalyticsManager.PARAM_SOURCE, event.source)
                }
            }
        }
    }
    
    private fun handleTeamEvent(event: AnalyticsEvent.TeamContentEvent) {
        when (event.action) {
            TeamAction.VIEWED -> {
                analyticsManager.trackTeamViewed(
                    teamCode = event.teamCode,
                    teamName = event.teamName,
                    source = event.source
                )
            }
            TeamAction.ROSTER_ACCESSED -> {
                analyticsManager.firebaseAnalytics.logEvent("team_roster_accessed") {
                    putString(AnalyticsManager.PARAM_TEAM_CODE, event.teamCode)
                    putString(AnalyticsManager.PARAM_TEAM_NAME, event.teamName)
                    putString(AnalyticsManager.PARAM_SOURCE, event.source)
                }
            }
            TeamAction.FAVORITED -> {
                analyticsManager.trackFavoriteAdded("team", event.teamCode)
                analyticsManager.setFavoriteTeam(event.teamCode, event.teamName)
            }
            TeamAction.SHARED -> {
                analyticsManager.trackContentShared("team", event.teamCode, "native_share")
            }
            else -> {
                analyticsManager.firebaseAnalytics.logEvent("team_${event.action.name.lowercase()}") {
                    putString(AnalyticsManager.PARAM_TEAM_CODE, event.teamCode)
                    putString(AnalyticsManager.PARAM_SOURCE, event.source)
                }
            }
        }
    }
    
    private fun handlePlayerEvent(event: AnalyticsEvent.PlayerContentEvent) {
        when (event.action) {
            PlayerAction.VIEWED -> {
                analyticsManager.trackPlayerViewed(
                    playerCode = event.playerCode,
                    playerName = event.playerName,
                    teamCode = event.teamCode
                )
            }
            PlayerAction.STATS_VIEWED -> {
                analyticsManager.firebaseAnalytics.logEvent(AnalyticsManager.EVENT_PLAYER_STATS_VIEWED) {
                    putString(AnalyticsManager.PARAM_PLAYER_CODE, event.playerCode)
                    putString(AnalyticsManager.PARAM_PLAYER_NAME, event.playerName)
                    putString(AnalyticsManager.PARAM_TEAM_CODE, event.teamCode)
                    event.position?.let { putString("position", it) }
                }
            }
            PlayerAction.SHARED -> {
                analyticsManager.trackContentShared("player", event.playerCode, "native_share")
            }
            else -> {
                analyticsManager.firebaseAnalytics.logEvent("player_${event.action.name.lowercase()}") {
                    putString(AnalyticsManager.PARAM_PLAYER_CODE, event.playerCode)
                    putString(AnalyticsManager.PARAM_SOURCE, event.source)
                }
            }
        }
    }
    
    private fun handleDataEvent(event: AnalyticsEvent.DataSyncEvent) {
        when (event.action) {
            DataAction.SYNC_STARTED -> {
                analyticsManager.trackDataSyncStarted(event.syncType)
            }
            DataAction.SYNC_COMPLETED -> {
                if (event.durationMs != null && event.itemsCount != null) {
                    analyticsManager.trackDataSyncCompleted(
                        syncType = event.syncType,
                        durationMs = event.durationMs,
                        itemsCount = event.itemsCount
                    )
                }
            }
            DataAction.SYNC_FAILED -> {
                analyticsManager.trackDataSyncFailed(
                    syncType = event.syncType,
                    errorMessage = event.errorMessage ?: "Unknown error"
                )
            }
            DataAction.OFFLINE_ACCESS -> {
                analyticsManager.firebaseAnalytics.logEvent(AnalyticsManager.EVENT_OFFLINE_MODE_ACCESSED) {
                    putString("content_type", event.syncType)
                }
            }
            else -> {
                analyticsManager.firebaseAnalytics.logEvent("data_${event.action.name.lowercase()}") {
                    putString("sync_type", event.syncType)
                }
            }
        }
    }
    
    private fun handleSearchEvent(event: AnalyticsEvent.SearchEvent) {
        analyticsManager.trackSearchPerformed(event.query, event.resultCount)
    }
    
    private fun handleFilterEvent(event: AnalyticsEvent.FilterEvent) {
        analyticsManager.trackFilterApplied(event.filterType, event.filterValue)
    }
    
    private fun handleFavoriteEvent(event: AnalyticsEvent.FavoriteEvent) {
        when (event.action) {
            FavoriteAction.ADDED -> {
                analyticsManager.trackFavoriteAdded(event.contentType, event.contentId)
            }
            FavoriteAction.REMOVED -> {
                analyticsManager.firebaseAnalytics.logEvent("favorite_removed") {
                    putString(AnalyticsManager.PARAM_CONTENT_TYPE, event.contentType)
                    putString("content_id", event.contentId)
                    putString("content_name", event.contentName)
                }
            }
            FavoriteAction.VIEWED_LIST -> {
                analyticsManager.firebaseAnalytics.logEvent("favorites_list_viewed") {
                    putString(AnalyticsManager.PARAM_CONTENT_TYPE, event.contentType)
                }
            }
        }
    }
    
    private fun handleShareEvent(event: AnalyticsEvent.ShareEvent) {
        analyticsManager.trackContentShared(
            contentType = event.contentType,
            contentId = event.contentId,
            shareMethod = event.shareMethod
        )
    }
    
    private fun handlePerformanceEvent(event: AnalyticsEvent.PerformanceEvent) {
        when (event.type) {
            PerformanceType.APP_STARTUP -> {
                analyticsManager.trackAppStartupTime(event.durationMs)
            }
            PerformanceType.IMAGE_LOAD -> {
                val imageType = event.details["imageType"] as? String ?: "unknown"
                analyticsManager.trackImageLoadTime(imageType, event.durationMs, event.success)
            }
            PerformanceType.API_CALL -> {
                val endpoint = event.details["endpoint"] as? String ?: "unknown"
                analyticsManager.trackApiResponseTime(endpoint, event.durationMs, event.success)
            }
            else -> {
                analyticsManager.firebaseAnalytics.logEvent("performance_${event.type.name.lowercase()}") {
                    putLong(AnalyticsManager.PARAM_LOAD_TIME_MS, event.durationMs)
                    putBoolean(AnalyticsManager.PARAM_SUCCESS, event.success)
                    event.details.forEach { (key, value) ->
                        when (value) {
                            is String -> putString(key, value)
                            is Long -> putLong(key, value)
                            is Int -> putInt(key, value)
                            is Boolean -> putBoolean(key, value)
                        }
                    }
                }
            }
        }
    }
    
    private fun handleUserInteractionEvent(event: AnalyticsEvent.UserInteractionEvent) {
        analyticsManager.firebaseAnalytics.logEvent("user_interaction") {
            putString("action", event.action)
            putString("element", event.element)
            putString(AnalyticsManager.PARAM_SCREEN_NAME, event.screen)
            event.value?.let { putString("value", it) }
        }
    }
    
    private fun handleErrorEvent(event: AnalyticsEvent.ErrorEvent) {
        analyticsManager.firebaseAnalytics.logEvent("app_error") {
            putString(AnalyticsManager.PARAM_ERROR_TYPE, event.errorType)
            putString("error_message", event.errorMessage)
            putString(AnalyticsManager.PARAM_SCREEN_NAME, event.screen)
            putString("severity", event.severity.name)
            event.action?.let { putString("action", it) }
        }
        
        // Para errores críticos, también enviar a Crashlytics
        if (event.severity == ErrorSeverity.CRITICAL || event.severity == ErrorSeverity.HIGH) {
            val exception = Exception("${event.errorType}: ${event.errorMessage}")
            analyticsManager.recordException(
                exception,
                mapOf(
                    "screen" to event.screen,
                    "severity" to event.severity.name,
                    "action" to (event.action ?: "unknown")
                )
            )
        }
    }
}

// Extensions para facilitar el logging con Bundle
private inline fun com.google.firebase.analytics.FirebaseAnalytics.logEvent(
    name: String,
    block: android.os.Bundle.() -> Unit
) {
    val bundle = android.os.Bundle().apply(block)
    logEvent(name, bundle)
}
