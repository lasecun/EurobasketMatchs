package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.domain.entity.Team
import org.junit.Test

class TeamMapperTest {

    @Test
    fun `when toDomain is called with TeamEntity, then returns correct Team`() {
        // Given
        val entity = TeamEntity(
            id = "1",
            name = "Real Madrid",
            city = "Madrid",
            country = "España",
            logoUrl = "https://example.com/logo.png",
            shortName = "RMA",
            code = "MAD",
            founded = 1902,
            coach = "Chus Mateo",
            isFavorite = true
        )

        // When
        val result = TeamMapper.toDomain(entity)

        // Then
        assertThat(result.id).isEqualTo("1")
        assertThat(result.name).isEqualTo("Real Madrid")
        assertThat(result.city).isEqualTo("Madrid")
        assertThat(result.country).isEqualTo("España")
        assertThat(result.logoUrl).isEqualTo("https://example.com/logo.png")
        assertThat(result.shortName).isEqualTo("RMA")
        assertThat(result.code).isEqualTo("MAD")
        assertThat(result.founded).isEqualTo(1902)
        assertThat(result.coach).isEqualTo("Chus Mateo")
        assertThat(result.isFavorite).isTrue()
    }

    @Test
    fun `when toEntity is called with Team, then returns correct TeamEntity`() {
        // Given
        val team = Team(
            id = "2",
            name = "FC Barcelona",
            city = "Barcelona",
            country = "España",
            logoUrl = "https://example.com/barca.png",
            shortName = "BAR",
            code = "FCB",
            founded = 1899,
            coach = "Joan Peñarroya",
            isFavorite = false
        )

        // When
        val result = TeamMapper.fromDomain(team)

        // Then
        assertThat(result.id).isEqualTo("2")
        assertThat(result.name).isEqualTo("FC Barcelona")
        assertThat(result.city).isEqualTo("Barcelona")
        assertThat(result.country).isEqualTo("España")
        assertThat(result.logoUrl).isEqualTo("https://example.com/barca.png")
        assertThat(result.shortName).isEqualTo("BAR")
        assertThat(result.code).isEqualTo("FCB")
        assertThat(result.founded).isEqualTo(1899)
        assertThat(result.coach).isEqualTo("Joan Peñarroya")
        assertThat(result.isFavorite).isFalse()
    }

    @Test
    fun `when mapping back and forth, then original values are preserved`() {
        // Given
        val originalTeam = Team(
            id = "3",
            name = "Anadolu Efes",
            city = "Istanbul",
            country = "Turkey",
            logoUrl = "https://example.com/efes.png",
            shortName = "AEF",
            code = "EFS",
            founded = 1976,
            coach = "Tomislav Mijatovic",
            isFavorite = true
        )

        // When
        val entity = TeamMapper.fromDomain(originalTeam)
        val mappedBackTeam = TeamMapper.toDomain(entity)

        // Then
        assertThat(mappedBackTeam).isEqualTo(originalTeam)
    }

    @Test
    fun `when mapping with null values, then handles gracefully`() {
        // Given
        val entityWithEmptyValues = TeamEntity(
            id = "4",
            name = "Test Team",
            city = "Test City",
            country = "Test Country",
            logoUrl = "",
            shortName = "",
            code = "",
            founded = 2000,
            coach = "",
            isFavorite = false
        )

        // When
        val result = TeamMapper.toDomain(entityWithEmptyValues)

        // Then
        assertThat(result.id).isEqualTo("4")
        assertThat(result.name).isEqualTo("Test Team")
        assertThat(result.city).isEqualTo("Test City")
        assertThat(result.country).isEqualTo("Test Country")
        assertThat(result.logoUrl).isEmpty()
        assertThat(result.shortName).isEmpty()
        assertThat(result.code).isEmpty()
        assertThat(result.founded).isEqualTo(2000)
        assertThat(result.coach).isEmpty()
        assertThat(result.isFavorite).isFalse()
    }
}
