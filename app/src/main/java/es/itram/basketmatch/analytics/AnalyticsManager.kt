package es.itram.basketmatch.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📊 Analytics Manager - Sistema completo de tracking para EuroLeague App
 * 
 * Este manager implementa las mejores prácticas de SEO y analytics móvil:
 * - 🎯 Event tracking estratégico para insights de usuario
 * - 📱 Screen view tracking para user journey analysis
 * - 🏀 Eventos específicos del dominio (basketball/sports)
 * - 🔍 Tracking de engagement y retención
 * - ⚡ Performance tracking para UX optimization
 * 
 * Basado en Google Analytics 4 con focus en:
 * - User engagement metrics
 * - Content performance
 * - Feature usage analytics
 * - Error tracking y app stability
 */
@Singleton
class AnalyticsManager @Inject constructor(
    val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {
    
    companion object {
        // 📊 SCREEN NAMES - Consistentes con navegación
        const val SCREEN_HOME = "home"
        const val SCREEN_MATCHES = "matches" 
        const val SCREEN_MATCH_DETAIL = "match_detail"
        const val SCREEN_TEAMS = "teams"
        const val SCREEN_TEAM_DETAIL = "team_detail"
        const val SCREEN_TEAM_ROSTER = "team_roster"
        const val SCREEN_PLAYER_DETAIL = "player_detail"
        const val SCREEN_STANDINGS = "standings"
        const val SCREEN_CALENDAR = "calendar"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_SYNC_SETTINGS = "sync_settings"
        
        // 🎯 CUSTOM EVENTS - Dominio específico
        const val EVENT_MATCH_VIEWED = "match_viewed"
        const val EVENT_TEAM_VIEWED = "team_viewed"
        const val EVENT_PLAYER_VIEWED = "player_viewed"
        const val EVENT_ROSTER_VIEWED = "roster_viewed"
        const val EVENT_STANDINGS_VIEWED = "standings_viewed"
        const val EVENT_CALENDAR_NAVIGATION = "calendar_navigation"
        
        // 🏀 BASKETBALL SPECIFIC EVENTS
        const val EVENT_LIVE_SCORE_VIEWED = "live_score_viewed"
        const val EVENT_MATCH_FAVORITE_ADDED = "match_favorite_added"
        const val EVENT_TEAM_FAVORITE_ADDED = "team_favorite_added"
        const val EVENT_PLAYER_STATS_VIEWED = "player_stats_viewed"
        
        // 💾 DATA EVENTS
        const val EVENT_DATA_SYNC_STARTED = "data_sync_started"
        const val EVENT_DATA_SYNC_COMPLETED = "data_sync_completed"
        const val EVENT_DATA_SYNC_FAILED = "data_sync_failed"
        const val EVENT_OFFLINE_MODE_ACCESSED = "offline_mode_accessed"
        
        // 🔍 SEARCH & DISCOVERY
        const val EVENT_SEARCH_PERFORMED = "search_performed"
        const val EVENT_FILTER_APPLIED = "filter_applied"
        const val EVENT_CONTENT_SHARED = "content_shared"
        
        // ⚡ PERFORMANCE EVENTS
        const val EVENT_APP_STARTUP_TIME = "app_startup_time"
        const val EVENT_IMAGE_LOAD_TIME = "image_load_time"
        const val EVENT_API_RESPONSE_TIME = "api_response_time"
        
        // 📊 PARAMETER KEYS
        const val PARAM_MATCH_ID = "match_id"
        const val PARAM_TEAM_CODE = "team_code"
        const val PARAM_TEAM_NAME = "team_name"
        const val PARAM_PLAYER_CODE = "player_code"
        const val PARAM_PLAYER_NAME = "player_name"
        const val PARAM_SCREEN_NAME = "screen_name"
        const val PARAM_CONTENT_TYPE = "content_type"
        const val PARAM_SEARCH_TERM = "search_term"
        const val PARAM_FILTER_TYPE = "filter_type"
        const val PARAM_SHARE_METHOD = "share_method"
        const val PARAM_ERROR_TYPE = "error_type"
        const val PARAM_LOAD_TIME_MS = "load_time_ms"
        const val PARAM_SUCCESS = "success"
        const val PARAM_SOURCE = "source"
    }
    
    /**
     * 📱 Track screen views con metadatos enriquecidos
     * Esencial para user journey analysis y SEO móvil
     */
    fun trackScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        
        // También establecer la propiedad de usuario para segmentación
        firebaseAnalytics.setUserProperty("last_screen_viewed", screenName)
    }
    
    /**
     * 🏀 Track eventos específicos de basketball con contexto rico
     */
    fun trackMatchViewed(matchId: String, homeTeam: String, awayTeam: String, isLive: Boolean = false) {
        val bundle = Bundle().apply {
            putString(PARAM_MATCH_ID, matchId)
            putString("home_team", homeTeam)
            putString("away_team", awayTeam)
            putBoolean("is_live", isLive)
            putString(PARAM_CONTENT_TYPE, "match")
        }
        firebaseAnalytics.logEvent(EVENT_MATCH_VIEWED, bundle)
    }
    
    fun trackTeamViewed(teamCode: String, teamName: String, source: String = "navigation") {
        val bundle = Bundle().apply {
            putString(PARAM_TEAM_CODE, teamCode)
            putString(PARAM_TEAM_NAME, teamName)
            putString(PARAM_SOURCE, source)
            putString(PARAM_CONTENT_TYPE, "team")
        }
        firebaseAnalytics.logEvent(EVENT_TEAM_VIEWED, bundle)
    }
    
    fun trackPlayerViewed(playerCode: String, playerName: String, teamCode: String) {
        val bundle = Bundle().apply {
            putString(PARAM_PLAYER_CODE, playerCode)
            putString(PARAM_PLAYER_NAME, playerName)
            putString(PARAM_TEAM_CODE, teamCode)
            putString(PARAM_CONTENT_TYPE, "player")
        }
        firebaseAnalytics.logEvent(EVENT_PLAYER_VIEWED, bundle)
    }
    
    /**
     * 📊 Track engagement events críticos para retención
     */
    fun trackRosterViewed(teamCode: String, teamName: String, playerCount: Int) {
        val bundle = Bundle().apply {
            putString(PARAM_TEAM_CODE, teamCode)
            putString(PARAM_TEAM_NAME, teamName)
            putInt("player_count", playerCount)
            putString(PARAM_CONTENT_TYPE, "roster")
        }
        firebaseAnalytics.logEvent(EVENT_ROSTER_VIEWED, bundle)
    }
    
    fun trackStandingsViewed(season: String = "2025-26") {
        val bundle = Bundle().apply {
            putString("season", season)
            putString(PARAM_CONTENT_TYPE, "standings")
        }
        firebaseAnalytics.logEvent(EVENT_STANDINGS_VIEWED, bundle)
    }
    
    /**
     * 🔍 Track search y discovery - crítico para content optimization
     */
    fun trackSearchPerformed(query: String, resultCount: Int = 0) {
        val bundle = Bundle().apply {
            putString(PARAM_SEARCH_TERM, query)
            putInt("result_count", resultCount)
        }
        firebaseAnalytics.logEvent(EVENT_SEARCH_PERFORMED, bundle)
    }
    
    fun trackFilterApplied(filterType: String, filterValue: String) {
        val bundle = Bundle().apply {
            putString(PARAM_FILTER_TYPE, filterType)
            putString("filter_value", filterValue)
        }
        firebaseAnalytics.logEvent(EVENT_FILTER_APPLIED, bundle)
    }
    
    /**
     * 💾 Track data operations - esencial para performance optimization
     */
    fun trackDataSyncStarted(syncType: String = "full") {
        val bundle = Bundle().apply {
            putString("sync_type", syncType)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics.logEvent(EVENT_DATA_SYNC_STARTED, bundle)
    }
    
    fun trackDataSyncCompleted(syncType: String, durationMs: Long, itemsCount: Int) {
        val bundle = Bundle().apply {
            putString("sync_type", syncType)
            putLong("duration_ms", durationMs)
            putInt("items_synced", itemsCount)
            putBoolean(PARAM_SUCCESS, true)
        }
        firebaseAnalytics.logEvent(EVENT_DATA_SYNC_COMPLETED, bundle)
    }
    
    fun trackDataSyncFailed(syncType: String, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("sync_type", syncType)
            putString("error_message", errorMessage)
            putBoolean(PARAM_SUCCESS, false)
        }
        firebaseAnalytics.logEvent(EVENT_DATA_SYNC_FAILED, bundle)
        
        // También reportar a Crashlytics
        crashlytics.recordException(Exception("Data sync failed: $syncType - $errorMessage"))
    }
    
    /**
     * ⚡ Track performance metrics - crítico para UX
     */
    fun trackAppStartupTime(startupTimeMs: Long) {
        val bundle = Bundle().apply {
            putLong(PARAM_LOAD_TIME_MS, startupTimeMs)
        }
        firebaseAnalytics.logEvent(EVENT_APP_STARTUP_TIME, bundle)
    }
    
    fun trackImageLoadTime(imageType: String, loadTimeMs: Long, success: Boolean) {
        val bundle = Bundle().apply {
            putString("image_type", imageType)
            putLong(PARAM_LOAD_TIME_MS, loadTimeMs)
            putBoolean(PARAM_SUCCESS, success)
        }
        firebaseAnalytics.logEvent(EVENT_IMAGE_LOAD_TIME, bundle)
    }
    
    fun trackApiResponseTime(endpoint: String, responseTimeMs: Long, success: Boolean) {
        val bundle = Bundle().apply {
            putString("endpoint", endpoint)
            putLong("response_time_ms", responseTimeMs)
            putBoolean(PARAM_SUCCESS, success)
        }
        firebaseAnalytics.logEvent(EVENT_API_RESPONSE_TIME, bundle)
    }
    
    /**
     * 🎯 Track user engagement patterns
     */
    fun trackContentShared(contentType: String, contentId: String, shareMethod: String) {
        val bundle = Bundle().apply {
            putString(PARAM_CONTENT_TYPE, contentType)
            putString("content_id", contentId)
            putString(PARAM_SHARE_METHOD, shareMethod)
        }
        firebaseAnalytics.logEvent(EVENT_CONTENT_SHARED, bundle)
    }
    
    fun trackFavoriteAdded(contentType: String, contentId: String) {
        val eventName = when (contentType) {
            "team" -> EVENT_TEAM_FAVORITE_ADDED
            "match" -> EVENT_MATCH_FAVORITE_ADDED
            else -> "favorite_added"
        }
        
        val bundle = Bundle().apply {
            putString(PARAM_CONTENT_TYPE, contentType)
            putString("content_id", contentId)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
    
    /**
     * 👤 Set user properties para segmentación avanzada
     */
    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }
    
    fun setFavoriteTeam(teamCode: String, teamName: String) {
        firebaseAnalytics.setUserProperty("favorite_team_code", teamCode)
        firebaseAnalytics.setUserProperty("favorite_team_name", teamName)
    }
    
    /**
     * 🔥 Crashlytics integration
     */
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }
    
    fun recordException(exception: Throwable, customData: Map<String, String> = emptyMap()) {
        customData.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(exception)
    }
    
    fun logMessage(message: String, priority: Int = android.util.Log.INFO) {
        crashlytics.log("Priority: $priority - $message")
    }
    
    /**
     * 🎯 Log custom events with bundle
     */
    fun logCustomEvent(eventName: String, bundle: Bundle) {
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
