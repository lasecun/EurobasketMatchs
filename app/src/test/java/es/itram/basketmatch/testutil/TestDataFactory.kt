package es.itram.basketmatch.testutil

import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.entity.Standing
import java.time.LocalDateTime

/**
 * Factory para crear datos de test
 */
object TestDataFactory {

    fun createTestTeam(
        id: String = "MAD",
        name: String = "Real Madrid",
        city: String = "Madrid",
        country: String = "ESP",
        isFavorite: Boolean = false
    ): Team {
        return Team(
            id = id,
            name = name,
            shortName = name.take(3).uppercase(),
            code = id,
            country = country,
            city = city,
            founded = 1902,
            coach = "Test Coach",
            logoUrl = "https://example.com/logo.png",
            isFavorite = isFavorite
        )
    }

    fun createTestMatch(
        id: String = "1",
        homeTeamId: String = "MAD",
        homeTeamName: String = "Real Madrid",
        awayTeamId: String = "FCB",
        awayTeamName: String = "FC Barcelona",
        dateTime: LocalDateTime = LocalDateTime.now(),
        venue: String = "WiZink Center",
        round: Int = 1,
        status: MatchStatus = MatchStatus.SCHEDULED,
        homeScore: Int? = null,
        awayScore: Int? = null,
        seasonType: SeasonType = SeasonType.REGULAR
    ): Match {
        return Match(
            id = id,
            homeTeamId = homeTeamId,
            homeTeamName = homeTeamName,
            homeTeamLogo = "https://example.com/home_logo.png",
            awayTeamId = awayTeamId,
            awayTeamName = awayTeamName,
            awayTeamLogo = "https://example.com/away_logo.png",
            dateTime = dateTime,
            venue = venue,
            round = round,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore,
            seasonType = seasonType
        )
    }

    fun createTestStanding(
        teamId: String = "MAD",
        position: Int = 1,
        played: Int = 10,
        won: Int = 8,
        lost: Int = 2,
        pointsFor: Int = 850,
        pointsAgainst: Int = 750,
        pointsDifference: Int = 100,
        seasonType: SeasonType = SeasonType.REGULAR
    ): Standing {
        return Standing(
            teamId = teamId,
            position = position,
            played = played,
            won = won,
            lost = lost,
            pointsFor = pointsFor,
            pointsAgainst = pointsAgainst,
            pointsDifference = pointsDifference,
            seasonType = seasonType
        )
    }

    // Métodos auxiliares para crear listas de test
    fun createTestTeamList(count: Int = 3): List<Team> {
        return (1..count).map { index ->
            createTestTeam(
                id = "TEAM$index",
                name = "Team $index",
                city = "City $index",
                country = "Country $index"
            )
        }
    }

    fun createTestMatchList(count: Int = 3): List<Match> {
        return (1..count).map { index ->
            createTestMatch(
                id = index.toString(),
                homeTeamId = "HOME$index",
                homeTeamName = "Home Team $index",
                awayTeamId = "AWAY$index",
                awayTeamName = "Away Team $index",
                dateTime = LocalDateTime.now().plusDays(index.toLong())
            )
        }
    }

    fun createTestStandingList(count: Int = 3): List<Standing> {
        return (1..count).map { index ->
            createTestStanding(
                teamId = "TEAM$index",
                position = index,
                played = 10,
                won = 10 - index,
                lost = index - 1
            )
        }
    }

    fun createMockGenerationResult(
        teamsGenerated: Int = 18,
        matchesGenerated: Int = 306,
        generationTimestamp: Long = System.currentTimeMillis()
    ): es.itram.basketmatch.data.generator.GenerationResult {
        return es.itram.basketmatch.data.generator.GenerationResult(
            teamsGenerated = teamsGenerated,
            matchesGenerated = matchesGenerated,
            generationTimestamp = generationTimestamp
        )
    }
}
