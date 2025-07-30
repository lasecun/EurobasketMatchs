package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.entity.SeasonType
import org.junit.Test

class StandingMapperTest {

    private val standingEntity = StandingEntity(
        teamId = "T001",
        position = 1,
        played = 30,
        won = 24,
        lost = 6,
        pointsFor = 2580,
        pointsAgainst = 2340,
        pointsDifference = 240,
        seasonType = SeasonType.REGULAR
    )

    private val standingDomain = Standing(
        teamId = "T001",
        position = 1,
        played = 30,
        won = 24,
        lost = 6,
        pointsFor = 2580,
        pointsAgainst = 2340,
        pointsDifference = 240,
        seasonType = SeasonType.REGULAR
    )

    @Test
    fun `toDomain should convert StandingEntity to Standing correctly`() {
        // Act
        val result = StandingMapper.toDomain(standingEntity)

        // Assert
        assertThat(result.teamId).isEqualTo("T001")
        assertThat(result.position).isEqualTo(1)
        assertThat(result.played).isEqualTo(30)
        assertThat(result.won).isEqualTo(24)
        assertThat(result.lost).isEqualTo(6)
        assertThat(result.pointsFor).isEqualTo(2580)
        assertThat(result.pointsAgainst).isEqualTo(2340)
        assertThat(result.pointsDifference).isEqualTo(240)
        assertThat(result.seasonType).isEqualTo(SeasonType.REGULAR)
    }

    @Test
    fun `fromDomain should convert Standing to StandingEntity correctly`() {
        // Act
        val result = StandingMapper.fromDomain(standingDomain)

        // Assert
        assertThat(result.teamId).isEqualTo("T001")
        assertThat(result.position).isEqualTo(1)
        assertThat(result.played).isEqualTo(30)
        assertThat(result.won).isEqualTo(24)
        assertThat(result.lost).isEqualTo(6)
        assertThat(result.pointsFor).isEqualTo(2580)
        assertThat(result.pointsAgainst).isEqualTo(2340)
        assertThat(result.pointsDifference).isEqualTo(240)
        assertThat(result.seasonType).isEqualTo(SeasonType.REGULAR)
    }

    @Test
    fun `toDomain should preserve calculated win percentage`() {
        // Act
        val result = StandingMapper.toDomain(standingEntity)

        // Assert
        assertThat(result.winPercentage).isWithin(0.01).of(80.0) // 24/30 * 100 = 80.0
    }

    @Test
    fun `toDomain should handle perfect season correctly`() {
        // Arrange
        val perfectSeasonEntity = standingEntity.copy(
            won = 30,
            lost = 0,
            pointsDifference = 500
        )

        // Act
        val result = StandingMapper.toDomain(perfectSeasonEntity)

        // Assert
        assertThat(result.won).isEqualTo(30)
        assertThat(result.lost).isEqualTo(0)
        assertThat(result.winPercentage).isWithin(0.01).of(100.0)
        assertThat(result.pointsDifference).isEqualTo(500)
    }

    @Test
    fun `toDomain should handle winless season correctly`() {
        // Arrange
        val winlessSeasonEntity = standingEntity.copy(
            won = 0,
            lost = 30,
            pointsDifference = -500
        )

        // Act
        val result = StandingMapper.toDomain(winlessSeasonEntity)

        // Assert
        assertThat(result.won).isEqualTo(0)
        assertThat(result.lost).isEqualTo(30)
        assertThat(result.winPercentage).isWithin(0.01).of(0.0)
        assertThat(result.pointsDifference).isEqualTo(-500)
    }

    @Test
    fun `toDomain should handle team with no games played correctly`() {
        // Arrange
        val noGamesEntity = standingEntity.copy(
            played = 0,
            won = 0,
            lost = 0,
            pointsFor = 0,
            pointsAgainst = 0,
            pointsDifference = 0
        )

        // Act
        val result = StandingMapper.toDomain(noGamesEntity)

        // Assert
        assertThat(result.played).isEqualTo(0)
        assertThat(result.won).isEqualTo(0)
        assertThat(result.lost).isEqualTo(0)
        assertThat(result.winPercentage).isWithin(0.01).of(0.0)
        assertThat(result.pointsFor).isEqualTo(0)
        assertThat(result.pointsAgainst).isEqualTo(0)
        assertThat(result.pointsDifference).isEqualTo(0)
    }

    @Test
    fun `toDomainList should convert list of StandingEntity to list of Standing correctly`() {
        // Arrange
        val entity1 = standingEntity
        val entity2 = standingEntity.copy(
            teamId = "T002",
            position = 2,
            won = 20,
            lost = 10
        )
        val entityList = listOf(entity1, entity2)

        // Act
        val result = StandingMapper.toDomainList(entityList)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].teamId).isEqualTo("T001")
        assertThat(result[0].position).isEqualTo(1)
        assertThat(result[0].winPercentage).isWithin(0.01).of(80.0)
        
        assertThat(result[1].teamId).isEqualTo("T002")
        assertThat(result[1].position).isEqualTo(2)
        assertThat(result[1].winPercentage).isWithin(0.01).of(66.67) // 20/30 * 100 ≈ 66.67
    }

    @Test
    fun `fromDomainList should convert list of Standing to list of StandingEntity correctly`() {
        // Arrange
        val domain1 = standingDomain
        val domain2 = standingDomain.copy(
            teamId = "T002",
            position = 2,
            won = 20,
            lost = 10
        )
        val domainList = listOf(domain1, domain2)

        // Act
        val result = StandingMapper.fromDomainList(domainList)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].teamId).isEqualTo("T001")
        assertThat(result[0].position).isEqualTo(1)
        assertThat(result[0].won).isEqualTo(24)
        
        assertThat(result[1].teamId).isEqualTo("T002")
        assertThat(result[1].position).isEqualTo(2)
        assertThat(result[1].won).isEqualTo(20)
    }

    @Test
    fun `mapping should handle different season types correctly`() {
        // Arrange
        val regularEntity = standingEntity.copy(seasonType = SeasonType.REGULAR)
        val playoffEntity = standingEntity.copy(seasonType = SeasonType.PLAYOFFS)
        val finalFourEntity = standingEntity.copy(seasonType = SeasonType.FINAL_FOUR)

        // Act
        val regularResult = StandingMapper.toDomain(regularEntity)
        val playoffResult = StandingMapper.toDomain(playoffEntity)
        val finalFourResult = StandingMapper.toDomain(finalFourEntity)

        // Assert
        assertThat(regularResult.seasonType).isEqualTo(SeasonType.REGULAR)
        assertThat(playoffResult.seasonType).isEqualTo(SeasonType.PLAYOFFS)
        assertThat(finalFourResult.seasonType).isEqualTo(SeasonType.FINAL_FOUR)
    }

    @Test
    fun `toDomainList should handle empty list correctly`() {
        // Arrange
        val emptyList = emptyList<StandingEntity>()

        // Act
        val result = StandingMapper.toDomainList(emptyList)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `fromDomainList should handle empty list correctly`() {
        // Arrange
        val emptyList = emptyList<Standing>()

        // Act
        val result = StandingMapper.fromDomainList(emptyList)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `round trip conversion should maintain data integrity`() {
        // Arrange
        val originalEntity = standingEntity

        // Act
        val domain = StandingMapper.toDomain(originalEntity)
        val backToEntity = StandingMapper.fromDomain(domain)

        // Assert
        assertThat(backToEntity.teamId).isEqualTo(originalEntity.teamId)
        assertThat(backToEntity.position).isEqualTo(originalEntity.position)
        assertThat(backToEntity.played).isEqualTo(originalEntity.played)
        assertThat(backToEntity.won).isEqualTo(originalEntity.won)
        assertThat(backToEntity.lost).isEqualTo(originalEntity.lost)
        assertThat(backToEntity.pointsFor).isEqualTo(originalEntity.pointsFor)
        assertThat(backToEntity.pointsAgainst).isEqualTo(originalEntity.pointsAgainst)
        assertThat(backToEntity.pointsDifference).isEqualTo(originalEntity.pointsDifference)
        assertThat(backToEntity.seasonType).isEqualTo(originalEntity.seasonType)
    }

    @Test
    fun `mapping should handle edge case with negative points difference`() {
        // Arrange
        val entityWithNegativeDiff = standingEntity.copy(
            position = 18,
            won = 8,
            lost = 22,
            pointsFor = 2100,
            pointsAgainst = 2500,
            pointsDifference = -400
        )

        // Act
        val result = StandingMapper.toDomain(entityWithNegativeDiff)

        // Assert
        assertThat(result.position).isEqualTo(18)
        assertThat(result.won).isEqualTo(8)
        assertThat(result.lost).isEqualTo(22)
        assertThat(result.pointsDifference).isEqualTo(-400)
        assertThat(result.winPercentage).isWithin(0.01).of(26.67) // 8/30 * 100 ≈ 26.67
    }

    @Test
    fun `mapping should handle teams with identical records`() {
        // Arrange
        val team1Entity = standingEntity.copy(teamId = "T001", position = 5)
        val team2Entity = standingEntity.copy(teamId = "T002", position = 6)

        // Act
        val team1Result = StandingMapper.toDomain(team1Entity)
        val team2Result = StandingMapper.toDomain(team2Entity)

        // Assert
        assertThat(team1Result.teamId).isEqualTo("T001")
        assertThat(team1Result.position).isEqualTo(5)
        assertThat(team2Result.teamId).isEqualTo("T002")
        assertThat(team2Result.position).isEqualTo(6)
        
        // Same records, different positions (tie-breaker applied)
        assertThat(team1Result.won).isEqualTo(team2Result.won)
        assertThat(team1Result.lost).isEqualTo(team2Result.lost)
        assertThat(team1Result.winPercentage).isEqualTo(team2Result.winPercentage)
    }
}
