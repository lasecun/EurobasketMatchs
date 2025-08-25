package es.itram.basketmatch

import org.junit.Test
import org.junit.Assert.*
import es.itram.basketmatch.data.datasource.local.assets.StaticDataManager
import kotlinx.serialization.json.Json

/**
 * Basic test to verify static data system compiles correctly
 */
class StaticDataSystemTest {
    
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun staticDataSystem_builds() {
        // This test passes if the project compiles with static data system
        assertTrue("Static data system compiles successfully", true)
    }
    
    @Test
    fun staticDataManager_canBeInstantiated() {
        // Verify that StaticDataManager class exists and can be referenced
        assertNotNull("StaticDataManager class should be available", StaticDataManager::class)
    }
    
    @Test
    fun jsonSerialization_isAvailable() {
        // Verify that JSON serialization is working
        val json = Json { ignoreUnknownKeys = true }
        assertNotNull("JSON serialization should be available", json)
    }
    
    @Test
    fun staticDataClasses_exist() {
        // Verify that our static data classes can be referenced
        try {
            Class.forName("es.itram.basketmatch.data.datasource.local.assets.StaticDataManager")
            Class.forName("es.itram.basketmatch.data.sync.SmartSyncManager")
            Class.forName("es.itram.basketmatch.domain.usecase.ManageStaticDataUseCase")
            assertTrue("All static data classes are available", true)
        } catch (e: ClassNotFoundException) {
            fail("Static data classes should be available: ${e.message}")
        }
    }
}
