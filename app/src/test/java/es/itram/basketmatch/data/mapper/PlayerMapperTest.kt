package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import org.junit.Test

class PlayerMapperTest {

    private val playerDto = PlayerDto(
        personId = "P001234",
        name = "John",
        surname = "Smith",
        position = "Forward",
        dorsal = "10",
        height = "200",
        weight = "85",
        birthDate = "1995-06-15",
        birthPlace = "Madrid, Spain",
        nationality = "Spanish",
        experience = "5",
        captain = "Y"
    )

    @Test
    fun `toDomainModel should convert PlayerDto to Player correctly`() {
        // Act
        val result = playerDto.toDomainModel()

        // Assert
        assertThat(result.personId).isEqualTo("P001234")
        assertThat(result.name).isEqualTo("John")
        assertThat(result.surname).isEqualTo("Smith")
        assertThat(result.position).isEqualTo("Forward")
        assertThat(result.dorsal).isEqualTo("10")
        assertThat(result.height).isEqualTo("200")
        assertThat(result.weight).isEqualTo("85")
        assertThat(result.birthDate).isEqualTo("1995-06-15")
        assertThat(result.birthPlace).isEqualTo("Madrid, Spain")
        assertThat(result.nationality).isEqualTo("Spanish")
        assertThat(result.experience).isEqualTo("5")
        assertThat(result.captain).isTrue()
    }

    @Test
    fun `toDomainModel should handle captain field Y as true`() {
        // Arrange
        val playerWithCaptainY = playerDto.copy(captain = "Y")

        // Act
        val result = playerWithCaptainY.toDomainModel()

        // Assert
        assertThat(result.captain).isTrue()
    }

    @Test
    fun `toDomainModel should handle captain field N as false`() {
        // Arrange
        val playerWithCaptainN = playerDto.copy(captain = "N")

        // Act
        val result = playerWithCaptainN.toDomainModel()

        // Assert
        assertThat(result.captain).isFalse()
    }

    @Test
    fun `toDomainModel should handle captain field empty string as false`() {
        // Arrange
        val playerWithEmptyCaptain = playerDto.copy(captain = "")

        // Act
        val result = playerWithEmptyCaptain.toDomainModel()

        // Assert
        assertThat(result.captain).isFalse()
    }

    @Test
    fun `toDomainModel should handle captain field null as false`() {
        // Arrange
        val playerWithNullCaptain = playerDto.copy(captain = null)

        // Act
        val result = playerWithNullCaptain.toDomainModel()

        // Assert
        assertThat(result.captain).isFalse()
    }

    @Test
    fun `toDomainModel should handle all null fields gracefully`() {
        // Arrange
        val playerWithNulls = PlayerDto(
            personId = null,
            name = null,
            surname = null,
            position = null,
            dorsal = null,
            height = null,
            weight = null,
            birthDate = null,
            birthPlace = null,
            nationality = null,
            experience = null,
            captain = null
        )

        // Act
        val result = playerWithNulls.toDomainModel()

        // Assert
        assertThat(result.personId).isEqualTo("")
        assertThat(result.name).isEqualTo("")
        assertThat(result.surname).isEqualTo("")
        assertThat(result.position).isEqualTo("")
        assertThat(result.dorsal).isEqualTo("")
        assertThat(result.height).isEqualTo("")
        assertThat(result.weight).isEqualTo("")
        assertThat(result.birthDate).isEqualTo("")
        assertThat(result.birthPlace).isEqualTo("")
        assertThat(result.nationality).isEqualTo("")
        assertThat(result.experience).isEqualTo("")
        assertThat(result.captain).isFalse()
    }

    @Test
    fun `toDomainModel should generate correct imageUrl`() {
        // Act
        val result = playerDto.toDomainModel()

        // Assert
        assertThat(result.imageUrl).contains("P001234")
        assertThat(result.imageUrl).startsWith("https://")
    }

    @Test
    fun `toDomainModel should handle empty personId for imageUrl`() {
        // Arrange
        val playerWithEmptyId = playerDto.copy(personId = "")

        // Act
        val result = playerWithEmptyId.toDomainModel()

        // Assert
        assertThat(result.imageUrl).isNotEmpty()
    }
}
