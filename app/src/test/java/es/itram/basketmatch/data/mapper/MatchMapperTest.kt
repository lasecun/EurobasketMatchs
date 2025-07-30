package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import org.junit.Test
import java.time.LocalDateTime

class MatchMapperTest {

    private val testDateTime = LocalDateTime.of(2024, 3, 15, 20, 0)

    private val matchEntity = MatchEntity(
        id = "M001",
        homeTeamId = "T001",
        homeTeamName = "Real Madrid",
        homeTeamLogo = "https://example.com/madrid.png",
        awayTeamId = "T002",
        awayTeamName = "FC Barcelona",
        awayTeamLogo = "https://example.com/barcelona.png",
        dateTime = testDateTime,
        venue = "WiZink Center",
        round = 15,
        status = MatchStatus.FINISHED,
        homeScore = 85,
        awayScore = 78,
        seasonType = SeasonType.REGULAR
    )

    private val matchDomain = Match(
        id = "M001",
        homeTeamId = "T001",
        homeTeamName = "Real Madrid",
        homeTeamLogo = "https://example.com/madrid.png",
        awayTeamId = "T002",
        awayTeamName = "FC Barcelona",
        awayTeamLogo = "https://example.com/barcelona.png",
        dateTime = testDateTime,
        venue = "WiZink Center",
        round = 15,
        status = MatchStatus.FINISHED,
        homeScore = 85,
        awayScore = 78,
        seasonType = SeasonType.REGULAR
    )

    @Test
    fun `toDomain should convert MatchEntity to Match correctly`() {
        // Act
        val result = MatchMapper.toDomain(matchEntity)

        // Assert
        assertThat(result.id).isEqualTo("M001")
        assertThat(result.homeTeamId).isEqualTo("T001")
        assertThat(result.homeTeamName).isEqualTo("Real Madrid")
        assertThat(result.homeTeamLogo).isEqualTo("https://example.com/madrid.png")
        assertThat(result.awayTeamId).isEqualTo("T002")
        assertThat(result.awayTeamName).isEqualTo("FC Barcelona")
        assertThat(result.awayTeamLogo).isEqualTo("https://example.com/barcelona.png")
        assertThat(result.dateTime).isEqualTo(testDateTime)
        assertThat(result.venue).isEqualTo("WiZink Center")
        assertThat(result.round).isEqualTo(15)
        assertThat(result.status).isEqualTo(MatchStatus.FINISHED)
        assertThat(result.homeScore).isEqualTo(85)
        assertThat(result.awayScore).isEqualTo(78)
        assertThat(result.seasonType).isEqualTo(SeasonType.REGULAR)
    }

    @Test
    fun `fromDomain should convert Match to MatchEntity correctly`() {
        // Act
        val result = MatchMapper.fromDomain(matchDomain)

        // Assert
        assertThat(result.id).isEqualTo("M001")
        assertThat(result.homeTeamId).isEqualTo("T001")
        assertThat(result.homeTeamName).isEqualTo("Real Madrid")
        assertThat(result.homeTeamLogo).isEqualTo("https://example.com/madrid.png")
        assertThat(result.awayTeamId).isEqualTo("T002")
        assertThat(result.awayTeamName).isEqualTo("FC Barcelona")
        assertThat(result.awayTeamLogo).isEqualTo("https://example.com/barcelona.png")
        assertThat(result.dateTime).isEqualTo(testDateTime)
        assertThat(result.venue).isEqualTo("WiZink Center")
        assertThat(result.round).isEqualTo(15)
        assertThat(result.status).isEqualTo(MatchStatus.FINISHED)
        assertThat(result.homeScore).isEqualTo(85)
        assertThat(result.awayScore).isEqualTo(78)
        assertThat(result.seasonType).isEqualTo(SeasonType.REGULAR)
    }

    @Test
    fun `toDomain should handle null scores correctly`() {
        // Arrange
        val entityWithNullScores = matchEntity.copy(
            homeScore = null,
            awayScore = null,
            status = MatchStatus.SCHEDULED
        )

        // Act
        val result = MatchMapper.toDomain(entityWithNullScores)

        // Assert
        assertThat(result.homeScore).isNull()
        assertThat(result.awayScore).isNull()
        assertThat(result.status).isEqualTo(MatchStatus.SCHEDULED)
    }

    @Test
    fun `fromDomain should handle null scores correctly`() {
        // Arrange
        val domainWithNullScores = matchDomain.copy(
            homeScore = null,
            awayScore = null,
            status = MatchStatus.SCHEDULED
        )

        // Act
        val result = MatchMapper.fromDomain(domainWithNullScores)

        // Assert
        assertThat(result.homeScore).isNull()
        assertThat(result.awayScore).isNull()
        assertThat(result.status).isEqualTo(MatchStatus.SCHEDULED)
    }

    @Test
    fun `toDomain should handle null team logos correctly`() {
        // Arrange
        val entityWithNullLogos = matchEntity.copy(
            homeTeamLogo = null,
            awayTeamLogo = null
        )

        // Act
        val result = MatchMapper.toDomain(entityWithNullLogos)

        // Assert
        assertThat(result.homeTeamLogo).isNull()
        assertThat(result.awayTeamLogo).isNull()
    }

    @Test
    fun `fromDomain should handle null team logos correctly`() {
        // Arrange
        val domainWithNullLogos = matchDomain.copy(
            homeTeamLogo = null,
            awayTeamLogo = null
        )

        // Act
        val result = MatchMapper.fromDomain(domainWithNullLogos)

        // Assert
        assertThat(result.homeTeamLogo).isNull()
        assertThat(result.awayTeamLogo).isNull()
    }

    @Test
    fun `toDomainList should convert list of MatchEntity to list of Match correctly`() {
        // Arrange
        val entity1 = matchEntity
        val entity2 = matchEntity.copy(id = "M002", homeTeamName = "Fenerbahce")
        val entityList = listOf(entity1, entity2)

        // Act
        val result = MatchMapper.toDomainList(entityList)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("M001")
        assertThat(result[0].homeTeamName).isEqualTo("Real Madrid")
        assertThat(result[1].id).isEqualTo("M002")
        assertThat(result[1].homeTeamName).isEqualTo("Fenerbahce")
    }

    @Test
    fun `fromDomainList should convert list of Match to list of MatchEntity correctly`() {
        // Arrange
        val domain1 = matchDomain
        val domain2 = matchDomain.copy(id = "M002", homeTeamName = "Fenerbahce")
        val domainList = listOf(domain1, domain2)

        // Act
        val result = MatchMapper.fromDomainList(domainList)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("M001")
        assertThat(result[0].homeTeamName).isEqualTo("Real Madrid")
        assertThat(result[1].id).isEqualTo("M002")
        assertThat(result[1].homeTeamName).isEqualTo("Fenerbahce")
    }

    @Test
    fun `mapping should handle different match statuses correctly`() {
        // Arrange
        val scheduledMatch = matchEntity.copy(status = MatchStatus.SCHEDULED, homeScore = null, awayScore = null)
        val liveMatch = matchEntity.copy(status = MatchStatus.LIVE, homeScore = 42, awayScore = 38)
        val postponedMatch = matchEntity.copy(status = MatchStatus.POSTPONED)
        val cancelledMatch = matchEntity.copy(status = MatchStatus.CANCELLED)

        // Act
        val scheduledResult = MatchMapper.toDomain(scheduledMatch)
        val liveResult = MatchMapper.toDomain(liveMatch)
        val postponedResult = MatchMapper.toDomain(postponedMatch)
        val cancelledResult = MatchMapper.toDomain(cancelledMatch)

        // Assert
        assertThat(scheduledResult.status).isEqualTo(MatchStatus.SCHEDULED)
        assertThat(scheduledResult.homeScore).isNull()
        assertThat(scheduledResult.awayScore).isNull()
        
        assertThat(liveResult.status).isEqualTo(MatchStatus.LIVE)
        assertThat(liveResult.homeScore).isEqualTo(42)
        assertThat(liveResult.awayScore).isEqualTo(38)
        
        assertThat(postponedResult.status).isEqualTo(MatchStatus.POSTPONED)
        assertThat(cancelledResult.status).isEqualTo(MatchStatus.CANCELLED)
    }

    @Test
    fun `mapping should handle different season types correctly`() {
        // Arrange
        val regularMatch = matchEntity.copy(seasonType = SeasonType.REGULAR)
        val playoffMatch = matchEntity.copy(seasonType = SeasonType.PLAYOFFS)
        val finalFourMatch = matchEntity.copy(seasonType = SeasonType.FINAL_FOUR)

        // Act
        val regularResult = MatchMapper.toDomain(regularMatch)
        val playoffResult = MatchMapper.toDomain(playoffMatch)
        val finalFourResult = MatchMapper.toDomain(finalFourMatch)

        // Assert
        assertThat(regularResult.seasonType).isEqualTo(SeasonType.REGULAR)
        assertThat(playoffResult.seasonType).isEqualTo(SeasonType.PLAYOFFS)
        assertThat(finalFourResult.seasonType).isEqualTo(SeasonType.FINAL_FOUR)
    }

    @Test
    fun `toDomainList should handle empty list correctly`() {
        // Arrange
        val emptyList = emptyList<MatchEntity>()

        // Act
        val result = MatchMapper.toDomainList(emptyList)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `fromDomainList should handle empty list correctly`() {
        // Arrange
        val emptyList = emptyList<Match>()

        // Act
        val result = MatchMapper.fromDomainList(emptyList)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `round trip conversion should maintain data integrity`() {
        // Arrange
        val originalEntity = matchEntity

        // Act
        val domain = MatchMapper.toDomain(originalEntity)
        val backToEntity = MatchMapper.fromDomain(domain)

        // Assert
        assertThat(backToEntity.id).isEqualTo(originalEntity.id)
        assertThat(backToEntity.homeTeamId).isEqualTo(originalEntity.homeTeamId)
        assertThat(backToEntity.homeTeamName).isEqualTo(originalEntity.homeTeamName)
        assertThat(backToEntity.homeTeamLogo).isEqualTo(originalEntity.homeTeamLogo)
        assertThat(backToEntity.awayTeamId).isEqualTo(originalEntity.awayTeamId)
        assertThat(backToEntity.awayTeamName).isEqualTo(originalEntity.awayTeamName)
        assertThat(backToEntity.awayTeamLogo).isEqualTo(originalEntity.awayTeamLogo)
        assertThat(backToEntity.dateTime).isEqualTo(originalEntity.dateTime)
        assertThat(backToEntity.venue).isEqualTo(originalEntity.venue)
        assertThat(backToEntity.round).isEqualTo(originalEntity.round)
        assertThat(backToEntity.status).isEqualTo(originalEntity.status)
        assertThat(backToEntity.homeScore).isEqualTo(originalEntity.homeScore)
        assertThat(backToEntity.awayScore).isEqualTo(originalEntity.awayScore)
        assertThat(backToEntity.seasonType).isEqualTo(originalEntity.seasonType)
    }
}
