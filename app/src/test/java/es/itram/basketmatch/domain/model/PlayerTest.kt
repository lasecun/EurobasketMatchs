package es.itram.basketmatch.domain.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para Player domain model
 */
class PlayerTest {

    @Test
    fun `player creation with all parameters should work correctly`() {
        // Given
        val player = createSamplePlayer()

        // Then
        assertEquals("P001", player.code)
        assertEquals("Luka", player.name)
        assertEquals("Doncic", player.surname)
        assertEquals("Luka Doncic", player.fullName)
        assertEquals(7, player.jersey)
        assertEquals(PlayerPosition.POINT_GUARD, player.position)
        assertEquals("201cm", player.height)
        assertEquals("104kg", player.weight)
        assertEquals("1999-02-28", player.dateOfBirth)
        assertEquals("Ljubljana, Slovenia", player.placeOfBirth)
        assertEquals("Slovenia", player.nationality)
        assertEquals(5, player.experience)
        assertNull(player.profileImageUrl)
        assertTrue(player.isActive)
        assertFalse(player.isStarter)
        assertFalse(player.isCaptain)
    }

    @Test
    fun `player with different positions should work correctly`() {
        val positions = listOf(
            PlayerPosition.POINT_GUARD,
            PlayerPosition.SHOOTING_GUARD,
            PlayerPosition.SMALL_FORWARD,
            PlayerPosition.POWER_FORWARD,
            PlayerPosition.CENTER
        )

        positions.forEach { position ->
            val player = createSamplePlayer().copy(position = position)
            assertEquals("Position should be set correctly", position, player.position)
        }
    }

    @Test
    fun `player with starter flag should work correctly`() {
        // Given
        val starterPlayer = createSamplePlayer().copy(isStarter = true)
        val benchPlayer = createSamplePlayer().copy(isStarter = false)

        // Then
        assertTrue("Starter player should be marked as starter", starterPlayer.isStarter)
        assertFalse("Bench player should not be marked as starter", benchPlayer.isStarter)
    }

    @Test
    fun `player with captain flag should work correctly`() {
        // Given
        val captainPlayer = createSamplePlayer().copy(isCaptain = true)
        val regularPlayer = createSamplePlayer().copy(isCaptain = false)

        // Then
        assertTrue("Captain player should be marked as captain", captainPlayer.isCaptain)
        assertFalse("Regular player should not be marked as captain", regularPlayer.isCaptain)
    }

    @Test
    fun `player with inactive status should work correctly`() {
        // Given
        val activePlayer = createSamplePlayer().copy(isActive = true)
        val inactivePlayer = createSamplePlayer().copy(isActive = false)

        // Then
        assertTrue("Active player should be marked as active", activePlayer.isActive)
        assertFalse("Inactive player should not be marked as active", inactivePlayer.isActive)
    }

    @Test
    fun `player with profile image URL should work correctly`() {
        // Given
        val imageUrl = "https://example.com/player.jpg"
        val playerWithImage = createSamplePlayer().copy(profileImageUrl = imageUrl)
        val playerWithoutImage = createSamplePlayer().copy(profileImageUrl = null)

        // Then
        assertEquals("Player should have image URL", imageUrl, playerWithImage.profileImageUrl)
        assertNull("Player should not have image URL", playerWithoutImage.profileImageUrl)
    }

    @Test
    fun `player with zero experience should work correctly`() {
        // Given
        val rookiePlayer = createSamplePlayer().copy(experience = 0)

        // Then
        assertEquals("Rookie player should have zero experience", 0, rookiePlayer.experience)
    }

    @Test
    fun `player with high experience should work correctly`() {
        // Given
        val veteranPlayer = createSamplePlayer().copy(experience = 15)

        // Then
        assertEquals("Veteran player should have high experience", 15, veteranPlayer.experience)
    }

    @Test
    fun `player equality should work correctly`() {
        // Given
        val player1 = createSamplePlayer()
        val player2 = createSamplePlayer()
        val differentPlayer = createSamplePlayer().copy(code = "P002")

        // Then
        assertEquals("Same players should be equal", player1, player2)
        assertNotEquals("Different players should not be equal", player1, differentPlayer)
    }

    @Test
    fun `player hash code should be consistent`() {
        // Given
        val player1 = createSamplePlayer()
        val player2 = createSamplePlayer()

        // Then
        assertEquals("Same players should have same hash code", player1.hashCode(), player2.hashCode())
    }

    @Test
    fun `player copy should create new instance with changes`() {
        // Given
        val originalPlayer = createSamplePlayer()
        
        // When
        val copiedPlayer = originalPlayer.copy(
            name = "Modified",
            jersey = 99,
            isStarter = true
        )

        // Then
        assertEquals("Original code should remain", originalPlayer.code, copiedPlayer.code)
        assertEquals("Name should be modified", "Modified", copiedPlayer.name)
        assertEquals("Jersey should be modified", 99, copiedPlayer.jersey)
        assertTrue("Should be starter", copiedPlayer.isStarter)
        
        // Original should be unchanged
        assertEquals("Original name should remain", "Luka", originalPlayer.name)
        assertEquals("Original jersey should remain", 7, originalPlayer.jersey)
        assertFalse("Original should not be starter", originalPlayer.isStarter)
    }

    @Test
    fun `player with empty strings should work correctly`() {
        // Given
        val playerWithEmptyFields = createSamplePlayer().copy(
            placeOfBirth = "",
            nationality = "",
            height = "",
            weight = ""
        )

        // Then
        assertEquals("Place of birth should be empty", "", playerWithEmptyFields.placeOfBirth)
        assertEquals("Nationality should be empty", "", playerWithEmptyFields.nationality)
        assertEquals("Height should be empty", "", playerWithEmptyFields.height)
        assertEquals("Weight should be empty", "", playerWithEmptyFields.weight)
    }

    private fun createSamplePlayer(): Player {
        return Player(
            code = "P001",
            name = "Luka",
            surname = "Doncic",
            fullName = "Luka Doncic",
            jersey = 7,
            position = PlayerPosition.POINT_GUARD,
            height = "201cm",
            weight = "104kg",
            dateOfBirth = "1999-02-28",
            placeOfBirth = "Ljubljana, Slovenia",
            nationality = "Slovenia",
            experience = 5,
            profileImageUrl = null,
            isActive = true,
            isStarter = false,
            isCaptain = false
        )
    }
}
