package es.itram.basketmatch.testutil

import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus as WebMatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.CountryDto
import es.itram.basketmatch.data.datasource.remote.dto.PlayerDto
import es.itram.basketmatch.data.datasource.remote.dto.PersonDto
import es.itram.basketmatch.data.datasource.remote.dto.PlayerImageUrls
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
 * Factory para crear datos de prueba
 */
object TestDataFactory {

    // DOMAIN ENTITIES
    
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
        isFavorite: Boolean = false
    ): Team {
        return Team(
            id = id,
            name = name,
            shortName = shortName,
            code = code,
            city = city,
            country = country,
            logoUrl = logoUrl,
            founded = founded,
            coach = coach,
            isFavorite = isFavorite
        )
    }

    fun createTestTeamList(): List<Team> {
        return listOf(
            createTestTeam(id = "1", name = "Real Madrid", code = "MAD"),
            createTestTeam(id = "2", name = "FC Barcelona", code = "FCB"),
            createTestTeam(id = "3", name = "Fenerbahçe", code = "FEN")
        )
    }

    fun createTestMatch(
        id: String = "1",
        homeTeamId: String = "1",
        homeTeamName: String = "Real Madrid",
        homeTeamLogo: String? = "https://example.com/madrid.png",
        awayTeamId: String = "2",
        awayTeamName: String = "FC Barcelona",
        awayTeamLogo: String? = "https://example.com/barca.png",
        dateTime: LocalDateTime = LocalDateTime.now().plusDays(1),
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
            homeTeamLogo = homeTeamLogo,
            awayTeamId = awayTeamId,
            awayTeamName = awayTeamName,
            awayTeamLogo = awayTeamLogo,
            dateTime = dateTime,
            venue = venue,
            round = round,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore,
            seasonType = seasonType
        )
    }

    fun createTestMatchList(): List<Match> {
        return listOf(
            createTestMatch(id = "1", homeTeamName = "Real Madrid", awayTeamName = "FC Barcelona"),
            createTestMatch(id = "2", homeTeamName = "Fenerbahçe", awayTeamName = "Olympiacos"),
            createTestMatch(id = "3", homeTeamName = "CSKA Moscow", awayTeamName = "Anadolu Efes")
        )
    }

    fun createTestPlayer(
        code: String = "P001",
        name: String = "Sergio",
        surname: String = "Llull",
        fullName: String = "Sergio Llull",
        jersey: Int? = 23,
        position: PlayerPosition? = PlayerPosition.GUARD,
        height: String? = "190 cm",
        weight: String? = "85 kg",
        dateOfBirth: String? = "1987-11-15",
        placeOfBirth: String? = "Menorca, España",
        nationality: String? = "España",
        experience: Int? = 15,
        profileImageUrl: String? = "https://example.com/llull.jpg",
        isActive: Boolean = true,
        isStarter: Boolean = true,
        isCaptain: Boolean = true
    ): Player {
        return Player(
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
            profileImageUrl = profileImageUrl,
            isActive = isActive,
            isStarter = isStarter,
            isCaptain = isCaptain
        )
    }

    fun createTestPlayerList(): List<Player> {
        return listOf(
            createTestPlayer(code = "P001", name = "Sergio", surname = "Llull", jersey = 23),
            createTestPlayer(code = "P002", name = "Facundo", surname = "Campazzo", jersey = 7),
            createTestPlayer(code = "P003", name = "Edy", surname = "Tavares", jersey = 22)
        )
    }

    fun createTestTeamRoster(
        teamCode: String = "MAD",
        teamName: String = "Real Madrid",
        season: String = "2024-25",
        players: List<Player> = createTestPlayerList()
    ): TeamRoster {
        return TeamRoster(
            teamCode = teamCode,
            teamName = teamName,
            season = season,
            players = players,
            coaches = emptyList()
        )
    }

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

    // DATABASE ENTITIES
    
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
        secondaryColor: String = "#000000",
        isFavorite: Boolean = false
    ): TeamEntity {
        return TeamEntity(
            id = id,
            name = name,
            shortName = shortName,
            code = code,
            city = city,
            country = country,
            logoUrl = logoUrl,
            founded = founded,
            coach = coach,
            isFavorite = isFavorite
        )
    }

    fun createTestMatchEntity(
        id: String = "1",
        homeTeamId: String = "1",
        homeTeamName: String = "Real Madrid",
        homeTeamLogo: String? = "https://example.com/madrid.png",
        awayTeamId: String = "2",
        awayTeamName: String = "FC Barcelona",
        awayTeamLogo: String? = "https://example.com/barca.png",
        dateTime: LocalDateTime = LocalDateTime.now().plusDays(1),
        venue: String = "WiZink Center",
        round: Int = 1,
        status: MatchStatus = MatchStatus.SCHEDULED,
        homeScore: Int? = null,
        awayScore: Int? = null,
        seasonType: SeasonType = SeasonType.REGULAR
    ): MatchEntity {
        return MatchEntity(
            id = id,
            homeTeamId = homeTeamId,
            homeTeamName = homeTeamName,
            homeTeamLogo = homeTeamLogo,
            awayTeamId = awayTeamId,
            awayTeamName = awayTeamName,
            awayTeamLogo = awayTeamLogo,
            dateTime = dateTime,
            venue = venue,
            round = round,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore,
            seasonType = seasonType
        )
    }

    fun createTestPlayerEntity(
        id: String = "pe_1",
        teamCode: String = "MAD",
        playerCode: String = "P001",
        name: String = "Sergio",
        surname: String = "Llull",
        fullName: String = "Sergio Llull",
        jersey: Int? = 23,
        position: String? = "GUARD",
        height: String? = "190 cm",
        weight: String? = "85 kg",
        dateOfBirth: String? = "1987-11-15",
        placeOfBirth: String? = "Menorca, España",
        nationality: String? = "España",
        experience: Int? = 15,
        profileImageUrl: String? = "https://example.com/llull.jpg",
        isActive: Boolean = true,
        isStarter: Boolean = true,
        isCaptain: Boolean = true,
        lastUpdated: Long = System.currentTimeMillis()
    ): PlayerEntity {
        return PlayerEntity(
            id = id,
            teamCode = teamCode,
            playerCode = playerCode,
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
            profileImageUrl = profileImageUrl,
            isActive = isActive,
            isStarter = isStarter,
            isCaptain = isCaptain,
            lastUpdated = lastUpdated
        )
    }

    fun createTestTeamRosterEntity(
        teamCode: String = "MAD",
        teamName: String = "Real Madrid",
        season: String = "2024-25",
        lastUpdated: Long = System.currentTimeMillis()
    ): TeamRosterEntity {
        return TeamRosterEntity(
            teamCode = teamCode,
            teamName = teamName,
            season = season,
            lastUpdated = lastUpdated
        )
    }

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
    ): StandingEntity {
        return StandingEntity(
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

    // WEB DTOs
    
    fun createTestTeamWebDto(
        id: String = "1",
        name: String = "Real Madrid",
        fullName: String = "Real Madrid Club de Fútbol",
        shortCode: String = "RMA", 
        country: String = "España",
        logoUrl: String = "https://example.com/logo.png",
        venue: String = "Santiago Bernabéu",
        profileUrl: String = "https://realmadrid.com"
    ): TeamWebDto {
        return TeamWebDto(
            id = id,
            name = name,
            fullName = fullName,
            shortCode = shortCode,
            country = country,
            logoUrl = logoUrl,
            venue = venue,
            profileUrl = profileUrl
        )
    }

    fun createTestMatchWebDto(
        id: String = "1",
        homeTeamId: String = "1",
        homeTeamName: String = "Real Madrid",
        awayTeamId: String = "2",
        awayTeamName: String = "FC Barcelona",
        date: String = "2024-12-01",
        time: String = "20:00",
        venue: String = "WiZink Center",
        round: String = "1",
        status: WebMatchStatus = WebMatchStatus.SCHEDULED,
        homeScore: Int? = null,
        awayScore: Int? = null
    ): MatchWebDto {
        return MatchWebDto(
            id = id,
            homeTeamId = homeTeamId,
            homeTeamName = homeTeamName,
            awayTeamId = awayTeamId,
            awayTeamName = awayTeamName,
            date = date,
            time = time,
            venue = venue,
            round = round,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore
        )
    }

    fun createTestPlayerDto(
        code: String = "LUK77",
        name: String = "Luka",
        surname: String = "Dončić",
        jersey: Int = 77,
        position: Int = 1,
        positionName: String = "Point Guard",
        height: Int = 201,
        weight: Int = 104,
        dateOfBirth: String = "1999-02-28T00:00:00",
        nationality: String = "Slovenia",
        isActive: Boolean = true,
        isStarter: Boolean = true,
        isCaptain: Boolean = false
    ): es.itram.basketmatch.data.datasource.remote.dto.PlayerDto {
        return es.itram.basketmatch.data.datasource.remote.dto.PlayerDto(
            person = es.itram.basketmatch.data.datasource.remote.dto.PersonDto(
                code = code,
                name = name,
                passportSurname = surname,
                jerseyName = surname,
                height = height,
                weight = weight,
                birthDate = dateOfBirth,
                country = es.itram.basketmatch.data.datasource.remote.dto.CountryDto(
                    code = nationality.take(3).uppercase(),
                    name = nationality
                ),
                images = es.itram.basketmatch.data.datasource.remote.dto.PlayerImageUrls(
                    profile = "https://example.com/player/$code.jpg"
                )
            ),
            type = "J",
            typeName = "Player",
            active = isActive,
            dorsal = jersey.toString(),
            position = position,
            positionName = positionName
        )
    }
}
