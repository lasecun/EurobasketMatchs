package es.itram.basketmatch.analytics.events

import android.os.Bundle
import es.itram.basketmatch.analytics.AnalyticsManager

/**
 * 📊 Analytics Events - Sistema de eventos tipados para tracking
 *
 * Define todos los eventos de analytics de forma estructurada y type-safe
 * para garantizar consistency y facilitar el mantenimiento.
 */
sealed class AnalyticsEvent {
    abstract fun toBundle(): Bundle
    abstract val eventName: String

    // 🏀 Basketball Content Events
    data class MatchEvent(
        val action: MatchAction,
        val matchId: String,
        val homeTeam: String? = null,
        val awayTeam: String? = null,
        val isLive: Boolean = false,
        val source: String = "navigation"
    ) : AnalyticsEvent() {
        override val eventName = when (action) {
            MatchAction.VIEWED -> AnalyticsManager.EVENT_MATCH_VIEWED
            MatchAction.FAVORITED -> AnalyticsManager.EVENT_MATCH_FAVORITE_ADDED
            MatchAction.LIVE_SCORE_VIEWED -> AnalyticsManager.EVENT_LIVE_SCORE_VIEWED
        }

        override fun toBundle() = Bundle().apply {
            putString(AnalyticsManager.PARAM_MATCH_ID, matchId)
            homeTeam?.let { putString("home_team", it) }
            awayTeam?.let { putString("away_team", it) }
            putBoolean("is_live", isLive)
            putString(AnalyticsManager.PARAM_SOURCE, source)
            putString(AnalyticsManager.PARAM_CONTENT_TYPE, "match")
        }
    }

    data class TeamContentEvent(
        val action: TeamAction,
        val teamCode: String,
        val teamName: String? = null,
        val source: String = "navigation"
    ) : AnalyticsEvent() {
        override val eventName = when (action) {
            TeamAction.VIEWED -> AnalyticsManager.EVENT_TEAM_VIEWED
            TeamAction.FAVORITED -> AnalyticsManager.EVENT_TEAM_FAVORITE_ADDED
            TeamAction.ROSTER_VIEWED -> AnalyticsManager.EVENT_ROSTER_VIEWED
        }

        override fun toBundle() = Bundle().apply {
            putString(AnalyticsManager.PARAM_TEAM_CODE, teamCode)
            teamName?.let { putString(AnalyticsManager.PARAM_TEAM_NAME, it) }
            putString(AnalyticsManager.PARAM_SOURCE, source)
            putString(AnalyticsManager.PARAM_CONTENT_TYPE, "team")
        }
    }

    data class PlayerEvent(
        val action: PlayerAction,
        val playerCode: String,
        val playerName: String? = null,
        val teamCode: String? = null
    ) : AnalyticsEvent() {
        override val eventName = when (action) {
            PlayerAction.VIEWED -> AnalyticsManager.EVENT_PLAYER_VIEWED
            PlayerAction.STATS_VIEWED -> AnalyticsManager.EVENT_PLAYER_STATS_VIEWED
        }

        override fun toBundle() = Bundle().apply {
            putString(AnalyticsManager.PARAM_PLAYER_CODE, playerCode)
            playerName?.let { putString(AnalyticsManager.PARAM_PLAYER_NAME, it) }
            teamCode?.let { putString(AnalyticsManager.PARAM_TEAM_CODE, it) }
            putString(AnalyticsManager.PARAM_CONTENT_TYPE, "player")
        }
    }

    // 🔍 Discovery & Navigation Events
    data class SearchEvent(
        val query: String,
        val resultCount: Int = 0,
        val category: String? = null
    ) : AnalyticsEvent() {
        override val eventName = AnalyticsManager.EVENT_SEARCH_PERFORMED

        override fun toBundle() = Bundle().apply {
            putString(AnalyticsManager.PARAM_SEARCH_TERM, query)
            putInt("result_count", resultCount)
            category?.let { putString("category", it) }
        }
    }

    data class FilterEvent(
        val filterType: String,
        val filterValue: String,
        val screen: String
    ) : AnalyticsEvent() {
        override val eventName = AnalyticsManager.EVENT_FILTER_APPLIED

        override fun toBundle() = Bundle().apply {
            putString(AnalyticsManager.PARAM_FILTER_TYPE, filterType)
            putString("filter_value", filterValue)
            putString("screen", screen)
        }
    }

    // 💾 Data & Sync Events
    data class DataSyncEvent(
        val action: SyncAction,
        val syncType: String = "full",
        val durationMs: Long? = null,
        val itemsCount: Int? = null,
        val success: Boolean = true,
        val errorType: String? = null
    ) : AnalyticsEvent() {
        override val eventName = when (action) {
            SyncAction.STARTED -> AnalyticsManager.EVENT_DATA_SYNC_STARTED
            SyncAction.COMPLETED -> AnalyticsManager.EVENT_DATA_SYNC_COMPLETED
            SyncAction.FAILED -> AnalyticsManager.EVENT_DATA_SYNC_FAILED
        }

        override fun toBundle() = Bundle().apply {
            putString("sync_type", syncType)
            putBoolean(AnalyticsManager.PARAM_SUCCESS, success)
            durationMs?.let { putLong("duration_ms", it) }
            itemsCount?.let { putInt("items_synced", it) }
            errorType?.let { putString(AnalyticsManager.PARAM_ERROR_TYPE, it) }
            putLong("timestamp", System.currentTimeMillis())
        }
    }

    // ⚡ Performance Events
    data class PerformanceEvent(
        val eventType: PerformanceType,
        val durationMs: Long,
        val context: String? = null
    ) : AnalyticsEvent() {
        override val eventName = when (eventType) {
            PerformanceType.APP_STARTUP -> AnalyticsManager.EVENT_APP_STARTUP_TIME
            PerformanceType.IMAGE_LOAD -> AnalyticsManager.EVENT_IMAGE_LOAD_TIME
            PerformanceType.API_RESPONSE -> AnalyticsManager.EVENT_API_RESPONSE_TIME
        }

        override fun toBundle() = Bundle().apply {
            putLong(AnalyticsManager.PARAM_LOAD_TIME_MS, durationMs)
            context?.let { putString("context", it) }
        }
    }

    // 📱 Screen View Event
    data class ScreenViewEvent(
        val screenName: String,
        val screenClass: String? = null
    ) : AnalyticsEvent() {
        override val eventName = "screen_view"

        override fun toBundle() = Bundle().apply {
            putString("screen_name", screenName)
            putString("screen_class", screenClass ?: screenName)
        }
    }
}

// Action Enums
enum class MatchAction { VIEWED, FAVORITED, LIVE_SCORE_VIEWED }
enum class TeamAction { VIEWED, FAVORITED, ROSTER_VIEWED }
enum class PlayerAction { VIEWED, STATS_VIEWED }
enum class SyncAction { STARTED, COMPLETED, FAILED }
enum class PerformanceType { APP_STARTUP, IMAGE_LOAD, API_RESPONSE }
