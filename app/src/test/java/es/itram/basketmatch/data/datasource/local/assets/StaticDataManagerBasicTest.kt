package es.itram.basketmatch.data.datasource.local.assets

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream

/**
 * Test simple para verificar que StaticDataManager puede cargar datos JSON básicos
 */
class StaticDataManagerBasicTest {

    @MockK
    private lateinit var context: Context

    private lateinit var staticDataManager: StaticDataManager

    private val sampleValidJson = """[{"id": "1", "name": "Test"}]"""
    private val sampleInvalidJson = """{ invalid json }"""

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        staticDataManager = StaticDataManager(context)
    }

    @Test
    fun `StaticDataManager can be instantiated`() {
        // Given
        val manager = StaticDataManager(context)
        
        // Then
        assertNotNull("StaticDataManager should be created", manager)
    }

    @Test
    fun `loadStaticTeams handles file not found exception`() = runTest {
        // Given
        every { context.assets.open("static_data/teams_2025_26.json") } throws java.io.FileNotFoundException("File not found")

        // When
        val result = staticDataManager.loadStaticTeams()

        // Then
        assertTrue("Should return failure for missing file", result.isFailure)
        assertTrue("Should contain FileNotFoundException", 
            result.exceptionOrNull() is java.io.FileNotFoundException)
    }

    @Test
    fun `loadStaticMatches handles file not found exception`() = runTest {
        // Given
        every { context.assets.open("static_data/matches_calendar_2025_26.json") } throws java.io.FileNotFoundException("File not found")

        // When
        val result = staticDataManager.loadStaticMatches()

        // Then
        assertTrue("Should return failure for missing file", result.isFailure)
        assertTrue("Should contain FileNotFoundException", 
            result.exceptionOrNull() is java.io.FileNotFoundException)
    }

    @Test
    fun `loadDataVersion handles file not found exception`() = runTest {
        // Given
        every { context.assets.open("static_data/data_version.json") } throws java.io.FileNotFoundException("File not found")

        // When
        val result = staticDataManager.loadDataVersion()

        // Then
        assertTrue("Should return failure for missing file", result.isFailure)
        assertTrue("Should contain FileNotFoundException", 
            result.exceptionOrNull() is java.io.FileNotFoundException)
    }

    @Test
    fun `loadStaticTeams handles invalid JSON gracefully`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream(sampleInvalidJson.toByteArray())
        every { context.assets.open("static_data/teams_2025_26.json") } returns inputStream

        // When
        val result = staticDataManager.loadStaticTeams()

        // Then
        assertTrue("Should return failure for invalid JSON", result.isFailure)
        assertNotNull("Should have an exception", result.exceptionOrNull())
    }

    @Test
    fun `loadStaticMatches handles invalid JSON gracefully`() = runTest {
        // Given
        val inputStream = ByteArrayInputStream(sampleInvalidJson.toByteArray())
        every { context.assets.open("static_data/matches_calendar_2025_26.json") } returns inputStream

        // When
        val result = staticDataManager.loadStaticMatches()

        // Then
        assertTrue("Should return failure for invalid JSON", result.isFailure)
        assertNotNull("Should have an exception", result.exceptionOrNull())
    }

    @Test
    fun `all methods return Result objects`() = runTest {
        // Given - setup mock to throw exceptions
        every { context.assets.open(any()) } throws java.io.FileNotFoundException("File not found")

        // When
        val teamsResult = staticDataManager.loadStaticTeams()
        val matchesResult = staticDataManager.loadStaticMatches()
        val versionResult = staticDataManager.loadDataVersion()

        // Then
        assertNotNull("Teams result should not be null", teamsResult)
        assertNotNull("Matches result should not be null", matchesResult)
        assertNotNull("Version result should not be null", versionResult)
        
        assertTrue("All results should be failures", 
            teamsResult.isFailure && matchesResult.isFailure && versionResult.isFailure)
    }
}
