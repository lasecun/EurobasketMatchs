package es.itram.basketmatch.testutil

import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus as WebMatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.data.datasource.remote.dto.PersonDto
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import java.time.LocalDateTime

/**
 * Utilidades para crear datos de prueba
 */
object TestDataFactory {

    fun createTestTeam(
        id: String = "1",
        name: String = "Real Madrid",
        shortName: String = "RMA",
        code: String = "MAD",
        city: String = "Madrid",
        country: String = "España",
        logoUrl: String = "https://example.com/logo.png",
        founded: Int = 1902,
        coach: String = "Chus Mateo",
        website: String = "https://realmadrid.com",
        primaryColor: String = "#FFFFFF",
        secondaryColor: String = "#002FA7",
        isFavorite: Boolean = false
    ) = Team(
        id = id,
        name = name,
        shortName = shortName,
        code = code,
        city = city,
        country = country,
        logoUrl = logoUrl,
        founded = founded,
        coach = coach,
        website = website,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        isFavorite = isFavorite
    )

    fun createTestTeamEntity(
        id: String = "1",
        name: String = "Real Madrid",
        shortName: String = "RMA",
        code: String = "MAD",
        city: String = "Madrid",
        country: String = "España",
        logoUrl: String = "https://example.com/logo.png",
        founded: Int = 1902,
        coach: String = "Chus Mateo",
        website: String = "https://realmadrid.com",
        primaryColor: String = "#FFFFFF",
        secondaryColor: String = "#002FA7",
        isFavorite: Boolean = false
    ) = TeamEntity(
        id = id,
        name = name,
        shortName = shortName,
        code = code,
        city = city,
        country = country,
        logoUrl = logoUrl,
        founded = founded,
        coach = coach,
        website = website,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        isFavorite = isFavorite
    )

    fun createTestMatch(
        id: String = "1",
        homeTeamId: String = "1",
        homeTeamName: String = "Real Madrid",
        homeTeamLogo: String? = null,
        awayTeamId: String = "2",
        awayTeamName: String = "FC Barcelona",
        awayTeamLogo: String? = null,
        dateTime: LocalDateTime = LocalDateTime.now(),
        homeScore: Int? = null,
        awayScore: Int? = null,
        status: MatchStatus = MatchStatus.SCHEDULED,
        round: Int = 1,
        venue: String = "WiZink Center"
    ) = Match(
        id = id,
        homeTeamId = homeTeamId,
        homeTeamName = homeTeamName,
        homeTeamLogo = homeTeamLogo,
        awayTeamId = awayTeamId,
        awayTeamName = awayTeamName,
        awayTeamLogo = awayTeamLogo,
        dateTime = dateTime,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        round = round,
        venue = venue
    )

    fun createTestMatchEntity(
        id: String = "1",
        homeTeamId: String = "1",
        homeTeamName: String = "Real Madrid",
        homeTeamLogo: String? = null,
        awayTeamId: String = "2",
        awayTeamName: String = "FC Barcelona",
        awayTeamLogo: String? = null,
        dateTime: LocalDateTime = LocalDateTime.now(),
        homeScore: Int? = null,
        awayScore: Int? = null,
        status: MatchStatus = MatchStatus.SCHEDULED,
        round: Int = 1,
        venue: String = "WiZink Center",
        seasonType: SeasonType = SeasonType.REGULAR
    ) = MatchEntity(
        id = id,
        homeTeamId = homeTeamId,
        homeTeamName = homeTeamName,
        homeTeamLogo = homeTeamLogo,
        awayTeamId = awayTeamId,
        awayTeamName = awayTeamName,
        awayTeamLogo = awayTeamLogo,
        dateTime = dateTime,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        round = round,
        venue = venue,
        seasonType = seasonType
    )

    fun createTestStanding(
        teamId: String = "1",
        position: Int = 1,
        played: Int = 10,
        won: Int = 8,
        lost: Int = 2,
        pointsFor: Int = 850,
        pointsAgainst: Int = 780,
        pointsDifference: Int = 70,
        seasonType: SeasonType = SeasonType.REGULAR
    ) = Standing(
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

    fun createTestStandingEntity(
        teamId: String = "1",
        position: Int = 1,
        played: Int = 10,
        won: Int = 8,
        lost: Int = 2,
        pointsFor: Int = 850,
        pointsAgainst: Int = 780,
        pointsDifference: Int = 70,
        seasonType: SeasonType = SeasonType.REGULAR
    ) = StandingEntity(
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

    fun createTeamsList(count: Int = 3): List<Team> {
        return (1..count).map { index ->
            createTestTeam(
                id = index.toString(),
                name = "Team $index",
                shortName = "T$index",
                code = "T$index",
                city = "City $index",
                isFavorite = index % 2 == 0
            )
        }
    }

    fun createMatchesList(count: Int = 5): List<Match> {
        return (1..count).map { index ->
            createTestMatch(
                id = index.toString(),
                homeTeamId = (index % 2 + 1).toString(),
                awayTeamId = ((index + 1) % 2 + 1).toString(),
                dateTime = LocalDateTime.now().plusDays(index.toLong()),
                round = index
            )
        }
    }

    /**
     * Creates a default list of test teams
     */
    fun createTestTeamList(): List<Team> = listOf(
        createTestTeam("1", "Real Madrid"),
        createTestTeam("2", "FC Barcelona")
    )

    /**
     * Creates a default list of test matches
     */
    fun createTestMatchList(): List<Match> = listOf(
        createTestMatch("1"),
        createTestMatch("2"),
        createTestMatch("3")
    )

    /**
     * Creates a test TeamWebDto for web scraping tests
     */
    fun createTestTeamWebDto(
        id: String = "1",
        name: String = "Real Madrid",
        fullName: String = "Real Madrid Baloncesto",
        shortCode: String = "RMA",
        logoUrl: String? = "https://example.com/logo.png",
        country: String? = "España",
        venue: String? = "WiZink Center, Madrid",
        profileUrl: String = "https://euroleaguebasketball.net/teams/real-madrid"
    ) = TeamWebDto(
        id = id,
        name = name,
        fullName = fullName,
        shortCode = shortCode,
        logoUrl = logoUrl,
        country = country,
        venue = venue,
        profileUrl = profileUrl
    )

    /**
     * Creates a test MatchWebDto for web scraping tests
     */
    fun createTestMatchWebDto(
        id: String = "1",
        homeTeamId: String = "1",
        homeTeamName: String = "Real Madrid",
        homeTeamLogo: String? = null,
        awayTeamId: String = "2",
        awayTeamName: String = "FC Barcelona",
        awayTeamLogo: String? = null,
        date: String = "2024-03-15",
        time: String? = "20:30",
        venue: String? = "WiZink Center",
        status: WebMatchStatus = WebMatchStatus.SCHEDULED,
        homeScore: Int? = null,
        awayScore: Int? = null,
        round: String? = "Round 1",
        season: String = "2024-25"
    ) = MatchWebDto(
        id = id,
        homeTeamId = homeTeamId,
        homeTeamName = homeTeamName,
        homeTeamLogo = homeTeamLogo,
        awayTeamId = awayTeamId,
        awayTeamName = awayTeamName,
        awayTeamLogo = awayTeamLogo,
        date = date,
        time = time,
        venue = venue,
        status = status,
        homeScore = homeScore,
        awayScore = awayScore,
        round = round,
        season = season
    )
    
    fun createTestPlayer(
        code: String = "P001",
        name: String = "Juan",
        surname: String = "García",
        fullName: String = "Juan García",
        jersey: Int? = 23,
        position: PlayerPosition? = PlayerPosition.GUARD,
        height: String? = "1.85m",
        weight: String? = "80kg",
        dateOfBirth: String? = "1995-01-01",
        placeOfBirth: String? = "Madrid",
        nationality: String? = "ESP",
        experience: Int? = 5,
        profileImageUrl: String? = null
    ) = Player(
        code = code,
        name = name,
        surname = surname,
        fullName = fullName,
        jersey = jersey,
        position = position,
        height = height,
        weight = weight,
        dateOfBirth = dateOfBirth,
        placeOfBirth = placeOfBirth,
        nationality = nationality,
        experience = experience,
        profileImageUrl = profileImageUrl
    )
    
    fun createTestTeamRoster(
        teamCode: String = "MAD",
        teamName: String = "Real Madrid",
        season: String = "E2025"
    ) = TeamRoster(
        teamCode = teamCode,
        teamName = teamName,
        season = season,
        players = listOf(
            createTestPlayer(code = "P001", name = "Juan", jersey = 1),
            createTestPlayer(code = "P002", name = "Pedro", jersey = 2)
        ),
        coaches = emptyList()
    )
    
    fun createTestPlayerDto(
        code: String = "P001",
        name: String = "Juan",
        surname: String? = "García",
        jersey: Int? = 23,
        position: Int? = 1,
        height: Int? = 185,
        weight: Int? = 80
    ) = PlayerDto(
        person = PersonDto(
            code = code,
            name = name,
            surname = surname,
            height = height,
            weight = weight,
            dateOfBirth = "1995-01-01",
            placeOfBirth = "Madrid",
            nationality = "ESP"
        ),
        jersey = jersey,
        dorsalRaw = jersey?.toString(),
        position = position,
        positionName = "Guard",
        isActive = true,
        isStarter = false,
        isCaptain = false
    )
}
