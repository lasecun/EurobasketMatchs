package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.testutil.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests unitarios para TeamMapper
 */
class TeamMapperTest {

    @Test
    fun `when toDomain is called, should map TeamEntity to Team correctly`() {
        // Given
        val teamEntity = TeamEntity(
            id = "MAD",
            name = "Real Madrid",
            shortName = "RM",
            code = "MAD",
            country = "ESP",
            city = "Madrid",
            founded = 1902,
            coach = "Test Coach",
            logoUrl = "test_url",
            isFavorite = true
        )

        // When
        val team = TeamMapper.toDomain(teamEntity)

        // Then
        assertEquals(teamEntity.id, team.id)
        assertEquals(teamEntity.name, team.name)
        assertEquals(teamEntity.shortName, team.shortName)
        assertEquals(teamEntity.code, team.code)
        assertEquals(teamEntity.country, team.country)
        assertEquals(teamEntity.city, team.city)
        assertEquals(teamEntity.founded, team.founded)
        assertEquals(teamEntity.coach, team.coach)
        assertEquals(teamEntity.logoUrl, team.logoUrl)
        assertEquals(teamEntity.isFavorite, team.isFavorite)
    }

    @Test
    fun `when fromDomain is called, should map Team to TeamEntity correctly`() {
        // Given
        val team = TestDataFactory.createTestTeam(
            id = "BAR",
            name = "FC Barcelona",
            isFavorite = false
        )

        // When
        val teamEntity = TeamMapper.fromDomain(team)

        // Then
        assertEquals(team.id, teamEntity.id)
        assertEquals(team.name, teamEntity.name)
        assertEquals(team.shortName, teamEntity.shortName)
        assertEquals(team.code, teamEntity.code)
        assertEquals(team.country, teamEntity.country)
        assertEquals(team.city, teamEntity.city)
        assertEquals(team.founded, teamEntity.founded)
        assertEquals(team.coach, teamEntity.coach)
        assertEquals(team.logoUrl, teamEntity.logoUrl)
        assertEquals(team.isFavorite, teamEntity.isFavorite)
    }

    @Test
    fun `when toDomainList is called, should map list of TeamEntity to list of Team`() {
        // Given
        val teamEntities = listOf(
            TeamEntity(
                id = "MAD",
                name = "Real Madrid",
                shortName = "RM",
                code = "MAD",
                country = "ESP",
                city = "Madrid",
                founded = 1902,
                coach = "Coach 1",
                logoUrl = "url1",
                isFavorite = true
            ),
            TeamEntity(
                id = "BAR",
                name = "FC Barcelona",
                shortName = "FCB",
                code = "BAR",
                country = "ESP",
                city = "Barcelona",
                founded = 1899,
                coach = "Coach 2",
                logoUrl = "url2",
                isFavorite = false
            )
        )

        // When
        val teams = TeamMapper.toDomainList(teamEntities)

        // Then
        assertEquals(teamEntities.size, teams.size)
        assertEquals(teamEntities[0].id, teams[0].id)
        assertEquals(teamEntities[0].name, teams[0].name)
        assertEquals(teamEntities[0].isFavorite, teams[0].isFavorite)
        assertEquals(teamEntities[1].id, teams[1].id)
        assertEquals(teamEntities[1].name, teams[1].name)
        assertEquals(teamEntities[1].isFavorite, teams[1].isFavorite)
    }

    @Test
    fun `when fromDomainList is called, should map list of Team to list of TeamEntity`() {
        // Given
        val teams = TestDataFactory.createTestTeamList()

        // When
        val teamEntities = TeamMapper.fromDomainList(teams)

        // Then
        assertEquals(teams.size, teamEntities.size)
        assertEquals(teams[0].id, teamEntities[0].id)
        assertEquals(teams[0].name, teamEntities[0].name)
        assertEquals(teams[0].isFavorite, teamEntities[0].isFavorite)
        assertEquals(teams[1].id, teamEntities[1].id)
        assertEquals(teams[1].name, teamEntities[1].name)
        assertEquals(teams[1].isFavorite, teamEntities[1].isFavorite)
    }

    @Test
    fun `when mapping empty lists, should return empty lists`() {
        // Given
        val emptyTeamEntities = emptyList<TeamEntity>()
        val emptyTeams = emptyList<es.itram.basketmatch.domain.entity.Team>()

        // When
        val teamsFromEntities = TeamMapper.toDomainList(emptyTeamEntities)
        val entitiesFromTeams = TeamMapper.fromDomainList(emptyTeams)

        // Then
        assertEquals(0, teamsFromEntities.size)
        assertEquals(0, entitiesFromTeams.size)
    }

    @Test
    fun `when mapping team with special characters, should preserve all data`() {
        // Given
        val teamWithSpecialChars = TeamEntity(
            id = "ÑÇ1",
            name = "Ñoño & Çömplëx Team 123",
            shortName = "ÑÇT",
            code = "ÑÇ1",
            country = "ÉSP",
            city = "Madríd",
            founded = 1950,
            coach = "José María López-González",
            logoUrl = "https://example.com/logo_ñç.png",
            isFavorite = true
        )

        // When
        val team = TeamMapper.toDomain(teamWithSpecialChars)
        val mappedBackEntity = TeamMapper.fromDomain(team)

        // Then
        assertEquals(teamWithSpecialChars.id, team.id)
        assertEquals(teamWithSpecialChars.name, team.name)
        assertEquals(teamWithSpecialChars.city, team.city)
        assertEquals(teamWithSpecialChars.coach, team.coach)
        
        // Verify round-trip mapping
        assertEquals(teamWithSpecialChars.id, mappedBackEntity.id)
        assertEquals(teamWithSpecialChars.name, mappedBackEntity.name)
        assertEquals(teamWithSpecialChars.city, mappedBackEntity.city)
        assertEquals(teamWithSpecialChars.coach, mappedBackEntity.coach)
        assertEquals(teamWithSpecialChars.isFavorite, mappedBackEntity.isFavorite)
    }
}
