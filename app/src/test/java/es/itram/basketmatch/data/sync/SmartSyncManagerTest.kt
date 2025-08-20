package es.itram.basketmatch.data.sync

import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.data.datasource.local.assets.StaticDataManager
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.domain.model.DataVersion
import es.itram.basketmatch.domain.model.Team
import es.itram.basketmatch.domain.model.Match
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.TeamRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class SmartSyncManagerTest {

    @MockK
    private lateinit var staticDataManager: StaticDataManager

    @MockK
    private lateinit var teamRepository: TeamRepository

    @MockK
    private lateinit var matchRepository: MatchRepository

    @MockK
    private lateinit var remoteDataSource: EuroLeagueRemoteDataSource

    @MockK
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var smartSyncManager: SmartSyncManager

    private val sampleTeams = listOf(
        Team(
            id = "1",
            name = "FC Barcelona",
            code = "BAR",
            city = "Barcelona",
            country = "Spain",
            logoUrl = "https://example.com/barcelona.png",
            conference = "A"
        ),
        Team(
            id = "2",
            name = "Real Madrid",
            code = "RMB",
            city = "Madrid",
            country = "Spain",
            logoUrl = "https://example.com/madrid.png",
            conference = "A"
        )
    )

    private val sampleMatches = listOf(
        Match(
            id = "match1",
            homeTeamId = "1",
            awayTeamId = "2",
            homeTeamName = "FC Barcelona",
            awayTeamName = "Real Madrid",
            date = LocalDate.of(2025, 10, 15),
            time = "20:30",
            round = "Round 3",
            phase = "Regular Season",
            venue = "Palau de la Música Catalana",
            homeScore = null,
            awayScore = null,
            status = "SCHEDULED",
            isFinished = false
        )
    )

    private val sampleVersion = DataVersion(
        version = "2025-26-v1.0",
        lastUpdated = "2025-08-15T10:00:00Z",
        description = "EuroLeague 2025-26 season data"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        smartSyncManager = SmartSyncManager(
            staticDataManager,
            teamRepository,
            matchRepository,
            remoteDataSource,
            analyticsManager
        )
    }

    @Test
    fun `initializeStaticData loads data when no static data exists`() = runTest {
        // Given
        every { teamRepository.getAllTeams() } returns MutableStateFlow(emptyList())
        coEvery { staticDataManager.loadStaticTeams() } returns Result.success(sampleTeams)
        coEvery { staticDataManager.loadStaticMatches() } returns Result.success(sampleMatches)
        coEvery { staticDataManager.loadDataVersion() } returns Result.success(sampleVersion)
        coEvery { teamRepository.insertTeams(any()) } returns Unit
        coEvery { matchRepository.insertMatches(any()) } returns Unit
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit

        // When
        val result = smartSyncManager.initializeStaticData()

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { staticDataManager.loadStaticTeams() }
        coVerify { staticDataManager.loadStaticMatches() }
        coVerify { staticDataManager.loadDataVersion() }
        coVerify { teamRepository.insertTeams(sampleTeams) }
        coVerify { matchRepository.insertMatches(sampleMatches) }
        verify { analyticsManager.logCustomEvent("static_data_initialized", any()) }
    }

    @Test
    fun `initializeStaticData skips loading when static data already exists`() = runTest {
        // Given
        every { teamRepository.getAllTeams() } returns MutableStateFlow(sampleTeams)
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit

        // When
        val result = smartSyncManager.initializeStaticData()

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify(exactly = 0) { staticDataManager.loadStaticTeams() }
        coVerify(exactly = 0) { staticDataManager.loadStaticMatches() }
        coVerify(exactly = 0) { teamRepository.insertTeams(any()) }
        coVerify(exactly = 0) { matchRepository.insertMatches(any()) }
        verify { analyticsManager.logCustomEvent("static_data_initialized", any()) }
    }

    @Test
    fun `syncDynamicData updates match scores and status`() = runTest {
        // Given
        val today = LocalDate.now()
        val finishedMatch = sampleMatches[0].copy(
            homeScore = 85,
            awayScore = 78,
            status = "FINISHED",
            isFinished = true
        )
        
        coEvery { matchRepository.getMatchesByDateRange(any(), any()) } returns listOf(sampleMatches[0])
        coEvery { remoteDataSource.getMatchesByDateRange(any(), any()) } returns listOf(finishedMatch)
        coEvery { matchRepository.updateMatch(any()) } returns Unit
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit

        // When
        val result = smartSyncManager.syncDynamicData(forceSync = false)

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { matchRepository.getMatchesByDateRange(today.minusDays(7), today.plusDays(30)) }
        coVerify { remoteDataSource.getMatchesByDateRange(today.minusDays(7), today.plusDays(30)) }
        coVerify { matchRepository.updateMatch(finishedMatch) }
        verify { analyticsManager.logCustomEvent("smart_sync_completed", any()) }
    }

    @Test
    fun `checkForUpdates returns NoUpdatesAvailable when no new data`() = runTest {
        // Given
        coEvery { staticDataManager.loadDataVersion() } returns Result.success(sampleVersion)
        // Simular que no hay nuevos datos remotos disponibles
        coEvery { remoteDataSource.getLatestDataVersion() } returns sampleVersion

        // When
        val result = smartSyncManager.checkForUpdates()

        // Then
        assertEquals(UpdateCheckResult.NoUpdatesAvailable, result)
        coVerify { staticDataManager.loadDataVersion() }
        coVerify { remoteDataSource.getLatestDataVersion() }
    }

    @Test
    fun `checkForUpdates returns UpdatesAvailable when new data exists`() = runTest {
        // Given
        val newVersion = sampleVersion.copy(
            version = "2025-26-v1.1",
            lastUpdated = "2025-08-20T10:00:00Z"
        )
        coEvery { staticDataManager.loadDataVersion() } returns Result.success(sampleVersion)
        coEvery { remoteDataSource.getLatestDataVersion() } returns newVersion

        // When
        val result = smartSyncManager.checkForUpdates()

        // Then
        assertTrue("Should be UpdatesAvailable", result is UpdateCheckResult.UpdatesAvailable)
        val updatesResult = result as UpdateCheckResult.UpdatesAvailable
        assertEquals("2025-26-v1.1", updatesResult.newVersion.version)
        assertEquals("2025-26-v1.0", updatesResult.currentVersion.version)
    }

    @Test
    fun `state flows update correctly during operations`() = runTest {
        // Given
        every { teamRepository.getAllTeams() } returns MutableStateFlow(emptyList())
        coEvery { staticDataManager.loadStaticTeams() } returns Result.success(sampleTeams)
        coEvery { staticDataManager.loadStaticMatches() } returns Result.success(sampleMatches)
        coEvery { staticDataManager.loadDataVersion() } returns Result.success(sampleVersion)
        coEvery { teamRepository.insertTeams(any()) } returns Unit
        coEvery { matchRepository.insertMatches(any()) } returns Unit
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit

        // When
        smartSyncManager.initializeStaticData()

        // Then
        val finalState = smartSyncManager.syncState.first()
        assertFalse("Should not be initializing", finalState.isInitializing)
        assertFalse("Should not be syncing", finalState.isSyncing)
        assertTrue("Should have succeeded", finalState.lastSyncSuccess)
        assertEquals("Datos estáticos listos", finalState.status)
    }

    @Test
    fun `error handling works correctly for failed static data loading`() = runTest {
        // Given
        every { teamRepository.getAllTeams() } returns MutableStateFlow(emptyList())
        coEvery { staticDataManager.loadStaticTeams() } returns Result.failure(Exception("Network error"))
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit

        // When
        val result = smartSyncManager.initializeStaticData()

        // Then
        assertTrue("Should return failure", result.isFailure)
        assertTrue("Should contain expected exception", 
            result.exceptionOrNull()?.message?.contains("Network error") == true)
        
        val finalState = smartSyncManager.syncState.first()
        assertFalse("Should not be initializing", finalState.isInitializing)
        assertFalse("Should not be syncing", finalState.isSyncing)
        assertFalse("Should have failed", finalState.lastSyncSuccess)
        assertNotNull("Should have error", finalState.error)
    }

    @Test
    fun `force sync bypasses cache and fetches fresh data`() = runTest {
        // Given
        val today = LocalDate.now()
        coEvery { matchRepository.getMatchesByDateRange(any(), any()) } returns sampleMatches
        coEvery { remoteDataSource.getMatchesByDateRange(any(), any()) } returns sampleMatches
        coEvery { matchRepository.updateMatch(any()) } returns Unit
        every { analyticsManager.logCustomEvent(any(), any()) } returns Unit

        // When
        val result = smartSyncManager.syncDynamicData(forceSync = true)

        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { remoteDataSource.getMatchesByDateRange(today.minusDays(7), today.plusDays(30)) }
        verify { analyticsManager.logCustomEvent("smart_sync_completed", any()) }
        
        // Verify that the analytics event includes force_sync = true
        verify { 
            analyticsManager.logCustomEvent("smart_sync_completed", 
                match { bundle -> 
                    bundle.getString("force_sync") == "true"
                }
            ) 
        }
    }
}
