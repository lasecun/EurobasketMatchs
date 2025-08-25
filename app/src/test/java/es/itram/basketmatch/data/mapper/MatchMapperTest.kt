package es.itram.basketmatch.data.mapper

import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.testutil.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests unitarios para MatchMapper
 */
class MatchMapperTest {

    @Test
    fun `when toDomain is called, should map MatchEntity to Match correctly`() {
        // Given
        val dateTime = LocalDateTime.of(2024, 12, 25, 20, 30)
        val matchEntity = MatchEntity(
            id = "M001",
            homeTeamId = "MAD",
            homeTeamName = "Real Madrid",
            homeTeamLogo = "mad_logo.png",
            awayTeamId = "BAR",
            awayTeamName = "FC Barcelona",
            awayTeamLogo = "bar_logo.png",
            dateTime = dateTime,
            venue = "WiZink Center",
            round = 15,
            seasonType = SeasonType.REGULAR,
            status = MatchStatus.SCHEDULED,
            homeScore = null,
            awayScore = null
        )

        // When
        val match = MatchMapper.toDomain(matchEntity)

        // Then
        assertEquals(matchEntity.id, match.id)
        assertEquals(matchEntity.homeTeamId, match.homeTeamId)
        assertEquals(matchEntity.homeTeamName, match.homeTeamName)
        assertEquals(matchEntity.homeTeamLogo, match.homeTeamLogo)
        assertEquals(matchEntity.awayTeamId, match.awayTeamId)
        assertEquals(matchEntity.awayTeamName, match.awayTeamName)
        assertEquals(matchEntity.awayTeamLogo, match.awayTeamLogo)
        assertEquals(matchEntity.dateTime, match.dateTime)
        assertEquals(matchEntity.venue, match.venue)
        assertEquals(matchEntity.round, match.round)
        assertEquals(matchEntity.seasonType, match.seasonType)
        assertEquals(matchEntity.status, match.status)
        assertEquals(matchEntity.homeScore, match.homeScore)
        assertEquals(matchEntity.awayScore, match.awayScore)
    }

    @Test
    fun `when fromDomain is called, should map Match to MatchEntity correctly`() {
        // Given
        val match = TestDataFactory.createTestMatch(
            id = "M002",
            homeTeamId = "MIL",
            homeTeamName = "EA7 Emporio Armani Milan",
            awayTeamId = "CSK",
            awayTeamName = "CSKA Moscow",
            status = MatchStatus.FINISHED
        )

        // When
        val matchEntity = MatchMapper.fromDomain(match)

        // Then
        assertEquals(match.id, matchEntity.id)
        assertEquals(match.homeTeamId, matchEntity.homeTeamId)
        assertEquals(match.homeTeamName, matchEntity.homeTeamName)
        assertEquals(match.homeTeamLogo, matchEntity.homeTeamLogo)
        assertEquals(match.awayTeamId, matchEntity.awayTeamId)
        assertEquals(match.awayTeamName, matchEntity.awayTeamName)
        assertEquals(match.awayTeamLogo, matchEntity.awayTeamLogo)
        assertEquals(match.dateTime, matchEntity.dateTime)
        assertEquals(match.venue, matchEntity.venue)
        assertEquals(match.round, matchEntity.round)
        assertEquals(match.seasonType, matchEntity.seasonType)
        assertEquals(match.status, matchEntity.status)
        assertEquals(match.homeScore, matchEntity.homeScore)
        assertEquals(match.awayScore, matchEntity.awayScore)
    }

    @Test
    fun `when toDomainList is called, should map list of MatchEntity to list of Match`() {
        // Given
        val dateTime1 = LocalDateTime.of(2024, 12, 20, 18, 0)
        val dateTime2 = LocalDateTime.of(2024, 12, 21, 20, 30)
        
        val matchEntities = listOf(
            MatchEntity(
                id = "M001",
                homeTeamId = "MAD",
                homeTeamName = "Real Madrid",
                homeTeamLogo = "mad_logo.png",
                awayTeamId = "BAR",
                awayTeamName = "FC Barcelona",
                awayTeamLogo = "bar_logo.png",
                dateTime = dateTime1,
                venue = "WiZink Center",
                round = 15,
                seasonType = SeasonType.REGULAR,
                status = MatchStatus.SCHEDULED,
                homeScore = null,
                awayScore = null
            ),
            MatchEntity(
                id = "M002",
                homeTeamId = "MIL",
                homeTeamName = "EA7 Emporio Armani Milan",
                homeTeamLogo = "mil_logo.png",
                awayTeamId = "CSK",
                awayTeamName = "CSKA Moscow",
                awayTeamLogo = "csk_logo.png",
                dateTime = dateTime2,
                venue = "Mediolanum Forum",
                round = 15,
                seasonType = SeasonType.REGULAR,
                status = MatchStatus.FINISHED,
                homeScore = 85,
                awayScore = 78
            )
        )

        // When
        val matches = MatchMapper.toDomainList(matchEntities)

        // Then
        assertEquals(matchEntities.size, matches.size)
        assertEquals(matchEntities[0].id, matches[0].id)
        assertEquals(matchEntities[0].status, matches[0].status)
        assertEquals(matchEntities[1].id, matches[1].id)
        assertEquals(matchEntities[1].homeScore, matches[1].homeScore)
        assertEquals(matchEntities[1].awayScore, matches[1].awayScore)
    }

    @Test
    fun `when fromDomainList is called, should map list of Match to list of MatchEntity`() {
        // Given
        val matches = TestDataFactory.createTestMatchList()

        // When
        val matchEntities = MatchMapper.fromDomainList(matches)

        // Then
        assertEquals(matches.size, matchEntities.size)
        assertEquals(matches[0].id, matchEntities[0].id)
        assertEquals(matches[0].homeTeamName, matchEntities[0].homeTeamName)
        assertEquals(matches[0].awayTeamName, matchEntities[0].awayTeamName)
        assertEquals(matches[0].status, matchEntities[0].status)
    }

    @Test
    fun `when mapping match with all null optional fields, should preserve nulls`() {
        // Given
        val matchEntity = MatchEntity(
            id = "M003",
            homeTeamId = "PAO",
            homeTeamName = "Panathinaikos",
            homeTeamLogo = null,
            awayTeamId = "OLY",
            awayTeamName = "Olympiacos",
            awayTeamLogo = null,
            dateTime = LocalDateTime.now(),
            venue = "",
            round = 1,
            seasonType = SeasonType.REGULAR,
            status = MatchStatus.SCHEDULED,
            homeScore = null,
            awayScore = null
        )

        // When
        val match = MatchMapper.toDomain(matchEntity)
        val mappedBackEntity = MatchMapper.fromDomain(match)

        // Then
        assertEquals(null, match.homeTeamLogo)
        assertEquals(null, match.awayTeamLogo)
        assertEquals("", match.venue) // Empty string since venue is not nullable
        assertEquals(null, match.homeScore)
        assertEquals(null, match.awayScore)
        
        // Verify round-trip mapping
        assertEquals(matchEntity.homeTeamLogo, mappedBackEntity.homeTeamLogo)
        assertEquals(matchEntity.awayTeamLogo, mappedBackEntity.awayTeamLogo)
        assertEquals(matchEntity.venue, mappedBackEntity.venue)
        assertEquals(matchEntity.homeScore, mappedBackEntity.homeScore)
        assertEquals(matchEntity.awayScore, mappedBackEntity.awayScore)
    }

    @Test
    fun `when mapping match with scores, should preserve score values`() {
        // Given
        val matchEntity = MatchEntity(
            id = "M004",
            homeTeamId = "MAD",
            homeTeamName = "Real Madrid",
            homeTeamLogo = "mad_logo.png",
            awayTeamId = "BAR",
            awayTeamName = "FC Barcelona",
            awayTeamLogo = "bar_logo.png",
            dateTime = LocalDateTime.now(),
            venue = "WiZink Center",
            round = 1,
            seasonType = SeasonType.PLAYOFFS,
            status = MatchStatus.FINISHED,
            homeScore = 95,
            awayScore = 88
        )

        // When
        val match = MatchMapper.toDomain(matchEntity)

        // Then
        assertEquals(95, match.homeScore)
        assertEquals(88, match.awayScore)
        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(SeasonType.PLAYOFFS, match.seasonType)
    }

    @Test
    fun `when mapping empty lists, should return empty lists`() {
        // Given
        val emptyMatchEntities = emptyList<MatchEntity>()
        val emptyMatches = emptyList<es.itram.basketmatch.domain.entity.Match>()

        // When
        val matchesFromEntities = MatchMapper.toDomainList(emptyMatchEntities)
        val entitiesFromMatches = MatchMapper.fromDomainList(emptyMatches)

        // Then
        assertEquals(0, matchesFromEntities.size)
        assertEquals(0, entitiesFromMatches.size)
    }
}
