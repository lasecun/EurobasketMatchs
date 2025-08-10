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
    fun `when fromDomain is called with Team, then returns correct TeamEntity`() {
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
    fun `when toDomainList with empty list, then returns empty list`() {
        // Given
        val emptyList = emptyList<TeamEntity>()

        // When
        val result = TeamMapper.toDomainList(emptyList)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `when fromDomainList with empty list, then returns empty list`() {
        // Given
        val emptyList = emptyList<Team>()

        // When
        val result = TeamMapper.fromDomainList(emptyList)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `when toDomainList with multiple teams, then maps all correctly`() {
        // Given
        val teamEntities = listOf(
            TeamEntity(
                id = "rea",
                name = "Real Madrid",
                shortName = "Madrid",
                code = "RMD",
                city = "Madrid",
                country = "España",
                logoUrl = "https://example.com/real.png",
                founded = 1902,
                coach = "Chus Mateo",
                isFavorite = true
            ),
            TeamEntity(
                id = "fcb",
                name = "FC Barcelona",
                shortName = "Barça",
                code = "BAR",
                city = "Barcelona",
                country = "España",
                logoUrl = "https://example.com/barca.png",
                founded = 1899,
                coach = "Joan Peñarroya",
                isFavorite = false
            )
        )

        // When
        val result = TeamMapper.toDomainList(teamEntities)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Real Madrid")
        assertThat(result[1].name).isEqualTo("FC Barcelona")
    }

    @Test
    fun `when fromDomainList with multiple teams, then maps all correctly`() {
        // Given
        val teams = listOf(
            Team(
                id = "oly",
                name = "Olympiacos",
                shortName = "Oly",
                code = "OLY",
                city = "Piraeus",
                country = "Greece",
                logoUrl = "https://example.com/oly.png",
                founded = 1925,
                coach = "Georgios Bartzokas",
                isFavorite = false
            ),
            Team(
                id = "pan",
                name = "Panathinaikos",
                shortName = "Pao",
                code = "PAO",
                city = "Athens",
                country = "Greece",
                logoUrl = "https://example.com/pao.png",
                founded = 1908,
                coach = "Ergin Ataman",
                isFavorite = true
            )
        )

        // When
        val result = TeamMapper.fromDomainList(teams)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Olympiacos")
        assertThat(result[1].name).isEqualTo("Panathinaikos")
    }

    @Test
    fun `when round trip conversion with list, then preserves all data`() {
        // Given
        val originalEntities = listOf(
            TeamEntity(
                id = "vir",
                name = "Virtus Segafredo Bologna",
                shortName = "Virtus",
                code = "VIR",
                city = "Bologna",
                country = "Italia",
                logoUrl = "https://example.com/virtus.png",
                founded = 1929,
                coach = "Luca Banchi",
                isFavorite = false
            )
        )

        // When - conversión de ida y vuelta
        val domains = TeamMapper.toDomainList(originalEntities)
        val backToEntities = TeamMapper.fromDomainList(domains)

        // Then - debe preservar orden y datos
        assertThat(backToEntities).hasSize(originalEntities.size)
        assertThat(backToEntities).isEqualTo(originalEntities)
    }
}