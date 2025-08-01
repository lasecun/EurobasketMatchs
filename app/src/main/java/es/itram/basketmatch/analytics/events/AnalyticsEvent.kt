package es.itram.basketmatch.analytics.events

/**
 * 📊 Analytics Events - Eventos específicos para tracking de comportamiento de usuario
 * 
 * Esta clase define eventos customizados que proporcionan insights valiosos sobre:
 * - User engagement patterns
 * - Feature adoption rates  
 * - Content consumption metrics
 * - Performance bottlenecks
 * - Error patterns
 * 
 * Optimizado para SEO móvil y analytics de aplicaciones deportivas.
 */
sealed class AnalyticsEvent {
    
    // 📱 SCREEN NAVIGATION EVENTS
    data class ScreenViewed(
        val screenName: String,
        val screenClass: String? = null,
        val previousScreen: String? = null,
        val sessionDuration: Long? = null
    ) : AnalyticsEvent()
    
    // 🏀 BASKETBALL CONTENT EVENTS
    data class MatchContentEvent(
        val action: MatchAction,
        val matchId: String,
        val homeTeam: String,
        val awayTeam: String,
        val matchStatus: String,
        val isLive: Boolean = false,
        val source: String = "navigation"
    ) : AnalyticsEvent()
    
    data class TeamContentEvent(
        val action: TeamAction,
        val teamCode: String,
        val teamName: String,
        val source: String = "navigation",
        val context: String? = null
    ) : AnalyticsEvent()
    
    data class PlayerContentEvent(
        val action: PlayerAction,
        val playerCode: String,
        val playerName: String,
        val teamCode: String,
        val position: String? = null,
        val source: String = "navigation"
    ) : AnalyticsEvent()
    
    // 📊 DATA INTERACTION EVENTS
    data class DataSyncEvent(
        val action: DataAction,
        val syncType: String,
        val durationMs: Long? = null,
        val itemsCount: Int? = null,
        val errorMessage: String? = null
    ) : AnalyticsEvent()
    
    // 🔍 SEARCH & DISCOVERY EVENTS
    data class SearchEvent(
        val query: String,
        val category: String? = null,
        val resultCount: Int = 0,
        val selectedResultIndex: Int? = null
    ) : AnalyticsEvent()
    
    data class FilterEvent(
        val filterType: String,
        val filterValue: String,
        val resultCount: Int = 0,
        val screen: String
    ) : AnalyticsEvent()
    
    // 💝 ENGAGEMENT EVENTS
    data class FavoriteEvent(
        val action: FavoriteAction,
        val contentType: String,
        val contentId: String,
        val contentName: String
    ) : AnalyticsEvent()
    
    data class ShareEvent(
        val contentType: String,
        val contentId: String,
        val shareMethod: String,
        val contentTitle: String? = null
    ) : AnalyticsEvent()
    
    // ⚡ PERFORMANCE EVENTS
    data class PerformanceEvent(
        val type: PerformanceType,
        val durationMs: Long,
        val success: Boolean,
        val details: Map<String, Any> = emptyMap()
    ) : AnalyticsEvent()
    
    // 📱 USER BEHAVIOR EVENTS
    data class UserInteractionEvent(
        val action: String,
        val element: String,
        val screen: String,
        val value: String? = null
    ) : AnalyticsEvent()
    
    // 🔥 ERROR EVENTS
    data class ErrorEvent(
        val errorType: String,
        val errorMessage: String,
        val screen: String,
        val action: String? = null,
        val severity: ErrorSeverity = ErrorSeverity.MEDIUM
    ) : AnalyticsEvent()
}

// 🏀 MATCH ACTIONS
enum class MatchAction {
    VIEWED,
    LIVE_SCORE_CHECKED,
    DETAILS_EXPANDED,
    STATS_VIEWED,
    FAVORITED,
    SHARED
}

// 👥 TEAM ACTIONS  
enum class TeamAction {
    VIEWED,
    ROSTER_ACCESSED,
    STATS_VIEWED,
    MATCHES_VIEWED,
    FAVORITED,
    SHARED
}

// 🏃 PLAYER ACTIONS
enum class PlayerAction {
    VIEWED,
    STATS_VIEWED,
    IMAGE_VIEWED,
    PROFILE_ACCESSED,
    SHARED
}

// 💾 DATA ACTIONS
enum class DataAction {
    SYNC_STARTED,
    SYNC_COMPLETED,
    SYNC_FAILED,
    CACHE_HIT,
    CACHE_MISS,
    OFFLINE_ACCESS
}

// 💝 FAVORITE ACTIONS
enum class FavoriteAction {
    ADDED,
    REMOVED,
    VIEWED_LIST
}

// ⚡ PERFORMANCE TYPES
enum class PerformanceType {
    APP_STARTUP,
    SCREEN_LOAD,
    IMAGE_LOAD,
    API_CALL,
    DATABASE_QUERY,
    NETWORK_REQUEST
}

// 🚨 ERROR SEVERITY
enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
