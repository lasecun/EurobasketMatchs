package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.remote.dto.MatchDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamDto
import org.junit.Test

class MatchMapperTest {

    private val homeTeamDto = TeamDto(
        teamId = "T1",
        teamName = "Real Madrid",
        teamTla = "MAD",
        imageUrl = "https://example.com/madrid.png"
    )

    private val awayTeamDto = TeamDto(
        teamId = "T2",
        teamName = "FC Barcelona",
        teamTla = "BAR", 
        imageUrl = "https://example.com/barcelona.png"
    )

    private val matchDto = MatchDto(
        matchId = "M001",
        gameCode = "GC001",
        homeTeam = homeTeamDto,
        awayTeam = awayTeamDto,
        homeScore = 85,
        awayScore = 78,
        dateTime = "2024-03-15T20:00:00.000Z",
        roundNumber = 1,
        phaseTypeCode = "RS",
        seasonCode = "E2023",
        liveScore = "85-78",
        matchStatus = "FINISHED"
    )

    @Test
    fun `toDomainModel should convert MatchDto to Match correctly`() {
        // Act
        val result = matchDto.toDomainModel()

        // Assert
        assertThat(result.matchId).isEqualTo("M001")
        assertThat(result.gameCode).isEqualTo("GC001")
        assertThat(result.homeTeam.teamId).isEqualTo("T1")
        assertThat(result.homeTeam.teamName).isEqualTo("Real Madrid")
        assertThat(result.awayTeam.teamId).isEqualTo("T2")
        assertThat(result.awayTeam.teamName).isEqualTo("FC Barcelona")
        assertThat(result.homeScore).isEqualTo(85)
        assertThat(result.awayScore).isEqualTo(78)
        assertThat(result.dateTime).isEqualTo("2024-03-15T20:00:00.000Z")
        assertThat(result.roundNumber).isEqualTo(1)
        assertThat(result.phaseTypeCode).isEqualTo("RS")
        assertThat(result.seasonCode).isEqualTo("E2023")
        assertThat(result.liveScore).isEqualTo("85-78")
        assertThat(result.matchStatus).isEqualTo("FINISHED")
    }

    @Test
    fun `toDomainModel should handle null scores`() {
        // Arrange
        val matchWithNullScores = matchDto.copy(
            homeScore = null,
            awayScore = null
        )

        // Act
        val result = matchWithNullScores.toDomainModel()

        // Assert
        assertThat(result.homeScore).isEqualTo(0)
        assertThat(result.awayScore).isEqualTo(0)
    }

    @Test
    fun `toDomainModel should handle null liveScore`() {
        // Arrange
        val matchWithNullLiveScore = matchDto.copy(liveScore = null)

        // Act
        val result = matchWithNullLiveScore.toDomainModel()

        // Assert
        assertThat(result.liveScore).isEqualTo("")
    }

    @Test
    fun `toDomainModel should handle null matchStatus`() {
        // Arrange
        val matchWithNullStatus = matchDto.copy(matchStatus = null)

        // Act
        val result = matchWithNullStatus.toDomainModel()

        // Assert
        assertThat(result.matchStatus).isEqualTo("")
    }

    @Test
    fun `toDomainModel should handle null optional fields`() {
        // Arrange
        val matchWithNulls = matchDto.copy(
            gameCode = null,
            dateTime = null,
            roundNumber = null,
            phaseTypeCode = null,
            seasonCode = null,
            liveScore = null,
            matchStatus = null
        )

        // Act
        val result = matchWithNulls.toDomainModel()

        // Assert
        assertThat(result.gameCode).isEqualTo("")
        assertThat(result.dateTime).isEqualTo("")
        assertThat(result.roundNumber).isEqualTo(0)
        assertThat(result.phaseTypeCode).isEqualTo("")
        assertThat(result.seasonCode).isEqualTo("")
        assertThat(result.liveScore).isEqualTo("")
        assertThat(result.matchStatus).isEqualTo("")
    }

    @Test
    fun `toDomainModel should convert teams correctly`() {
        // Act
        val result = matchDto.toDomainModel()

        // Assert
        assertThat(result.homeTeam.teamId).isEqualTo(homeTeamDto.teamId)
        assertThat(result.homeTeam.teamName).isEqualTo(homeTeamDto.teamName)
        assertThat(result.homeTeam.teamTla).isEqualTo(homeTeamDto.teamTla)
        assertThat(result.homeTeam.imageUrl).isEqualTo(homeTeamDto.imageUrl)
        assertThat(result.homeTeam.isFavorite).isFalse()

        assertThat(result.awayTeam.teamId).isEqualTo(awayTeamDto.teamId)
        assertThat(result.awayTeam.teamName).isEqualTo(awayTeamDto.teamName)
        assertThat(result.awayTeam.teamTla).isEqualTo(awayTeamDto.teamTla)
        assertThat(result.awayTeam.imageUrl).isEqualTo(awayTeamDto.imageUrl)
        assertThat(result.awayTeam.isFavorite).isFalse()
    }

    @Test
    fun `toDomainModel should handle match in progress with live score`() {
        // Arrange
        val liveMatch = matchDto.copy(
            homeScore = 42,
            awayScore = 38,
            liveScore = "42-38",
            matchStatus = "LIVE"
        )

        // Act
        val result = liveMatch.toDomainModel()

        // Assert
        assertThat(result.homeScore).isEqualTo(42)
        assertThat(result.awayScore).isEqualTo(38)
        assertThat(result.liveScore).isEqualTo("42-38")
        assertThat(result.matchStatus).isEqualTo("LIVE")
    }

    @Test
    fun `toDomainModel should handle scheduled match with no scores`() {
        // Arrange
        val scheduledMatch = matchDto.copy(
            homeScore = null,
            awayScore = null,
            liveScore = null,
            matchStatus = "SCHEDULED"
        )

        // Act
        val result = scheduledMatch.toDomainModel()

        // Assert
        assertThat(result.homeScore).isEqualTo(0)
        assertThat(result.awayScore).isEqualTo(0)
        assertThat(result.liveScore).isEqualTo("")
        assertThat(result.matchStatus).isEqualTo("SCHEDULED")
    }

    @Test
    fun `toDomainModel should handle different round numbers`() {
        // Arrange
        val playoffMatch = matchDto.copy(
            roundNumber = 8,
            phaseTypeCode = "PO"
        )

        // Act
        val result = playoffMatch.toDomainModel()

        // Assert
        assertThat(result.roundNumber).isEqualTo(8)
        assertThat(result.phaseTypeCode).isEqualTo("PO")
    }
}
