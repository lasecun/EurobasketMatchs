package es.itram.basketmatch.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para AnalyticsManager - Cobertura de tracking de eventos
 */
class AnalyticsManagerTest {

    @MockK
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @MockK
    private lateinit var crashlytics: FirebaseCrashlytics

    private lateinit var analyticsManager: AnalyticsManager

    private val bundleSlot = slot<Bundle>()
    private val eventNameSlot = slot<String>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        justRun { firebaseAnalytics.logEvent(capture(eventNameSlot), capture(bundleSlot)) }
        justRun { firebaseAnalytics.setUserProperty(any(), any()) }
        justRun { crashlytics.recordException(any()) }
        justRun { crashlytics.setCustomKey(any<String>(), any<String>()) }
        justRun { crashlytics.log(any()) }
        justRun { crashlytics.setUserId(any()) }
        justRun { firebaseAnalytics.setUserId(any()) }
        
        analyticsManager = AnalyticsManager(firebaseAnalytics, crashlytics)
    }

    @Test
    fun `trackScreenView logs correct screen view event`() {
        // Given
        val screenName = "test_screen"
        val screenClass = "TestActivity"

        // When
        analyticsManager.trackScreenView(screenName, screenClass)

        // Then
        verify { firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, any()) }
        verify { firebaseAnalytics.setUserProperty("last_screen_viewed", screenName) }
        
        assertEquals(FirebaseAnalytics.Event.SCREEN_VIEW, eventNameSlot.captured)
        assertEquals(screenName, bundleSlot.captured.getString(FirebaseAnalytics.Param.SCREEN_NAME))
        assertEquals(screenClass, bundleSlot.captured.getString(FirebaseAnalytics.Param.SCREEN_CLASS))
    }

    @Test
    fun `trackScreenView with null screenClass uses screenName`() {
        // Given
        val screenName = "test_screen"

        // When
        analyticsManager.trackScreenView(screenName, null)

        // Then
        assertEquals(screenName, bundleSlot.captured.getString(FirebaseAnalytics.Param.SCREEN_CLASS))
    }

    @Test
    fun `trackMatchViewed logs match view event with correct parameters`() {
        // Given
        val matchId = "match123"
        val homeTeam = "Real Madrid"
        val awayTeam = "Barcelona"
        val isLive = true

        // When
        analyticsManager.trackMatchViewed(matchId, homeTeam, awayTeam, isLive)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_MATCH_VIEWED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_MATCH_VIEWED, eventNameSlot.captured)
        assertEquals(matchId, bundleSlot.captured.getString(AnalyticsManager.PARAM_MATCH_ID))
        assertEquals(homeTeam, bundleSlot.captured.getString("home_team"))
        assertEquals(awayTeam, bundleSlot.captured.getString("away_team"))
        assertEquals(isLive, bundleSlot.captured.getBoolean("is_live"))
        assertEquals("match", bundleSlot.captured.getString(AnalyticsManager.PARAM_CONTENT_TYPE))
    }

    @Test
    fun `trackPlayerViewed logs player view event`() {
        // Given
        val playerCode = "P001234"
        val playerName = "Luka Doncic"
        val teamCode = "MAD"

        // When
        analyticsManager.trackPlayerViewed(playerCode, playerName, teamCode)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_PLAYER_VIEWED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_PLAYER_VIEWED, eventNameSlot.captured)
        assertEquals(playerCode, bundleSlot.captured.getString(AnalyticsManager.PARAM_PLAYER_CODE))
        assertEquals(playerName, bundleSlot.captured.getString(AnalyticsManager.PARAM_PLAYER_NAME))
        assertEquals(teamCode, bundleSlot.captured.getString(AnalyticsManager.PARAM_TEAM_CODE))
    }

    @Test
    fun `trackTeamViewed logs team view event with source`() {
        // Given
        val teamCode = "MAD"
        val teamName = "Real Madrid"
        val source = "navigation"

        // When
        analyticsManager.trackTeamViewed(teamCode, teamName, source)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_TEAM_VIEWED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_TEAM_VIEWED, eventNameSlot.captured)
        assertEquals(teamCode, bundleSlot.captured.getString(AnalyticsManager.PARAM_TEAM_CODE))
        assertEquals(teamName, bundleSlot.captured.getString(AnalyticsManager.PARAM_TEAM_NAME))
        assertEquals(source, bundleSlot.captured.getString(AnalyticsManager.PARAM_SOURCE))
    }

    @Test
    fun `trackSearchPerformed logs search event`() {
        // Given
        val query = "real madrid"
        val resultCount = 5

        // When
        analyticsManager.trackSearchPerformed(query, resultCount)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_SEARCH_PERFORMED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_SEARCH_PERFORMED, eventNameSlot.captured)
        assertEquals(query, bundleSlot.captured.getString(AnalyticsManager.PARAM_SEARCH_TERM))
        assertEquals(resultCount, bundleSlot.captured.getInt("result_count"))
    }

    @Test
    fun `trackContentShared logs share event`() {
        // Given
        val contentType = "match"
        val contentId = "match123"
        val shareMethod = "native_share"

        // When
        analyticsManager.trackContentShared(contentType, contentId, shareMethod)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_CONTENT_SHARED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_CONTENT_SHARED, eventNameSlot.captured)
        assertEquals(contentType, bundleSlot.captured.getString(AnalyticsManager.PARAM_CONTENT_TYPE))
        assertEquals(contentId, bundleSlot.captured.getString("content_id"))
        assertEquals(shareMethod, bundleSlot.captured.getString(AnalyticsManager.PARAM_SHARE_METHOD))
    }

    @Test
    fun `trackFavoriteAdded logs team favorite event`() {
        // Given
        val contentType = "team"
        val contentId = "MAD"

        // When
        analyticsManager.trackFavoriteAdded(contentType, contentId)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_TEAM_FAVORITE_ADDED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_TEAM_FAVORITE_ADDED, eventNameSlot.captured)
        assertEquals(contentType, bundleSlot.captured.getString(AnalyticsManager.PARAM_CONTENT_TYPE))
        assertEquals(contentId, bundleSlot.captured.getString("content_id"))
    }

    @Test
    fun `trackFavoriteAdded logs match favorite event`() {
        // Given
        val contentType = "match"
        val contentId = "match123"

        // When
        analyticsManager.trackFavoriteAdded(contentType, contentId)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_MATCH_FAVORITE_ADDED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_MATCH_FAVORITE_ADDED, eventNameSlot.captured)
        assertEquals(contentType, bundleSlot.captured.getString(AnalyticsManager.PARAM_CONTENT_TYPE))
        assertEquals(contentId, bundleSlot.captured.getString("content_id"))
    }

    @Test
    fun `setFavoriteTeam sets user properties`() {
        // Given
        val teamCode = "MAD"
        val teamName = "Real Madrid"

        // When
        analyticsManager.setFavoriteTeam(teamCode, teamName)

        // Then
        verify { firebaseAnalytics.setUserProperty("favorite_team_code", teamCode) }
        verify { firebaseAnalytics.setUserProperty("favorite_team_name", teamName) }
    }

    @Test
    fun `recordException logs exception to crashlytics with custom data`() {
        // Given
        val exception = RuntimeException("Test exception")
        val metadata = mapOf("context" to "test", "user_id" to "123")

        // When
        analyticsManager.recordException(exception, metadata)

        // Then
        verify { crashlytics.recordException(exception) }
        verify { crashlytics.setCustomKey("context", "test") }
        verify { crashlytics.setCustomKey("user_id", "123") }
    }

    @Test
    fun `logCustomEvent logs event with bundle`() {
        // Given
        val eventName = "custom_event"
        val bundle = Bundle().apply {
            putString("key1", "value1")
            putInt("key2", 123)
        }

        // When
        analyticsManager.logCustomEvent(eventName, bundle)

        // Then
        verify { firebaseAnalytics.logEvent(eventName, bundle) }
        assertEquals(eventName, eventNameSlot.captured)
    }

    @Test
    fun `logMessage logs message to crashlytics`() {
        // Given
        val message = "Test log message"
        val priority = 3

        // When
        analyticsManager.logMessage(message, priority)

        // Then
        verify { crashlytics.log("Priority: $priority - $message") }
    }

    @Test
    fun `trackRosterViewed logs roster event`() {
        // Given
        val teamCode = "MAD"
        val teamName = "Real Madrid"
        val playerCount = 15

        // When
        analyticsManager.trackRosterViewed(teamCode, teamName, playerCount)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_ROSTER_VIEWED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_ROSTER_VIEWED, eventNameSlot.captured)
        assertEquals(teamCode, bundleSlot.captured.getString(AnalyticsManager.PARAM_TEAM_CODE))
        assertEquals(teamName, bundleSlot.captured.getString(AnalyticsManager.PARAM_TEAM_NAME))
        assertEquals(playerCount, bundleSlot.captured.getInt("player_count"))
        assertEquals("roster", bundleSlot.captured.getString(AnalyticsManager.PARAM_CONTENT_TYPE))
    }

    @Test
    fun `trackDataSyncStarted logs sync start event`() {
        // Given
        val syncType = "teams"

        // When
        analyticsManager.trackDataSyncStarted(syncType)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_DATA_SYNC_STARTED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_DATA_SYNC_STARTED, eventNameSlot.captured)
        assertEquals(syncType, bundleSlot.captured.getString("sync_type"))
        assertTrue("Should include timestamp", bundleSlot.captured.getLong("timestamp") > 0)
    }

    @Test
    fun `trackDataSyncCompleted logs sync completion event`() {
        // Given
        val syncType = "teams"
        val durationMs = 1500L
        val itemsCount = 30

        // When
        analyticsManager.trackDataSyncCompleted(syncType, durationMs, itemsCount)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_DATA_SYNC_COMPLETED, any()) }
        
        assertEquals(AnalyticsManager.EVENT_DATA_SYNC_COMPLETED, eventNameSlot.captured)
        assertEquals(syncType, bundleSlot.captured.getString("sync_type"))
        assertEquals(durationMs, bundleSlot.captured.getLong("duration_ms"))
        assertEquals(itemsCount, bundleSlot.captured.getInt("items_synced"))
        assertTrue("Should mark as successful", bundleSlot.captured.getBoolean(AnalyticsManager.PARAM_SUCCESS))
    }

    @Test
    fun `trackDataSyncFailed logs sync failure and records exception`() {
        // Given
        val syncType = "teams"
        val errorMessage = "Network timeout"

        // When
        analyticsManager.trackDataSyncFailed(syncType, errorMessage)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_DATA_SYNC_FAILED, any()) }
        verify { crashlytics.recordException(any<Exception>()) }
        
        assertEquals(AnalyticsManager.EVENT_DATA_SYNC_FAILED, eventNameSlot.captured)
        assertEquals(syncType, bundleSlot.captured.getString("sync_type"))
        assertEquals(errorMessage, bundleSlot.captured.getString("error_message"))
        assertFalse("Should mark as failed", bundleSlot.captured.getBoolean(AnalyticsManager.PARAM_SUCCESS))
    }

    @Test
    fun `setUserId sets user ID in both analytics and crashlytics`() {
        // Given
        val userId = "user123"

        // When
        analyticsManager.setUserId(userId)

        // Then
        verify { firebaseAnalytics.setUserId(userId) }
        verify { crashlytics.setUserId(userId) }
    }

    @Test
    fun `screen name constants are correct`() {
        assertEquals("home", AnalyticsManager.SCREEN_HOME)
        assertEquals("match_detail", AnalyticsManager.SCREEN_MATCH_DETAIL)
        assertEquals("team_roster", AnalyticsManager.SCREEN_TEAM_ROSTER)
        assertEquals("player_detail", AnalyticsManager.SCREEN_PLAYER_DETAIL)
        assertEquals("calendar", AnalyticsManager.SCREEN_CALENDAR)
    }

    @Test
    fun `event name constants are correct`() {
        assertEquals("match_viewed", AnalyticsManager.EVENT_MATCH_VIEWED)
        assertEquals("player_viewed", AnalyticsManager.EVENT_PLAYER_VIEWED)
        assertEquals("team_viewed", AnalyticsManager.EVENT_TEAM_VIEWED)
        assertEquals("roster_viewed", AnalyticsManager.EVENT_ROSTER_VIEWED)
        assertEquals("search_performed", AnalyticsManager.EVENT_SEARCH_PERFORMED)
    }

    @Test
    fun `parameter constants are correct`() {
        assertEquals("match_id", AnalyticsManager.PARAM_MATCH_ID)
        assertEquals("team_code", AnalyticsManager.PARAM_TEAM_CODE)
        assertEquals("player_code", AnalyticsManager.PARAM_PLAYER_CODE)
        assertEquals("content_type", AnalyticsManager.PARAM_CONTENT_TYPE)
        assertEquals("source", AnalyticsManager.PARAM_SOURCE)
    }
}
