package es.itram.basketmatch.data.datasource.local.assets

import android.content.Context
import es.itram.basketmatch.domain.model.Team
import es.itram.basketmatch.domain.model.Match
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.time.LocalDate

class StaticDataManagerTest {

    @MockK
    private lateinit var context: Context

    private lateinit var staticDataManager: StaticDataManager

    private val sampleTeamsJson = """
        [
            {
                "id": "1",
                "name": "FC Barcelona",
                "code": "BAR",
                "city": "Barcelona",
                "country": "Spain",
                "logoUrl": "https://example.com/barcelona.png",
                "conference": "A"
            },
            {
                "id": "2",
                "name": "Real Madrid",
                "code": "RMB",
                "city": "Madrid",
                "country": "Spain",
                "logoUrl": "https://example.com/madrid.png",
                "conference": "A"
            }
        ]
    """.trimIndent()

    private val sampleMatchesJson = """
        [
            {
                "id": "match1",
                "homeTeamId": "1",
                "awayTeamId": "2",
                "homeTeamName": "FC Barcelona",
                "awayTeamName": "Real Madrid",
                "date": "2025-10-15",
                "time": "20:30",
                "round": "Round 3",
                "phase": "Regular Season",
                "venue": "Palau de la Música Catalana",
                "homeScore": null,
                "awayScore": null,
                "status": "SCHEDULED",
                "isFinished": false
            }
        ]
    """.trimIndent()

    private val sampleVersionJson = """
        {
            "version": "2025-26-v1.0",
            "lastUpdated": "2025-08-15T10:00:00Z",
            "description": "EuroLeague 2025-26 season data"
        }
    """.trimIndent()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        staticDataManager = StaticDataManager(context)
    }

    @Test
    fun `loadStaticTeams returns teams when valid JSON is provided`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream(sampleTeamsJson.toByteArray())
        every { context.assets.open("static_data/teams_2025_26.json") } returns inputStream

        // When
        val result = staticDataManager.loadStaticTeams()

        // Then
        assertTrue("Should return success", result.isSuccess)
        val teams = result.getOrNull()
        assertNotNull("Teams should not be null", teams)
        assertEquals("Should have 2 teams", 2, teams!!.size)
        
        val barcelona = teams.find { it.code == "BAR" }
        assertNotNull("Barcelona should exist", barcelona)
        assertEquals("FC Barcelona", barcelona!!.name)
        assertEquals("Barcelona", barcelona.city)
        assertEquals("Spain", barcelona.country)
    }

    @Test
    fun `loadStaticMatches returns matches when valid JSON is provided`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream(sampleMatchesJson.toByteArray())
        every { context.assets.open("static_data/matches_calendar_2025_26.json") } returns inputStream

        // When
        val result = staticDataManager.loadStaticMatches()

        // Then
        assertTrue("Should return success", result.isSuccess)
        val matches = result.getOrNull()
        assertNotNull("Matches should not be null", matches)
        assertEquals("Should have 1 match", 1, matches!!.size)
        
        val match = matches[0]
        assertEquals("match1", match.id)
        assertEquals("FC Barcelona", match.homeTeamName)
        assertEquals("Real Madrid", match.awayTeamName)
        assertEquals(LocalDate.of(2025, 10, 15), match.date)
        assertEquals("20:30", match.time)
        assertFalse("Match should not be finished", match.isFinished)
    }

    @Test
    fun `loadDataVersion returns version info when valid JSON is provided`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream(sampleVersionJson.toByteArray())
        every { context.assets.open("static_data/data_version.json") } returns inputStream

        // When
        val result = staticDataManager.loadDataVersion()

        // Then
        assertTrue("Should return success", result.isSuccess)
        val version = result.getOrNull()
        assertNotNull("Version should not be null", version)
        assertEquals("2025-26-v1.0", version!!.version)
        assertEquals("2025-08-15T10:00:00Z", version.lastUpdated)
        assertEquals("EuroLeague 2025-26 season data", version.description)
    }

    @Test
    fun `loadStaticTeams returns failure when file not found`() = runTest {
        // Given
        every { context.assets.open("static_data/teams_2025_26.json") } throws java.io.FileNotFoundException("File not found")

        // When
        val result = staticDataManager.loadStaticTeams()

        // Then
        assertTrue("Should return failure", result.isFailure)
        assertTrue("Should contain FileNotFoundException", 
            result.exceptionOrNull() is java.io.FileNotFoundException)
    }

    @Test
    fun `loadStaticTeams returns failure when JSON is invalid`() = runTest {
        // Given
        val invalidJson = "{ invalid json }"
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())
        every { context.assets.open("static_data/teams_2025_26.json") } returns inputStream

        // When
        val result = staticDataManager.loadStaticTeams()

        // Then
        assertTrue("Should return failure", result.isFailure)
    }

    @Test
    fun `loadStaticMatches returns failure when JSON is invalid`() = runTest {
        // Given
        val invalidJson = "[ { invalid: json } ]"
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())
        every { context.assets.open("static_data/matches_calendar_2025_26.json") } returns inputStream

        // When
        val result = staticDataManager.loadStaticMatches()

        // Then
        assertTrue("Should return failure", result.isFailure)
    }

    @Test
    fun `all load methods call correct asset files`() = runTest {
        // Given
        val teamsStream = ByteArrayInputStream(sampleTeamsJson.toByteArray())
        val matchesStream = ByteArrayInputStream(sampleMatchesJson.toByteArray())
        val versionStream = ByteArrayInputStream(sampleVersionJson.toByteArray())
        
        every { context.assets.open("static_data/teams_2025_26.json") } returns teamsStream
        every { context.assets.open("static_data/matches_calendar_2025_26.json") } returns matchesStream
        every { context.assets.open("static_data/data_version.json") } returns versionStream

        // When
        staticDataManager.loadStaticTeams()
        staticDataManager.loadStaticMatches()
        staticDataManager.loadDataVersion()

        // Then
        verify { context.assets.open("static_data/teams_2025_26.json") }
        verify { context.assets.open("static_data/matches_calendar_2025_26.json") }
        verify { context.assets.open("static_data/data_version.json") }
    }
}
