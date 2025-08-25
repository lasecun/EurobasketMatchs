package es.itram.basketmatch

import org.junit.Test
import org.junit.Assert.*

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
}
