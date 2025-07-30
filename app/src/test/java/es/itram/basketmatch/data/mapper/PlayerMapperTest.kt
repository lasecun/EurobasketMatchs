package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.remote.dto.*
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.utils.PlayerImageUtil
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlayerMapperTest {

    private val mockImageUtil = mockk<PlayerImageUtil>()

    private val countryDto = CountryDto(
        code = "ESP",
        name = "Spain"
    )

    private val personDto = PersonDto(
        code = "P001234",
        name = "John",
        passportSurname = "Smith",
        jerseyName = "SMITH",
        country = countryDto,
        height = 200,
        weight = 85,
        birthDate = "1995-06-15T00:00:00",
        birthCountry = countryDto
    )

    private val playerDto = PlayerDto(
        person = personDto,
        type = "J",
        typeName = "Player",
        active = true,
        dorsal = "10",
        position = 2,
        positionName = "Forward"
    )

    private val playerEntity = PlayerEntity(
        id = "MAD_P001234",
        teamCode = "MAD",
        playerCode = "P001234",
        name = "John",
        surname = "Smith",
        fullName = "John Smith",
        jersey = 10,
        position = "FORWARD",
        height = "200cm",
        weight = "85kg",
        dateOfBirth = "1995-06-15T00:00:00",
        placeOfBirth = "Spain",
        nationality = "Spain",
        experience = null,
        profileImageUrl = "https://example.com/player.jpg",
        isActive = true,
        isStarter = false,
        isCaptain = false,
        lastUpdated = 123456789L
    )

    @Test
    fun `fromDto should convert PlayerDto to Player correctly`() = runTest {
        // Arrange
        val teamCode = "MAD"
        val expectedImageUrl = "https://example.com/player.jpg"
        
        every { PlayerImageUtil.DEFAULT_PLAYER_IMAGES["P001234"] } returns null
        coEvery { mockImageUtil.getPlayerImageUrl("P001234", "John", teamCode) } returns expectedImageUrl

        // Act
        val result = PlayerMapper.fromDto(playerDto, teamCode, mockImageUtil)

        // Assert
        assertThat(result.code).isEqualTo("P001234")
        assertThat(result.name).isEqualTo("John")
        assertThat(result.surname).isEqualTo("Smith")
        assertThat(result.fullName).isEqualTo("John Smith")
        assertThat(result.jersey).isEqualTo(10)
        assertThat(result.position).isEqualTo(PlayerPosition.FORWARD)
        assertThat(result.height).isEqualTo("200cm")
        assertThat(result.weight).isEqualTo("85kg")
        assertThat(result.dateOfBirth).isEqualTo("1995-06-15T00:00:00")
        assertThat(result.placeOfBirth).isEqualTo("Spain")
        assertThat(result.nationality).isEqualTo("Spain")
        assertThat(result.profileImageUrl).isEqualTo(expectedImageUrl)
        assertThat(result.isActive).isTrue()
        assertThat(result.isStarter).isFalse()
        assertThat(result.isCaptain).isFalse()
    }

    @Test
    fun `fromDto should generate player code when dto code is null`() = runTest {
        // Arrange
        val playerDtoWithoutCode = playerDto.copy(
            person = personDto.copy(code = null),
            dorsal = "23"
        )
        val teamCode = "MAD"
        
        every { PlayerImageUtil.DEFAULT_PLAYER_IMAGES[any()] } returns null
        coEvery { mockImageUtil.getPlayerImageUrl(any(), any(), any()) } returns "https://example.com/player.jpg"

        // Act
        val result = PlayerMapper.fromDto(playerDtoWithoutCode, teamCode, mockImageUtil)

        // Assert
        assertThat(result.code).isEqualTo("JOHSMI23") // JOHn + SMIth + 23
    }

    @Test
    fun `fromDto should handle null optional fields`() = runTest {
        // Arrange
        val playerDtoWithNulls = PlayerDto(
            person = PersonDto(
                code = "P001",
                name = "John",
                passportSurname = null,
                jerseyName = null,
                country = null,
                height = null,
                weight = null,
                birthDate = null,
                birthCountry = null
            ),
            type = "J",
            typeName = "Player",
            active = true,
            dorsal = null,
            position = null,
            positionName = null
        )
        val teamCode = "MAD"
        
        every { PlayerImageUtil.DEFAULT_PLAYER_IMAGES[any()] } returns null
        coEvery { mockImageUtil.generatePlaceholderImageUrl("John") } returns "https://placeholder.com/player.jpg"

        // Act
        val result = PlayerMapper.fromDto(playerDtoWithNulls, teamCode, mockImageUtil)

        // Assert
        assertThat(result.code).isEqualTo("P001")
        assertThat(result.name).isEqualTo("John")
        assertThat(result.surname).isEqualTo("")
        assertThat(result.fullName).isEqualTo("John")
        assertThat(result.jersey).isNull()
        assertThat(result.position).isEqualTo(PlayerPosition.UNKNOWN)
        assertThat(result.height).isNull()
        assertThat(result.weight).isNull()
        assertThat(result.dateOfBirth).isNull()
        assertThat(result.placeOfBirth).isNull()
        assertThat(result.nationality).isNull()
    }

    @Test
    fun `fromDto should use default player image when available`() = runTest {
        // Arrange
        val teamCode = "MAD"
        val defaultImageUrl = "https://default.com/player.jpg"
        
        every { PlayerImageUtil.DEFAULT_PLAYER_IMAGES["P001234"] } returns defaultImageUrl

        // Act
        val result = PlayerMapper.fromDto(playerDto, teamCode, mockImageUtil)

        // Assert
        assertThat(result.profileImageUrl).isEqualTo(defaultImageUrl)
    }

    @Test
    fun `toEntity should convert Player to PlayerEntity correctly`() {
        // Arrange
        val player = Player(
            code = "P001234",
            name = "John",
            surname = "Smith",
            fullName = "John Smith",
            jersey = 10,
            position = PlayerPosition.FORWARD,
            height = "200cm",
            weight = "85kg",
            dateOfBirth = "1995-06-15T00:00:00",
            placeOfBirth = "Spain",
            nationality = "Spain",
            experience = 5,
            profileImageUrl = "https://example.com/player.jpg",
            isActive = true,
            isStarter = false,
            isCaptain = false
        )
        val teamCode = "MAD"

        // Act
        val result = PlayerMapper.toEntity(player, teamCode)

        // Assert
        assertThat(result.id).isEqualTo("MAD_P001234")
        assertThat(result.teamCode).isEqualTo("MAD")
        assertThat(result.playerCode).isEqualTo("P001234")
        assertThat(result.name).isEqualTo("John")
        assertThat(result.surname).isEqualTo("Smith")
        assertThat(result.fullName).isEqualTo("John Smith")
        assertThat(result.jersey).isEqualTo(10)
        assertThat(result.position).isEqualTo("FORWARD")
        assertThat(result.height).isEqualTo("200cm")
        assertThat(result.weight).isEqualTo("85kg")
        assertThat(result.dateOfBirth).isEqualTo("1995-06-15T00:00:00")
        assertThat(result.placeOfBirth).isEqualTo("Spain")
        assertThat(result.nationality).isEqualTo("Spain")
        assertThat(result.experience).isEqualTo(5)
        assertThat(result.profileImageUrl).isEqualTo("https://example.com/player.jpg")
        assertThat(result.isActive).isTrue()
        assertThat(result.isStarter).isFalse()
        assertThat(result.isCaptain).isFalse()
        assertThat(result.lastUpdated).isGreaterThan(0L)
    }

    @Test
    fun `fromEntity should convert PlayerEntity to Player correctly`() {
        // Act
        val result = PlayerMapper.fromEntity(playerEntity)

        // Assert
        assertThat(result.code).isEqualTo("P001234")
        assertThat(result.name).isEqualTo("John")
        assertThat(result.surname).isEqualTo("Smith")
        assertThat(result.fullName).isEqualTo("John Smith")
        assertThat(result.jersey).isEqualTo(10)
        assertThat(result.position).isEqualTo(PlayerPosition.FORWARD)
        assertThat(result.height).isEqualTo("200cm")
        assertThat(result.weight).isEqualTo("85kg")
        assertThat(result.dateOfBirth).isEqualTo("1995-06-15T00:00:00")
        assertThat(result.placeOfBirth).isEqualTo("Spain")
        assertThat(result.nationality).isEqualTo("Spain")
        assertThat(result.experience).isNull()
        assertThat(result.profileImageUrl).isEqualTo("https://example.com/player.jpg")
        assertThat(result.isActive).isTrue()
        assertThat(result.isStarter).isFalse()
        assertThat(result.isCaptain).isFalse()
    }

    @Test
    fun `fromEntity should handle null position gracefully`() {
        // Arrange
        val entityWithNullPosition = playerEntity.copy(position = null)

        // Act
        val result = PlayerMapper.fromEntity(entityWithNullPosition)

        // Assert
        assertThat(result.position).isNull()
    }

    @Test
    fun `fromDtoListToEntityList should filter only players and convert correctly`() = runTest {
        // Arrange
        val coachDto = playerDto.copy(type = "E", typeName = "Coach")
        val dtoList = listOf(playerDto, coachDto)
        val teamCode = "MAD"
        
        every { PlayerImageUtil.DEFAULT_PLAYER_IMAGES[any()] } returns null
        coEvery { mockImageUtil.getPlayerImageUrl(any(), any(), any()) } returns "https://example.com/player.jpg"

        // Act
        val result = PlayerMapper.fromDtoListToEntityList(dtoList, teamCode, mockImageUtil)

        // Assert
        assertThat(result).hasSize(1) // Only the player, not the coach
        assertThat(result[0].teamCode).isEqualTo(teamCode)
        assertThat(result[0].playerCode).isEqualTo("P001234")
    }

    @Test
    fun `fromEntityListToDomainList should convert entity list to domain list correctly`() {
        // Arrange
        val entityList = listOf(playerEntity, playerEntity.copy(id = "MAD_P002", playerCode = "P002"))

        // Act
        val result = PlayerMapper.fromEntityListToDomainList(entityList)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].code).isEqualTo("P001234")
        assertThat(result[1].code).isEqualTo("P002")
    }

    @Test
    fun `fromDto should handle different position numbers correctly`() = runTest {
        // Arrange
        val guardDto = playerDto.copy(position = 1, positionName = "Guard")
        val centerDto = playerDto.copy(position = 3, positionName = "Center")
        val teamCode = "MAD"
        
        every { PlayerImageUtil.DEFAULT_PLAYER_IMAGES[any()] } returns null
        coEvery { mockImageUtil.getPlayerImageUrl(any(), any(), any()) } returns "https://example.com/player.jpg"

        // Act
        val guardResult = PlayerMapper.fromDto(guardDto, teamCode, mockImageUtil)
        val centerResult = PlayerMapper.fromDto(centerDto, teamCode, mockImageUtil)

        // Assert
        assertThat(guardResult.position).isEqualTo(PlayerPosition.GUARD)
        assertThat(centerResult.position).isEqualTo(PlayerPosition.CENTER)
    }
}

// Tests para TeamRosterMapper que está dentro del archivo PlayerMapper.kt
class TeamRosterMapperTest {

    private val samplePlayers = listOf(
        Player(
            code = "P001",
            name = "John",
            surname = "Smith",
            fullName = "John Smith",
            jersey = 10,
            position = PlayerPosition.FORWARD,
            height = "200cm",
            weight = "85kg",
            dateOfBirth = "1995-06-15T00:00:00",
            placeOfBirth = "Spain",
            nationality = "Spain",
            experience = 5,
            profileImageUrl = "https://example.com/player1.jpg",
            isActive = true,
            isStarter = true,
            isCaptain = false
        ),
        Player(
            code = "P002",
            name = "Mike",
            surname = "Johnson",
            fullName = "Mike Johnson",
            jersey = 7,
            position = PlayerPosition.GUARD,
            height = "185cm",
            weight = "78kg",
            dateOfBirth = "1993-03-20T00:00:00",
            placeOfBirth = "USA",
            nationality = "USA",
            experience = 8,
            profileImageUrl = "https://example.com/player2.jpg",
            isActive = true,
            isStarter = false,
            isCaptain = true
        )
    )

    private val sampleTeamRoster = TeamRoster(
        teamCode = "MAD",
        teamName = "Real Madrid",
        season = "E2025",
        players = samplePlayers,
        coaches = emptyList(),
        logoUrl = "https://example.com/real-madrid-logo.png"
    )

    private val sampleTeamRosterEntity = TeamRosterEntity(
        teamCode = "MAD",
        teamName = "Real Madrid",
        season = "E2025",
        logoUrl = "https://example.com/real-madrid-logo.png",
        lastUpdated = 1234567890L
    )

    @Test
    fun `toEntity should convert TeamRoster to TeamRosterEntity correctly`() {
        // Act
        val result = TeamRosterMapper.toEntity(sampleTeamRoster)

        // Assert
        assertThat(result.teamCode).isEqualTo("MAD")
        assertThat(result.teamName).isEqualTo("Real Madrid")
        assertThat(result.season).isEqualTo("E2025")
        assertThat(result.logoUrl).isEqualTo("https://example.com/real-madrid-logo.png")
        assertThat(result.lastUpdated).isGreaterThan(0L)
    }

    @Test
    fun `fromEntity should convert TeamRosterEntity to TeamRoster correctly`() {
        // Act
        val result = TeamRosterMapper.fromEntity(sampleTeamRosterEntity, samplePlayers)

        // Assert
        assertThat(result.teamCode).isEqualTo("MAD")
        assertThat(result.teamName).isEqualTo("Real Madrid")
        assertThat(result.season).isEqualTo("E2025")
        assertThat(result.logoUrl).isEqualTo("https://example.com/real-madrid-logo.png")
        assertThat(result.players).hasSize(2)
        assertThat(result.players[0].code).isEqualTo("P001")
        assertThat(result.players[1].code).isEqualTo("P002")
        assertThat(result.coaches).isEmpty()
    }

    @Test
    fun `toEntity should handle null logoUrl correctly`() {
        // Arrange
        val rosterWithNullLogo = sampleTeamRoster.copy(logoUrl = null)

        // Act
        val result = TeamRosterMapper.toEntity(rosterWithNullLogo)

        // Assert
        assertThat(result.logoUrl).isNull()
        assertThat(result.teamCode).isEqualTo("MAD")
        assertThat(result.teamName).isEqualTo("Real Madrid")
    }

    @Test
    fun `fromEntity should handle null logoUrl correctly`() {
        // Arrange
        val entityWithNullLogo = sampleTeamRosterEntity.copy(logoUrl = null)

        // Act
        val result = TeamRosterMapper.fromEntity(entityWithNullLogo, samplePlayers)

        // Assert
        assertThat(result.logoUrl).isNull()
        assertThat(result.teamCode).isEqualTo("MAD")
        assertThat(result.teamName).isEqualTo("Real Madrid")
        assertThat(result.players).hasSize(2)
    }

    @Test
    fun `fromEntity should handle empty players list correctly`() {
        // Arrange
        val emptyPlayersList = emptyList<Player>()

        // Act
        val result = TeamRosterMapper.fromEntity(sampleTeamRosterEntity, emptyPlayersList)

        // Assert
        assertThat(result.players).isEmpty()
        assertThat(result.teamCode).isEqualTo("MAD")
        assertThat(result.teamName).isEqualTo("Real Madrid")
        assertThat(result.coaches).isEmpty()
    }

    @Test
    fun `toEntity should preserve all team information`() {
        // Arrange
        val complexTeamRoster = TeamRoster(
            teamCode = "FCB",
            teamName = "FC Barcelona Basket",
            season = "E2024",
            players = listOf(
                Player(
                    code = "P123",
                    name = "Test",
                    surname = "Player",
                    fullName = "Test Player",
                    jersey = 99,
                    position = PlayerPosition.CENTER,
                    height = "210cm",
                    weight = "95kg",
                    dateOfBirth = "1990-01-01T00:00:00",
                    placeOfBirth = "Barcelona",
                    nationality = "Spain",
                    experience = 10,
                    profileImageUrl = "https://example.com/test.jpg",
                    isActive = true,
                    isStarter = true,
                    isCaptain = true
                )
            ),
            coaches = emptyList(),
            logoUrl = "https://fcbarcelona.com/logo.png"
        )

        // Act
        val result = TeamRosterMapper.toEntity(complexTeamRoster)

        // Assert
        assertThat(result.teamCode).isEqualTo("FCB")
        assertThat(result.teamName).isEqualTo("FC Barcelona Basket")
        assertThat(result.season).isEqualTo("E2024")
        assertThat(result.logoUrl).isEqualTo("https://fcbarcelona.com/logo.png")
        assertThat(result.lastUpdated).isGreaterThan(0L)
    }

    @Test
    fun `fromEntity should work with different player configurations`() {
        // Arrange
        val diversePlayers = listOf(
            Player(
                code = "P001",
                name = "Juan",
                surname = "García",
                fullName = "Juan García",
                jersey = 4,
                position = PlayerPosition.POINT_GUARD,
                height = "180cm",
                weight = "75kg",
                dateOfBirth = "1992-12-25T00:00:00",
                placeOfBirth = "Madrid",
                nationality = "Spain",
                experience = 12,
                profileImageUrl = "https://example.com/juan.jpg",
                isActive = true,
                isStarter = true,
                isCaptain = true
            ),
            Player(
                code = "P002",
                name = "Nikola",
                surname = "Mirotic",
                fullName = "Nikola Mirotic",
                jersey = 33,
                position = PlayerPosition.POWER_FORWARD,
                height = "208cm",
                weight = "102kg",
                dateOfBirth = "1991-02-11T00:00:00",
                placeOfBirth = "Montenegro",
                nationality = "Montenegro",
                experience = 15,
                profileImageUrl = "https://example.com/nikola.jpg",
                isActive = true,
                isStarter = true,
                isCaptain = false
            ),
            Player(
                code = "P003",
                name = "Young",
                surname = "Rookie",
                fullName = "Young Rookie",
                jersey = 1,
                position = PlayerPosition.SHOOTING_GUARD,
                height = "195cm",
                weight = "82kg",
                dateOfBirth = "2002-08-10T00:00:00",
                placeOfBirth = "Lithuania",
                nationality = "Lithuania",
                experience = 0,
                profileImageUrl = "https://example.com/rookie.jpg",
                isActive = true,
                isStarter = false,
                isCaptain = false
            )
        )

        val entityForDiverseTeam = TeamRosterEntity(
            teamCode = "ZAL",
            teamName = "Zalgiris Kaunas",
            season = "E2025",
            logoUrl = "https://zalgiris.lt/logo.png",
            lastUpdated = 9876543210L
        )

        // Act
        val result = TeamRosterMapper.fromEntity(entityForDiverseTeam, diversePlayers)

        // Assert
        assertThat(result.teamCode).isEqualTo("ZAL")
        assertThat(result.teamName).isEqualTo("Zalgiris Kaunas")
        assertThat(result.players).hasSize(3)
        assertThat(result.players[0].jersey).isEqualTo(4)
        assertThat(result.players[0].position).isEqualTo(PlayerPosition.POINT_GUARD)
        assertThat(result.players[0].isCaptain).isTrue()
        assertThat(result.players[1].jersey).isEqualTo(33)
        assertThat(result.players[1].position).isEqualTo(PlayerPosition.POWER_FORWARD)
        assertThat(result.players[1].experience).isEqualTo(15)
        assertThat(result.players[2].jersey).isEqualTo(1)
        assertThat(result.players[2].experience).isEqualTo(0)
    }

    @Test
    fun `conversion should be bidirectional and preserve data`() {
        // Act - conversión de ida y vuelta
        val entityResult = TeamRosterMapper.toEntity(sampleTeamRoster)
        val backToDomain = TeamRosterMapper.fromEntity(entityResult, samplePlayers)

        // Assert - los datos principales deben coincidir (excepto lastUpdated que se genera)
        assertThat(backToDomain.teamCode).isEqualTo(sampleTeamRoster.teamCode)
        assertThat(backToDomain.teamName).isEqualTo(sampleTeamRoster.teamName)
        assertThat(backToDomain.season).isEqualTo(sampleTeamRoster.season)
        assertThat(backToDomain.logoUrl).isEqualTo(sampleTeamRoster.logoUrl)
        assertThat(backToDomain.players).hasSize(sampleTeamRoster.players.size)
        assertThat(backToDomain.coaches).isEmpty()
    }

    @Test
    fun `toEntity should handle special characters in team names and URLs`() {
        // Arrange
        val rosterWithSpecialChars = TeamRoster(
            teamCode = "ÑBA",
            teamName = "Ñoño's Basketball & Athletics Club (2024/25)",
            season = "E2025",
            players = emptyList(),
            coaches = emptyList(),
            logoUrl = "https://example.com/logos/special-chars-team.png?v=1.0&size=large&encoding=utf-8"
        )

        // Act
        val result = TeamRosterMapper.toEntity(rosterWithSpecialChars)

        // Assert
        assertThat(result.teamCode).isEqualTo("ÑBA")
        assertThat(result.teamName).isEqualTo("Ñoño's Basketball & Athletics Club (2024/25)")
        assertThat(result.logoUrl).contains("special-chars-team.png")
        assertThat(result.logoUrl).contains("encoding=utf-8")
    }

    @Test
    fun `fromEntity should handle edge case with very long season names`() {
        // Arrange
        val entityWithLongSeason = TeamRosterEntity(
            teamCode = "TST",
            teamName = "Test Team",
            season = "EuroLeague 2024-2025 Regular Season and Playoffs Championship Edition",
            logoUrl = "https://example.com/logo.png",
            lastUpdated = 1234567890L
        )

        // Act
        val result = TeamRosterMapper.fromEntity(entityWithLongSeason, emptyList())

        // Assert
        assertThat(result.season).isEqualTo("EuroLeague 2024-2025 Regular Season and Playoffs Championship Edition")
        assertThat(result.teamCode).isEqualTo("TST")
        assertThat(result.players).isEmpty()
    }
}
