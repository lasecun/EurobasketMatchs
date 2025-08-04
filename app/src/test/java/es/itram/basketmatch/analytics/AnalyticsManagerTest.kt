package es.itram.basketmatch.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para AnalyticsManager - Cobertura de tracking de eventos
 */
class AnalyticsManagerTest {

    private val firebaseAnalytics: FirebaseAnalytics = mockk()
    private val crashlytics: FirebaseCrashlytics = mockk()
    private lateinit var analyticsManager: AnalyticsManager

    @Before
    fun setUp() {
        every { firebaseAnalytics.logEvent(any(), any()) } returns Unit
        every { firebaseAnalytics.setUserProperty(any(), any()) } returns Unit
        every { crashlytics.recordException(any()) } returns Unit
        every { crashlytics.setCustomKey(any<String>(), any<String>()) } returns Unit
        every { crashlytics.log(any()) } returns Unit
        every { crashlytics.setUserId(any()) } returns Unit
        every { firebaseAnalytics.setUserId(any()) } returns Unit
        
        analyticsManager = AnalyticsManager(firebaseAnalytics, crashlytics)
    }

    @Test
    fun `trackScreenView logs screen view event`() {
        // Given
        val screenName = "test_screen"
        val screenClass = "TestActivity"

        // When
        analyticsManager.trackScreenView(screenName, screenClass)

        // Then
        verify { firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, any()) }
        verify { firebaseAnalytics.setUserProperty("last_screen_viewed", screenName) }
    }

    @Test
    fun `trackMatchViewed logs match view event`() {
        // Given
        val matchId = "match123"
        val homeTeam = "Real Madrid"
        val awayTeam = "Barcelona"
        val isLive = true

        // When
        analyticsManager.trackMatchViewed(matchId, homeTeam, awayTeam, isLive)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_MATCH_VIEWED, any()) }
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
    }

    @Test
    fun `trackTeamViewed logs team view event`() {
        // Given
        val teamCode = "MAD"
        val teamName = "Real Madrid"
        val source = "navigation"

        // When
        analyticsManager.trackTeamViewed(teamCode, teamName, source)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_TEAM_VIEWED, any()) }
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
    }

    @Test
    fun `trackFavoriteAdded logs favorite event for team`() {
        // Given
        val contentType = "team"
        val contentId = "MAD"

        // When
        analyticsManager.trackFavoriteAdded(contentType, contentId)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_TEAM_FAVORITE_ADDED, any()) }
    }

    @Test
    fun `trackFavoriteAdded logs favorite event for match`() {
        // Given
        val contentType = "match"
        val contentId = "match123"

        // When
        analyticsManager.trackFavoriteAdded(contentType, contentId)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_MATCH_FAVORITE_ADDED, any()) }
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
    fun `logCustomEvent logs custom event`() {
        // Given
        val eventName = "custom_event"
        val bundle = Bundle()

        // When
        analyticsManager.logCustomEvent(eventName, bundle)

        // Then
        verify { firebaseAnalytics.logEvent(eventName, bundle) }
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
    }

    @Test
    fun `trackDataSyncStarted logs sync start event`() {
        // Given
        val syncType = "teams"

        // When
        analyticsManager.trackDataSyncStarted(syncType)

        // Then
        verify { firebaseAnalytics.logEvent(AnalyticsManager.EVENT_DATA_SYNC_STARTED, any()) }
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
    fun `recordException records exception to crashlytics`() {
        // Given
        val exception = RuntimeException("Test exception")
        val customKeys = mapOf("key1" to "value1", "key2" to "value2")

        // When
        analyticsManager.recordException(exception, customKeys)

        // Then
        verify { crashlytics.recordException(exception) }
        verify { crashlytics.setCustomKey("key1", "value1") }
        verify { crashlytics.setCustomKey("key2", "value2") }
    }
}