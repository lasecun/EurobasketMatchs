package es.itram.basketmatch.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayerImageUtilTest {

    @Test
    fun `getPlayerImageUrl should generate correct URL for valid personId`() {
        // Arrange
        val personId = "P001234"

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).contains(personId)
        assertThat(result).startsWith("https://")
        assertThat(result).contains("euroleaguebasketball.net")
    }

    @Test
    fun `getPlayerImageUrl should handle empty personId`() {
        // Arrange
        val personId = ""

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).isNotEmpty()
        assertThat(result).startsWith("https://")
    }

    @Test
    fun `getPlayerImageUrl should handle null personId`() {
        // Arrange
        val personId: String? = null

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).isNotEmpty()
        assertThat(result).startsWith("https://")
    }

    @Test
    fun `getPlayerImageUrl should handle personId with special characters`() {
        // Arrange
        val personId = "P-001_234"

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).contains(personId)
        assertThat(result).startsWith("https://")
    }

    @Test
    fun `getPlayerImageUrl should generate consistent URLs for same personId`() {
        // Arrange
        val personId = "P001234"

        // Act
        val result1 = PlayerImageUtil.getPlayerImageUrl(personId)
        val result2 = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result1).isEqualTo(result2)
    }

    @Test
    fun `getPlayerImageUrl should generate different URLs for different personIds`() {
        // Arrange
        val personId1 = "P001234"
        val personId2 = "P005678"

        // Act
        val result1 = PlayerImageUtil.getPlayerImageUrl(personId1)
        val result2 = PlayerImageUtil.getPlayerImageUrl(personId2)

        // Assert
        assertThat(result1).isNotEqualTo(result2)
        assertThat(result1).contains(personId1)
        assertThat(result2).contains(personId2)
    }

    @Test
    fun `getPlayerImageUrl should handle very long personId`() {
        // Arrange
        val personId = "P" + "0".repeat(100) + "1234"

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).contains(personId)
        assertThat(result).startsWith("https://")
    }

    @Test
    fun `getPlayerImageUrl should handle personId with numbers only`() {
        // Arrange
        val personId = "001234"

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).contains(personId)
        assertThat(result).startsWith("https://")
    }

    @Test
    fun `getPlayerImageUrl should handle personId with letters only`() {
        // Arrange
        val personId = "PLAYER"

        // Act
        val result = PlayerImageUtil.getPlayerImageUrl(personId)

        // Assert
        assertThat(result).contains(personId)
        assertThat(result).startsWith("https://")
    }

    @Test
    fun `getPlayerImageUrl should be case sensitive`() {
        // Arrange
        val personId1 = "p001234"
        val personId2 = "P001234"

        // Act
        val result1 = PlayerImageUtil.getPlayerImageUrl(personId1)
        val result2 = PlayerImageUtil.getPlayerImageUrl(personId2)

        // Assert
        assertThat(result1).isNotEqualTo(result2)
        assertThat(result1).contains(personId1)
        assertThat(result2).contains(personId2)
    }
}
