package es.itram.basketmatch.domain.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para PlayerPosition enum
 */
class PlayerPositionTest {

    @Test
    fun `all player positions have correct display names`() {
        // Test each position has a non-empty display name
        PlayerPosition.entries.forEach { position ->
            assertFalse(
                "Position ${position.name} should have non-empty display name",
                position.displayName.isBlank()
            )
        }
    }

    @Test
    fun `point guard position has correct display name`() {
        assertEquals("Base", PlayerPosition.POINT_GUARD.displayName)
    }

    @Test
    fun `shooting guard position has correct display name`() {
        assertEquals("Escolta", PlayerPosition.SHOOTING_GUARD.displayName)
    }

    @Test
    fun `small forward position has correct display name`() {
        assertEquals("Alero", PlayerPosition.SMALL_FORWARD.displayName)
    }

    @Test
    fun `power forward position has correct display name`() {
        assertEquals("Ala-Pívot", PlayerPosition.POWER_FORWARD.displayName)
    }

    @Test
    fun `center position has correct display name`() {
        assertEquals("Pívot", PlayerPosition.CENTER.displayName)
    }

    @Test
    fun `position values should be distinct`() {
        val positions = PlayerPosition.entries
        val uniquePositions = positions.distinct()
        
        assertEquals(
            "All positions should be unique",
            positions.size,
            uniquePositions.size
        )
    }

    @Test
    fun `position display names should be distinct`() {
        val displayNames = PlayerPosition.entries.map { it.displayName }
        val uniqueDisplayNames = displayNames.distinct()
        
        assertEquals(
            "All display names should be unique",
            displayNames.size,
            uniqueDisplayNames.size
        )
    }

    @Test
    fun `should have exactly 8 positions`() {
        assertEquals("Should have 8 basketball positions", 8, PlayerPosition.entries.size)
    }

    @Test
    fun `all positions should be accessible by name`() {
        // Test that we can access all positions by their enum name
        assertEquals(PlayerPosition.POINT_GUARD, PlayerPosition.valueOf("POINT_GUARD"))
        assertEquals(PlayerPosition.SHOOTING_GUARD, PlayerPosition.valueOf("SHOOTING_GUARD"))
        assertEquals(PlayerPosition.SMALL_FORWARD, PlayerPosition.valueOf("SMALL_FORWARD"))
        assertEquals(PlayerPosition.POWER_FORWARD, PlayerPosition.valueOf("POWER_FORWARD"))
        assertEquals(PlayerPosition.CENTER, PlayerPosition.valueOf("CENTER"))
    }

    @Test
    fun `valueOf with invalid name should throw exception`() {
        try {
            PlayerPosition.valueOf("INVALID_POSITION")
            fail("Should throw IllegalArgumentException for invalid position name")
        } catch (e: IllegalArgumentException) {
            // Expected behavior
            assertTrue("Exception message should contain the invalid name", 
                e.message?.contains("INVALID_POSITION") == true)
        }
    }

    @Test
    fun `ordinal values should be consistent`() {
        // Test that ordinal values are as expected (useful for database storage)
        assertEquals(0, PlayerPosition.POINT_GUARD.ordinal)
        assertEquals(1, PlayerPosition.SHOOTING_GUARD.ordinal)
        assertEquals(2, PlayerPosition.SMALL_FORWARD.ordinal)
        assertEquals(3, PlayerPosition.POWER_FORWARD.ordinal)
        assertEquals(4, PlayerPosition.CENTER.ordinal)
    }

    @Test
    fun `toString should return position name`() {
        assertEquals("POINT_GUARD", PlayerPosition.POINT_GUARD.toString())
        assertEquals("SHOOTING_GUARD", PlayerPosition.SHOOTING_GUARD.toString())
        assertEquals("SMALL_FORWARD", PlayerPosition.SMALL_FORWARD.toString())
        assertEquals("POWER_FORWARD", PlayerPosition.POWER_FORWARD.toString())
        assertEquals("CENTER", PlayerPosition.CENTER.toString())
    }
}
