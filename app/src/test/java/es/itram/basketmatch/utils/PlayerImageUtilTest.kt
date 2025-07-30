package es.itram.basketmatch.utils

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Test

class PlayerImageUtilTest {

    private val mockHttpClient = mockk<OkHttpClient>()
    private val playerImageUtil = PlayerImageUtil(mockHttpClient)

    @Test
    fun `generatePlaceholderImageUrl should generate correct URL for player name`() {
        // Arrange
        val playerName = "John Smith"

        // Act
        val result = playerImageUtil.generatePlaceholderImageUrl(playerName)

        // Assert
        assertThat(result).contains("JS")
        assertThat(result).startsWith("https://ui-avatars.com")
        assertThat(result).contains("background=004996")
    }

    @Test
    fun `generatePlaceholderImageUrl should handle single name correctly`() {
        // Arrange
        val playerName = "Madonna"

        // Act
        val result = playerImageUtil.generatePlaceholderImageUrl(playerName)

        // Assert
        assertThat(result).contains("M")
        assertThat(result).startsWith("https://ui-avatars.com")
    }

    @Test
    fun `generatePlaceholderImageUrl should handle empty name correctly`() {
        // Arrange
        val playerName = ""

        // Act
        val result = playerImageUtil.generatePlaceholderImageUrl(playerName)

        // Assert
        assertThat(result).isNotEmpty()
        assertThat(result).startsWith("https://ui-avatars.com")
    }

    @Test
    fun `generatePlaceholderImageUrl should handle multiple names correctly`() {
        // Arrange
        val playerName = "John Michael Smith Johnson"

        // Act
        val result = playerImageUtil.generatePlaceholderImageUrl(playerName)

        // Assert
        assertThat(result).contains("JM") // Only first two names
        assertThat(result).startsWith("https://ui-avatars.com")
    }

    @Test
    fun `getPlayerImageUrl should return null when http request fails`() = runTest {
        // Arrange
        val playerCode = "003733"
        val playerName = "Sergio Llull"
        val teamCode = "MAD"
        
        val mockResponse = mockk<Response>()
        coEvery { mockResponse.isSuccessful } returns false
        coEvery { mockHttpClient.newCall(any()).execute() } returns mockResponse

        // Act
        val result = playerImageUtil.getPlayerImageUrl(playerCode, playerName, teamCode)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `getPlayerImageUrl should return image URL when found in HTML`() = runTest {
        // Arrange
        val playerCode = "003733"
        val playerName = "Sergio Llull"
        val teamCode = "MAD"
        val expectedImageUrl = "https://example.com/player.png"
        val htmlContent = """{"photo":"$expectedImageUrl"}"""
        
        val mockResponseBody = mockk<ResponseBody>()
        val mockResponse = mockk<Response>()
        
        coEvery { mockResponseBody.string() } returns htmlContent
        coEvery { mockResponse.isSuccessful } returns true
        coEvery { mockResponse.body } returns mockResponseBody
        coEvery { mockHttpClient.newCall(any()).execute() } returns mockResponse

        // Act
        val result = playerImageUtil.getPlayerImageUrl(playerCode, playerName, teamCode)

        // Assert
        assertThat(result).isEqualTo(expectedImageUrl)
    }

    @Test
    fun `getPlayerImageUrl should return null when image not found in HTML`() = runTest {
        // Arrange
        val playerCode = "003733"
        val playerName = "Sergio Llull"
        val teamCode = "MAD"
        val htmlContent = """<html><body>No photo data</body></html>"""
        
        val mockResponseBody = mockk<ResponseBody>()
        val mockResponse = mockk<Response>()
        
        coEvery { mockResponseBody.string() } returns htmlContent
        coEvery { mockResponse.isSuccessful } returns true
        coEvery { mockResponse.body } returns mockResponseBody
        coEvery { mockHttpClient.newCall(any()).execute() } returns mockResponse

        // Act
        val result = playerImageUtil.getPlayerImageUrl(playerCode, playerName, teamCode)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `getPlayerImageUrl should handle exception gracefully`() = runTest {
        // Arrange
        val playerCode = "003733"
        val playerName = "Sergio Llull"
        val teamCode = "MAD"
        
        coEvery { mockHttpClient.newCall(any()).execute() } throws RuntimeException("Network error")

        // Act
        val result = playerImageUtil.getPlayerImageUrl(playerCode, playerName, teamCode)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `DEFAULT_PLAYER_IMAGES should contain expected entries`() {
        // Assert
        assertThat(PlayerImageUtil.DEFAULT_PLAYER_IMAGES).containsKey("003733")
        assertThat(PlayerImageUtil.DEFAULT_PLAYER_IMAGES["003733"]).contains("https://")
    }
}
