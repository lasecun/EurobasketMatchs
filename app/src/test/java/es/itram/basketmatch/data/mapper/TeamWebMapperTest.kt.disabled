package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.domain.entity.Team
import org.junit.Test

/**
 * Tests para TeamWebMapper - mapping de TeamWebDto a Team de dominio
 */
class TeamWebMapperTest {

    @Test
    fun `toDomain should map TeamWebDto to Team correctly with all fields`() {
        // Given
        val teamWebDto = TeamWebDto(
            id = "rea",
            name = "Real Madrid",
            fullName = "Real Madrid Basketball",
            shortCode = "RMD",
            logoUrl = "https://img.euroleaguebasketball.net/design/ec/logos/clubs/real-madrid.png",
            country = "Spain",
            venue = "WiZink Center",
            profileUrl = "https://www.realmadrid.com/basketball"
        )

        // When
        val result = TeamWebMapper.toDomain(teamWebDto)

        // Then
        assertThat(result.id).isEqualTo("rea")
        assertThat(result.name).isEqualTo("Real Madrid")
        assertThat(result.shortName).isEqualTo("REA") // Generado desde el nombre (3 primeros caracteres)
        assertThat(result.code).isEqualTo("rea") // Usa ID como código
        assertThat(result.city).isEmpty() // No disponible en DTO
        assertThat(result.country).isEqualTo("Spain")
        assertThat(result.logoUrl).isEqualTo("https://img.euroleaguebasketball.net/design/ec/logos/clubs/real-madrid.png")
        // Campos con valores por defecto
        assertThat(result.founded).isEqualTo(0)
        assertThat(result.coach).isEmpty()
        assertThat(result.isFavorite).isFalse()
    }

    @Test
    fun `toDomain should handle null optional fields correctly`() {
        // Given
        val teamWebDto = TeamWebDto(
            id = "fcb",
            name = "FC Barcelona",
            fullName = "FC Barcelona Basketball",
            shortCode = "BAR",
            logoUrl = null,
            country = null,
            venue = null,
            profileUrl = ""
        )

        // When
        val result = TeamWebMapper.toDomain(teamWebDto)

        // Then
        assertThat(result.id).isEqualTo("fcb")
        assertThat(result.name).isEqualTo("FC Barcelona")
        assertThat(result.shortName).isEqualTo("FC ") // 3 primeros caracteres del nombre
        assertThat(result.code).isEqualTo("fcb")
        assertThat(result.city).isEmpty()
        assertThat(result.country).isEmpty() // null se convierte a empty string
        assertThat(result.logoUrl).isEmpty() // null se convierte a empty string
    }

    @Test
    fun `toDomain should generate shortName from name with short team names`() {
        // Given
        val shortNameDto = TeamWebDto(
            id = "bas",
            name = "BK", // Nombre muy corto
            fullName = "Baskonia Vitoria-Gasteiz",
            shortCode = "BAS",
            logoUrl = "https://example.com/baskonia.png",
            country = "Spain"
        )

        // When
        val result = TeamWebMapper.toDomain(shortNameDto)

        // Then
        assertThat(result.shortName).isEqualTo("BK") // Solo 2 caracteres disponibles
        assertThat(result.name).isEqualTo("BK")
    }

    @Test
    fun `toDomain should handle empty strings gracefully`() {
        // Given
        val emptyFieldsDto = TeamWebDto(
            id = "",
            name = "",
            fullName = "",
            shortCode = "",
            logoUrl = "",
            country = "",
            venue = "",
            profileUrl = ""
        )

        // When
        val result = TeamWebMapper.toDomain(emptyFieldsDto)

        // Then
        assertThat(result.id).isEmpty()
        assertThat(result.name).isEmpty()
        assertThat(result.shortName).isEmpty()
        assertThat(result.code).isEmpty()
        assertThat(result.country).isEmpty()
        assertThat(result.logoUrl).isEmpty()
    }

    @Test
    fun `toDomain should handle special characters in team names`() {
        // Given
        val specialCharsDto = TeamWebDto(
            id = "žal",
            name = "Žalgiris Kaunas",
            fullName = "BC Žalgiris Kaunas",
            shortCode = "ZAL",
            logoUrl = "https://example.com/žalgiris.png",
            country = "Lithuania",
            venue = "Žalgiris Arena"
        )

        // When
        val result = TeamWebMapper.toDomain(specialCharsDto)

        // Then
        assertThat(result.id).isEqualTo("žal")
        assertThat(result.name).isEqualTo("Žalgiris Kaunas")
        assertThat(result.shortName).isEqualTo("ŽAL") // 3 primeros caracteres en mayúsculas
        assertThat(result.country).isEqualTo("Lithuania")
        assertThat(result.logoUrl).contains("žalgiris.png")
    }

    @Test
    fun `toDomain should handle very long team names`() {
        // Given
        val longNameDto = TeamWebDto(
            id = "long",
            name = "Very Long Team Name Basketball Club Association",
            fullName = "Very Long Team Name Basketball Club Association Professional Team",
            shortCode = "VLT",
            logoUrl = "https://example.com/very-long-team.png",
            country = "Test Country"
        )

        // When
        val result = TeamWebMapper.toDomain(longNameDto)

        // Then
        assertThat(result.shortName).isEqualTo("VER") // Solo los primeros 3 caracteres
        assertThat(result.name).isEqualTo("Very Long Team Name Basketball Club Association")
    }

    @Test
    fun `toDomainList should convert empty list correctly`() {
        // Given
        val emptyList = emptyList<TeamWebDto>()

        // When
        val result = TeamWebMapper.toDomainList(emptyList)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `toDomainList should convert single team correctly`() {
        // Given
        val singleTeamList = listOf(
            TeamWebDto(
                id = "oly",
                name = "Olympiacos",
                fullName = "Olympiacos B.C.",
                shortCode = "OLY",
                logoUrl = "https://example.com/olympiacos.png",
                country = "Greece",
                venue = "Peace and Friendship Stadium"
            )
        )

        // When
        val result = TeamWebMapper.toDomainList(singleTeamList)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("oly")
        assertThat(result[0].name).isEqualTo("Olympiacos")
        assertThat(result[0].shortName).isEqualTo("OLY")
        assertThat(result[0].country).isEqualTo("Greece")
    }

    @Test
    fun `toDomainList should convert multiple teams correctly`() {
        // Given
        val multipleTeamsList = listOf(
            TeamWebDto(
                id = "pan",
                name = "Panathinaikos",
                fullName = "Panathinaikos AKTOR Athens",
                shortCode = "PAO",
                logoUrl = "https://example.com/panathinaikos.png",
                country = "Greece",
                venue = "OAKA"
            ),
            TeamWebDto(
                id = "mta",
                name = "Maccabi Tel Aviv",
                fullName = "Maccabi Playtika Tel Aviv",
                shortCode = "MTA",
                logoUrl = "https://example.com/maccabi.png",
                country = "Israel",
                venue = "Menora Mivtachim Arena"
            ),
            TeamWebDto(
                id = "efs",
                name = "Anadolu Efes",
                fullName = "Anadolu Efes Istanbul",
                shortCode = "EFS",
                logoUrl = "https://example.com/efes.png",
                country = "Turkey",
                venue = "Sinan Erdem Dome"
            )
        )

        // When
        val result = TeamWebMapper.toDomainList(multipleTeamsList)

        // Then
        assertThat(result).hasSize(3)
        
        // Verificar primer equipo
        assertThat(result[0].id).isEqualTo("pan")
        assertThat(result[0].name).isEqualTo("Panathinaikos")
        assertThat(result[0].shortName).isEqualTo("PAN")
        assertThat(result[0].country).isEqualTo("Greece")
        
        // Verificar segundo equipo
        assertThat(result[1].id).isEqualTo("mta")
        assertThat(result[1].name).isEqualTo("Maccabi Tel Aviv")
        assertThat(result[1].shortName).isEqualTo("MAC")
        assertThat(result[1].country).isEqualTo("Israel")
        
        // Verificar tercer equipo
        assertThat(result[2].id).isEqualTo("efs")
        assertThat(result[2].name).isEqualTo("Anadolu Efes")
        assertThat(result[2].shortName).isEqualTo("ANA")
        assertThat(result[2].country).isEqualTo("Turkey")
    }

    @Test
    fun `toDomainList should handle mixed null and valid fields`() {
        // Given
        val mixedList = listOf(
            TeamWebDto(
                id = "valid",
                name = "Valid Team",
                fullName = "Valid Team Full Name",
                shortCode = "VAL",
                logoUrl = "https://example.com/valid.png",
                country = "Valid Country",
                venue = "Valid Arena"
            ),
            TeamWebDto(
                id = "nulls",
                name = "Team With Nulls",
                fullName = "Team With Nulls Full Name",
                shortCode = "TWN",
                logoUrl = null,
                country = null,
                venue = null
            )
        )

        // When
        val result = TeamWebMapper.toDomainList(mixedList)

        // Then
        assertThat(result).hasSize(2)
        
        // Equipo con campos válidos
        assertThat(result[0].logoUrl).isEqualTo("https://example.com/valid.png")
        assertThat(result[0].country).isEqualTo("Valid Country")
        
        // Equipo con campos null
        assertThat(result[1].logoUrl).isEmpty()
        assertThat(result[1].country).isEmpty()
    }

    @Test
    fun `toDomain should handle complex URLs correctly`() {
        // Given
        val complexUrlDto = TeamWebDto(
            id = "complex",
            name = "Complex URL Team",
            fullName = "Complex URL Team Full",
            shortCode = "CUT",
            logoUrl = "https://cdn.euroleaguebasketball.net/media/cache/optimize_logo/design/ec/logos/clubs/2024-25/team-logo-high-res.png?v=2.1.0&timestamp=1234567890&format=webp",
            country = "Test Country",
            venue = "Complex Arena",
            profileUrl = "https://www.team.com/es/basketball/teams/euroleague/2024-25/roster?section=stats&filter=all&sort=desc"
        )

        // When
        val result = TeamWebMapper.toDomain(complexUrlDto)

        // Then
        assertThat(result.logoUrl).contains("cdn.euroleaguebasketball.net")
        assertThat(result.logoUrl).contains("timestamp=1234567890")
        assertThat(result.logoUrl).contains("format=webp")
        assertThat(result.name).isEqualTo("Complex URL Team")
    }

    @Test
    fun `toDomain should preserve team ID case sensitivity`() {
        // Given
        val caseSensitiveDto = TeamWebDto(
            id = "CaSeSeNsItIvE",
            name = "Case Sensitive Team",
            fullName = "Case Sensitive Team Full Name",
            shortCode = "CST",
            logoUrl = "https://example.com/case.png",
            country = "Test"
        )

        // When
        val result = TeamWebMapper.toDomain(caseSensitiveDto)

        // Then
        assertThat(result.id).isEqualTo("CaSeSeNsItIvE") // Preserva el case original
        assertThat(result.code).isEqualTo("CaSeSeNsItIvE") // Preserva el case original
        assertThat(result.shortName).isEqualTo("CAS") // shortName se genera en mayúsculas
    }
}
