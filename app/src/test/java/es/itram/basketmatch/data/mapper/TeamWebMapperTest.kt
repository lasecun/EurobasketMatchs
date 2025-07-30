package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import org.junit.Test

class TeamWebMapperTest {

    private val teamWebDto = TeamWebDto(
        id = "T001",
        name = "Real Madrid",
        fullName = "Real Madrid Baloncesto",
        shortCode = "MAD",
        logoUrl = "https://example.com/madrid.png",
        country = "España",
        venue = "WiZink Center",
        profileUrl = "https://example.com/profile"
    )

    @Test
    fun `toDomainModel should convert TeamWebDto to Team correctly`() {
        // Act
        val result = teamWebDto.toDomainModel()

        // Assert
        assertThat(result.id).isEqualTo("T001")
        assertThat(result.name).isEqualTo("Real Madrid")
        assertThat(result.shortName).isEqualTo("Real Madrid Baloncesto")
        assertThat(result.code).isEqualTo("MAD")
        assertThat(result.logoUrl).isEqualTo("https://example.com/madrid.png")
        assertThat(result.country).isEqualTo("España")
        assertThat(result.city).isEmpty()
        assertThat(result.isFavorite).isFalse()
    }

    @Test
    fun `toDomainModel should handle null values correctly`() {
        // Arrange
        val teamWithNulls = teamWebDto.copy(
            logoUrl = null,
            country = null,
            venue = null
        )

        // Act
        val result = teamWithNulls.toDomainModel()

        // Assert
        assertThat(result.logoUrl).isEmpty()
        assertThat(result.country).isEmpty()
    }

    @Test
    fun `toDomainModel should handle empty strings correctly`() {
        // Arrange
        val teamWithEmptyStrings = teamWebDto.copy(
            logoUrl = "",
            country = "",
            venue = ""
        )

        // Act
        val result = teamWithEmptyStrings.toDomainModel()

        // Assert
        assertThat(result.logoUrl).isEmpty()
        assertThat(result.country).isEmpty()
    }
}
