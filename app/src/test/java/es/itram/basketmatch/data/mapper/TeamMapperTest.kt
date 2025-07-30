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

    @Test
    fun `when toDomain with all optional fields filled, then maps correctly`() {
        // Given - TeamEntity con todos los campos opcionales
        val teamEntity = TeamEntity(
            id = "oly",
            name = "Olympiacos Piraeus",
            shortName = "Olympiacos",
            code = "OLY",
            city = "Piraeus",
            country = "Grecia",
            logoUrl = "https://img.euroleaguebasketball.net/design/ec/logos/clubs/olympiacos.png",
            founded = 1925,
            coach = "Georgios Bartzokas",
            website = "https://www.olympiacos.org/",
            primaryColor = "#DC143C",
            secondaryColor = "#FFFFFF",
            isFavorite = true
        )

        // When
        val result = TeamMapper.toDomain(teamEntity)

        // Then - verificar que todos los campos opcionales se mapean
        assertThat(result.website).isEqualTo("https://www.olympiacos.org/")
        assertThat(result.primaryColor).isEqualTo("#DC143C")
        assertThat(result.secondaryColor).isEqualTo("#FFFFFF")
        assertThat(result.founded).isEqualTo(1925)
        assertThat(result.coach).isEqualTo("Georgios Bartzokas")
    }

    @Test
    fun `when fromDomain with all optional fields filled, then maps correctly`() {
        // Given - Team con todos los campos opcionales
        val team = Team(
            id = "pan",
            name = "Panathinaikos AKTOR Athens",
            shortName = "Panathinaikos",
            code = "PAN",
            city = "Athens",
            country = "Grecia",
            logoUrl = "https://img.euroleaguebasketball.net/design/ec/logos/clubs/panathinaikos.png",
            founded = 1908,
            coach = "Ergin Ataman",
            website = "https://www.paobc.gr/",
            primaryColor = "#006400",
            secondaryColor = "#FFFFFF",
            isFavorite = false
        )

        // When
        val result = TeamMapper.fromDomain(team)

        // Then - verificar que todos los campos opcionales se mapean
        assertThat(result.website).isEqualTo("https://www.paobc.gr/")
        assertThat(result.primaryColor).isEqualTo("#006400")
        assertThat(result.secondaryColor).isEqualTo("#FFFFFF")
        assertThat(result.founded).isEqualTo(1908)
        assertThat(result.coach).isEqualTo("Ergin Ataman")
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
            ),
            TeamEntity(
                id = "bas",
                name = "Baskonia Vitoria-Gasteiz",
                shortName = "Baskonia",
                code = "BAS",
                city = "Vitoria-Gasteiz",
                country = "España",
                logoUrl = "https://example.com/baskonia.png",
                founded = 1959,
                coach = "Pablo Laso",
                isFavorite = false
            )
        )

        // When
        val result = TeamMapper.toDomainList(teamEntities)

        // Then
        assertThat(result).hasSize(3)
        assertThat(result[0].id).isEqualTo("rea")
        assertThat(result[0].name).isEqualTo("Real Madrid")
        assertThat(result[0].isFavorite).isTrue()
        assertThat(result[1].id).isEqualTo("fcb")
        assertThat(result[1].name).isEqualTo("FC Barcelona")
        assertThat(result[1].isFavorite).isFalse()
        assertThat(result[2].id).isEqualTo("bas")
        assertThat(result[2].name).isEqualTo("Baskonia Vitoria-Gasteiz")
        assertThat(result[2].founded).isEqualTo(1959)
    }

    @Test
    fun `when fromDomainList with multiple teams, then maps all correctly`() {
        // Given
        val teams = listOf(
            Team(
                id = "ulk",
                name = "Fenerbahce Beko Istanbul",
                shortName = "Fenerbahce",
                code = "ULK",
                city = "Istanbul",
                country = "Turquía",
                logoUrl = "https://example.com/fener.png",
                founded = 1907,
                coach = "Saras Jasikevicius",
                isFavorite = false
            ),
            Team(
                id = "mta",
                name = "Maccabi Playtika Tel Aviv",
                shortName = "Maccabi",
                code = "MTA",
                city = "Tel Aviv",
                country = "Israel",
                logoUrl = "https://example.com/maccabi.png",
                founded = 1932,
                coach = "Oded Kattash",
                isFavorite = true
            )
        )

        // When
        val result = TeamMapper.fromDomainList(teams)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("ulk")
        assertThat(result[0].name).isEqualTo("Fenerbahce Beko Istanbul")
        assertThat(result[0].isFavorite).isFalse()
        assertThat(result[1].id).isEqualTo("mta")
        assertThat(result[1].name).isEqualTo("Maccabi Playtika Tel Aviv")
        assertThat(result[1].isFavorite).isTrue()
    }

    @Test
    fun `when mapping list back and forth, then preserves order and data`() {
        // Given - lista original de entities
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
                website = "https://www.virtus.it/",
                primaryColor = "#000000",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            ),
            TeamEntity(
                id = "red",
                name = "EA7 Emporio Armani Milan",
                shortName = "Milan",
                code = "MIL",
                city = "Milano",
                country = "Italia",
                logoUrl = "https://example.com/milan.png",
                founded = 1936,
                coach = "Ettore Messina",
                website = "https://www.olimpiamilano.com/",
                primaryColor = "#FF0000",
                secondaryColor = "#FFFFFF",
                isFavorite = true
            )
        )

        // When - conversión de ida y vuelta
        val domains = TeamMapper.toDomainList(originalEntities)
        val backToEntities = TeamMapper.fromDomainList(domains)

        // Then - debe preservar orden y datos
        assertThat(backToEntities).hasSize(originalEntities.size)
        assertThat(backToEntities).isEqualTo(originalEntities)
    }

    @Test
    fun `when mapping with special characters, then preserves them correctly`() {
        // Given - TeamEntity con caracteres especiales
        val teamEntity = TeamEntity(
            id = "test_special",
            name = "Žalgiris Kaunas Ñoño's & Co.",
            shortName = "Žalgiris",
            code = "ŽAL",
            city = "São Paulo",
            country = "España/França",
            logoUrl = "https://example.com/logo.png?v=1.0&size=large",
            founded = 1944,
            coach = "José María Fernández-González",
            website = "https://team.com/path?param=value&other=test",
            primaryColor = "#228B22",
            secondaryColor = "#FFFFFF",
            isFavorite = true
        )

        // When - conversión bidireccional
        val domain = TeamMapper.toDomain(teamEntity)
        val backToEntity = TeamMapper.fromDomain(domain)

        // Then - caracteres especiales preservados
        assertThat(domain.name).isEqualTo("Žalgiris Kaunas Ñoño's & Co.")
        assertThat(domain.shortName).isEqualTo("Žalgiris")
        assertThat(domain.code).isEqualTo("ŽAL")
        assertThat(domain.city).isEqualTo("São Paulo")
        assertThat(domain.country).isEqualTo("España/França")
        assertThat(domain.coach).isEqualTo("José María Fernández-González")
        assertThat(backToEntity).isEqualTo(teamEntity)
    }

    @Test
    fun `when mapping with default values, then uses correct defaults`() {
        // Given - TeamEntity con valores por defecto
        val teamEntity = TeamEntity(
            id = "default_test",
            name = "Default Test Team",
            shortName = "Default",
            code = "DEF",
            city = "Default City",
            country = "Default Country",
            logoUrl = "https://example.com/default.png"
            // founded, coach, website, colors, isFavorite usan valores por defecto
        )

        // When
        val result = TeamMapper.toDomain(teamEntity)

        // Then - verificar valores por defecto
        assertThat(result.founded).isEqualTo(0)
        assertThat(result.coach).isEqualTo("")
        assertThat(result.website).isEqualTo("")
        assertThat(result.primaryColor).isEqualTo("")
        assertThat(result.secondaryColor).isEqualTo("")
        assertThat(result.isFavorite).isFalse()
    }

    @Test
    fun `when mapping single team with full data, then round trip conversion preserves all fields`() {
        // Given - TeamEntity completo con todos los campos
        val completeEntity = TeamEntity(
            id = "complete",
            name = "Complete Test Team Ltd.",
            shortName = "Complete",
            code = "CMP",
            city = "Complete City",
            country = "Complete Country",
            logoUrl = "https://cdn.example.com/logos/complete-team-logo.png",
            founded = 1985,
            coach = "Complete Coach Name",
            website = "https://www.completeteam.com/official",
            primaryColor = "#123456",
            secondaryColor = "#ABCDEF",
            isFavorite = true
        )

        // When - conversión completa de ida y vuelta
        val domain = TeamMapper.toDomain(completeEntity)
        val backToEntity = TeamMapper.fromDomain(domain)

        // Then - todos los campos deben coincidir exactamente
        assertThat(backToEntity.id).isEqualTo(completeEntity.id)
        assertThat(backToEntity.name).isEqualTo(completeEntity.name)
        assertThat(backToEntity.shortName).isEqualTo(completeEntity.shortName)
        assertThat(backToEntity.code).isEqualTo(completeEntity.code)
        assertThat(backToEntity.city).isEqualTo(completeEntity.city)
        assertThat(backToEntity.country).isEqualTo(completeEntity.country)
        assertThat(backToEntity.logoUrl).isEqualTo(completeEntity.logoUrl)
        assertThat(backToEntity.founded).isEqualTo(completeEntity.founded)
        assertThat(backToEntity.coach).isEqualTo(completeEntity.coach)
        assertThat(backToEntity.website).isEqualTo(completeEntity.website)
        assertThat(backToEntity.primaryColor).isEqualTo(completeEntity.primaryColor)
        assertThat(backToEntity.secondaryColor).isEqualTo(completeEntity.secondaryColor)
        assertThat(backToEntity.isFavorite).isEqualTo(completeEntity.isFavorite)
        assertThat(backToEntity).isEqualTo(completeEntity)
    }
}
