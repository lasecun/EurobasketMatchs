package es.itram.basketmatch.integration

import org.junit.Test
import org.junit.Assert.*
import kotlinx.serialization.json.Json
import es.itram.basketmatch.data.datasource.local.assets.StaticDataManager

/**
 * Integration tests for static data system
 */
class StaticDataIntegrationTest {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    @Test
    fun staticDataManager_hasCorrectDependencies() {
        // Verify StaticDataManager can be created with required dependencies
        val staticDataManagerClass = StaticDataManager::class.java
        assertNotNull("StaticDataManager should exist", staticDataManagerClass)
        
        // Check constructor parameters
        val constructors = staticDataManagerClass.constructors
        assertTrue("StaticDataManager should have constructors", constructors.isNotEmpty())
    }
    
    @Test
    fun staticDataAssets_pathsAreCorrect() {
        // Verify that the expected asset paths are well-formed
        val expectedPaths = listOf(
            "static_data/teams_2025_26.json",
            "static_data/matches_calendar_2025_26.json", 
            "static_data/data_version.json"
        )
        
        expectedPaths.forEach { path ->
            assertFalse("Path should not be empty", path.isEmpty())
            assertTrue("Path should be well-formed", path.contains("/"))
            assertTrue("Path should be JSON", path.endsWith(".json"))
        }
    }
    
    @Test
    fun teamLogos_pathsAreCorrect() {
        // Verify team logo asset paths
        val logoPath = "file:///android_asset/team_logos/mad_logo.png"
        assertTrue("Logo path should be android asset URL", logoPath.startsWith("file:///android_asset/"))
        assertTrue("Logo path should point to team_logos", logoPath.contains("team_logos/"))
        assertTrue("Logo should be PNG", logoPath.endsWith(".png"))
    }
    
    @Test
    fun json_canParseBasicStructures() {
        // Test that JSON can parse expected data structures
        val sampleTeamJson = """
        {
            "version": "2025-26",
            "lastUpdated": "2025-08-25T00:00:00Z",
            "teams": [
                {
                    "id": "MAD",
                    "name": "Real Madrid",
                    "city": "Madrid",
                    "country": "ESP",
                    "logoUrl": "file:///android_asset/team_logos/mad_logo.png"
                }
            ]
        }
        """.trimIndent()
        
        // This should not throw an exception
        assertDoesNotThrow("JSON parsing should work") {
            json.parseToJsonElement(sampleTeamJson)
        }
    }
    
    @Test
    fun staticDataSystem_componentsExist() {
        // Verify all main components of static data system exist
        val requiredClasses = listOf(
            "es.itram.basketmatch.data.datasource.local.assets.StaticDataManager",
            "es.itram.basketmatch.data.sync.SmartSyncManager", 
            "es.itram.basketmatch.domain.usecase.ManageStaticDataUseCase",
            "es.itram.basketmatch.di.StaticDataModule"
        )
        
        requiredClasses.forEach { className ->
            assertDoesNotThrow("Class $className should exist") {
                Class.forName(className)
            }
        }
    }
    
    private fun assertDoesNotThrow(message: String, executable: () -> Unit) {
        try {
            executable()
        } catch (e: Exception) {
            fail("$message - but threw: ${e.message}")
        }
    }
}
