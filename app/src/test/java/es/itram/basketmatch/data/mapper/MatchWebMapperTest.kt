package es.itram.basketmatch.data.mapper

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus as WebMatchStatus
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests para MatchWebMapper - mapping de MatchWebDto a Match de dominio
 */
class MatchWebMapperTest {

    @Test
    fun `toDomain should map MatchWebDto to Match correctly with all fields`() {
        // Given
        val matchWebDto = MatchWebDto(
            id = "match123",
            homeTeamId = "rea",
            homeTeamName = "Real Madrid",
            homeTeamLogo = "https://example.com/real-madrid.png",
            awayTeamId = "fcb",
            awayTeamName = "FC Barcelona",
            awayTeamLogo = "https://example.com/barcelona.png",
            date = "2025-01-15",
            time = "20:30",
            venue = "WiZink Center",
            status = WebMatchStatus.SCHEDULED,
            homeScore = null,
            awayScore = null,
            round = "15",
            season = "2024-25"
        )

        // When
        val result = MatchWebMapper.toDomain(matchWebDto)

        // Then
        assertThat(result.id).isEqualTo("match123")
        assertThat(result.homeTeamId).isEqualTo("rea")
        assertThat(result.homeTeamName).isEqualTo("Real Madrid") // Usa el nombre del DTO
        assertThat(result.homeTeamLogo).isEqualTo("https://example.com/real-madrid.png")
        assertThat(result.awayTeamId).isEqualTo("fcb")
        assertThat(result.awayTeamName).isEqualTo("FC Barcelona") // Usa el nombre del DTO
        assertThat(result.awayTeamLogo).isEqualTo("https://example.com/barcelona.png")
        assertThat(result.dateTime).isEqualTo(LocalDateTime.of(2025, 1, 15, 20, 30, 0))
        assertThat(result.venue).isEqualTo("WiZink Center")
        assertThat(result.round).isEqualTo(15)
        assertThat(result.status).isEqualTo(MatchStatus.SCHEDULED)
        assertThat(result.homeScore).isNull()
        assertThat(result.awayScore).isNull()
        assertThat(result.seasonType).isEqualTo(SeasonType.REGULAR)
    }

    @Test
    fun `toDomain should handle finished match with scores`() {
        // Given
        val finishedMatch = MatchWebDto(
            id = "finished123",
            homeTeamId = "oly",
            homeTeamName = "Olympiacos Piraeus",
            homeTeamLogo = null,
            awayTeamId = "pan",
            awayTeamName = "Panathinaikos Athens",
            awayTeamLogo = null,
            date = "2025-01-10",
            time = "19:00",
            venue = "Peace and Friendship Stadium",
            status = WebMatchStatus.FINISHED,
            homeScore = 85,
            awayScore = 78,
            round = "12"
        )

        // When
        val result = MatchWebMapper.toDomain(finishedMatch)

        // Then
        assertThat(result.status).isEqualTo(MatchStatus.FINISHED)
        assertThat(result.homeScore).isEqualTo(85)
        assertThat(result.awayScore).isEqualTo(78)
        assertThat(result.dateTime).isEqualTo(LocalDateTime.of(2025, 1, 10, 19, 0, 0))
    }

    @Test
    fun `toDomain should generate team names from TLA when DTO names are missing`() {
        // Given
        val matchWithMissingNames = MatchWebDto(
            id = "match456",
            homeTeamId = "bas",
            homeTeamName = "", // Nombre vacío
            homeTeamLogo = null,
            awayTeamId = "mta",
            awayTeamName = "", // Nombre vacío
            awayTeamLogo = null,
            date = "2025-02-01",
            time = "18:45",
            status = WebMatchStatus.LIVE
        )

        // When
        val result = MatchWebMapper.toDomain(matchWithMissingNames)

        // Then
        assertThat(result.homeTeamName).isEqualTo("Baskonia Vitoria-Gasteiz") // Generado desde TLA
        assertThat(result.awayTeamName).isEqualTo("Maccabi Playtika Tel Aviv") // Generado desde TLA
        assertThat(result.status).isEqualTo(MatchStatus.LIVE)
    }

    @Test
    fun `toDomain should generate team logos when DTO logos are null`() {
        // Given
        val matchWithoutLogos = MatchWebDto(
            id = "match789",
            homeTeamId = "zal",
            homeTeamName = "Zalgiris",
            homeTeamLogo = null,
            awayTeamId = "efs",
            awayTeamName = "Anadolu Efes",
            awayTeamLogo = null,
            date = "2025-02-05",
            status = WebMatchStatus.SCHEDULED
        )

        // When
        val result = MatchWebMapper.toDomain(matchWithoutLogos)

        // Then
        assertThat(result.homeTeamLogo).isEqualTo("https://img.euroleaguebasketball.net/design/ec/logos/clubs/zalgiris-kaunas.png")
        assertThat(result.awayTeamLogo).isEqualTo("https://img.euroleaguebasketball.net/design/ec/logos/clubs/anadolu-efes-istanbul.png")
    }

    @Test
    fun `toDomain should handle null optional fields correctly`() {
        // Given
        val minimalMatch = MatchWebDto(
            id = "minimal",
            homeTeamId = "unknown",
            homeTeamName = "Unknown Team",
            homeTeamLogo = null,
            awayTeamId = "another",
            awayTeamName = "Another Team",
            awayTeamLogo = null,
            date = "2025-03-01",
            time = null, // Sin hora específica
            venue = null, // Sin venue
            status = WebMatchStatus.SCHEDULED,
            homeScore = null,
            awayScore = null,
            round = null // Sin round
        )

        // When
        val result = MatchWebMapper.toDomain(minimalMatch)

        // Then
        assertThat(result.id).isEqualTo("minimal")
        assertThat(result.dateTime).isEqualTo(LocalDateTime.of(2025, 3, 1, 20, 0, 0)) // Hora por defecto 20:00
        assertThat(result.venue).isEmpty()
        assertThat(result.round).isEqualTo(1) // Round por defecto
        assertThat(result.homeTeamLogo).isNull() // No hay logo para TLA desconocido
        assertThat(result.awayTeamLogo).isNull()
    }

    @Test
    fun `toDomain should handle all match statuses correctly`() {
        // Given
        val baseMatch = MatchWebDto(
            id = "status_test",
            homeTeamId = "test",
            homeTeamName = "Test Home",
            awayTeamId = "test2",
            awayTeamName = "Test Away",
            date = "2025-01-20"
        )

        // When & Then - Probar todos los status
        val scheduledResult = MatchWebMapper.toDomain(baseMatch.copy(status = WebMatchStatus.SCHEDULED))
        assertThat(scheduledResult.status).isEqualTo(MatchStatus.SCHEDULED)

        val liveResult = MatchWebMapper.toDomain(baseMatch.copy(status = WebMatchStatus.LIVE))
        assertThat(liveResult.status).isEqualTo(MatchStatus.LIVE)

        val finishedResult = MatchWebMapper.toDomain(baseMatch.copy(status = WebMatchStatus.FINISHED))
        assertThat(finishedResult.status).isEqualTo(MatchStatus.FINISHED)

        val postponedResult = MatchWebMapper.toDomain(baseMatch.copy(status = WebMatchStatus.POSTPONED))
        assertThat(postponedResult.status).isEqualTo(MatchStatus.POSTPONED)

        val cancelledResult = MatchWebMapper.toDomain(baseMatch.copy(status = WebMatchStatus.CANCELLED))
        assertThat(cancelledResult.status).isEqualTo(MatchStatus.CANCELLED)
    }

    @Test
    fun `toDomain should handle invalid date formats gracefully`() {
        // Given
        val invalidDateMatch = MatchWebDto(
            id = "invalid_date",
            homeTeamId = "test",
            homeTeamName = "Test Home",
            awayTeamId = "test2",
            awayTeamName = "Test Away",
            date = "invalid-date-format",
            time = "invalid-time"
        )

        // When
        val result = MatchWebMapper.toDomain(invalidDateMatch)

        // Then - Debe usar fecha por defecto (mañana)
        assertThat(result.dateTime.isAfter(LocalDateTime.now())).isTrue()
        assertThat(result.id).isEqualTo("invalid_date")
    }

    @Test
    fun `toDomain should handle invalid round numbers`() {
        // Given
        val invalidRoundMatch = MatchWebDto(
            id = "invalid_round",
            homeTeamId = "test",
            homeTeamName = "Test Home",
            awayTeamId = "test2",
            awayTeamName = "Test Away",
            date = "2025-01-15",
            round = "not-a-number"
        )

        // When
        val result = MatchWebMapper.toDomain(invalidRoundMatch)

        // Then
        assertThat(result.round).isEqualTo(1) // Round por defecto
    }

    @Test
    fun `toDomain should extract TLA from complex team IDs`() {
        // Given
        val complexIdMatch = MatchWebDto(
            id = "complex_id",
            homeTeamId = "euroleague_rea_2025", // TLA embebido
            homeTeamName = "",
            awayTeamId = "team_fcb_official", // TLA embebido
            awayTeamName = "",
            date = "2025-01-15"
        )

        // When
        val result = MatchWebMapper.toDomain(complexIdMatch)

        // Then
        assertThat(result.homeTeamName).isEqualTo("Real Madrid") // Extraído desde TLA "rea"
        assertThat(result.awayTeamName).isEqualTo("FC Barcelona") // Extraído desde TLA "fcb"
    }

    @Test
    fun `toDomainList should convert empty list correctly`() {
        // Given
        val emptyList = emptyList<MatchWebDto>()

        // When
        val result = MatchWebMapper.toDomainList(emptyList)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `toDomainList should convert single match correctly`() {
        // Given
        val singleMatchList = listOf(
            MatchWebDto(
                id = "single",
                homeTeamId = "vir",
                homeTeamName = "Virtus Bologna",
                awayTeamId = "red",
                awayTeamName = "Milan",
                date = "2025-01-25",
                time = "21:00",
                status = WebMatchStatus.SCHEDULED
            )
        )

        // When
        val result = MatchWebMapper.toDomainList(singleMatchList)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("single")
        assertThat(result[0].homeTeamName).isEqualTo("Virtus Bologna")
        assertThat(result[0].awayTeamName).isEqualTo("Milan")
        assertThat(result[0].dateTime).isEqualTo(LocalDateTime.of(2025, 1, 25, 21, 0, 0))
    }

    @Test
    fun `toDomainList should convert multiple matches correctly`() {
        // Given
        val multipleMatchesList = listOf(
            MatchWebDto(
                id = "match1",
                homeTeamId = "ulk",
                homeTeamName = "Fenerbahce",
                awayTeamId = "par",
                awayTeamName = "Paris Basketball",
                date = "2025-02-01",
                time = "19:30",
                status = WebMatchStatus.SCHEDULED
            ),
            MatchWebDto(
                id = "match2",
                homeTeamId = "bay",
                homeTeamName = "Bayern Munich",
                awayTeamId = "vil",
                awayTeamName = "ASVEL",
                date = "2025-02-02",
                time = "20:00",
                status = WebMatchStatus.LIVE,
                homeScore = 45,
                awayScore = 42
            ),
            MatchWebDto(
                id = "match3",
                homeTeamId = "val",
                homeTeamName = "Valencia Basket",
                awayTeamId = "asm",
                awayTeamName = "AS Monaco",
                date = "2025-02-03",
                time = "18:00",
                status = WebMatchStatus.FINISHED,
                homeScore = 88,
                awayScore = 91
            )
        )

        // When
        val result = MatchWebMapper.toDomainList(multipleMatchesList)

        // Then
        assertThat(result).hasSize(3)
        
        // Verificar primer partido
        assertThat(result[0].id).isEqualTo("match1")
        assertThat(result[0].status).isEqualTo(MatchStatus.SCHEDULED)
        assertThat(result[0].homeScore).isNull()
        assertThat(result[0].awayScore).isNull()
        
        // Verificar segundo partido
        assertThat(result[1].id).isEqualTo("match2")
        assertThat(result[1].status).isEqualTo(MatchStatus.LIVE)
        assertThat(result[1].homeScore).isEqualTo(45)
        assertThat(result[1].awayScore).isEqualTo(42)
        
        // Verificar tercer partido
        assertThat(result[2].id).isEqualTo("match3")
        assertThat(result[2].status).isEqualTo(MatchStatus.FINISHED)
        assertThat(result[2].homeScore).isEqualTo(88)
        assertThat(result[2].awayScore).isEqualTo(91)
    }

    @Test
    fun `toDomain should use default time when time is null`() {
        // Given
        val matchWithoutTime = MatchWebDto(
            id = "no_time",
            homeTeamId = "test",
            homeTeamName = "Test Home",
            awayTeamId = "test2",
            awayTeamName = "Test Away",
            date = "2025-01-30",
            time = null
        )

        // When
        val result = MatchWebMapper.toDomain(matchWithoutTime)

        // Then
        assertThat(result.dateTime).isEqualTo(LocalDateTime.of(2025, 1, 30, 20, 0, 0)) // 20:00 por defecto
    }

    @Test
    fun `toDomain should preserve all season types as REGULAR`() {
        // Given
        val regularSeasonMatch = MatchWebDto(
            id = "regular",
            homeTeamId = "test",
            homeTeamName = "Test Home",
            awayTeamId = "test2",
            awayTeamName = "Test Away",
            date = "2025-01-15",
            season = "2024-25"
        )

        // When
        val result = MatchWebMapper.toDomain(regularSeasonMatch)

        // Then
        assertThat(result.seasonType).isEqualTo(SeasonType.REGULAR) // Siempre REGULAR según el mapper
    }

    @Test
    fun `toDomain should handle empty team IDs gracefully`() {
        // Given
        val emptyTeamIdsMatch = MatchWebDto(
            id = "empty_teams",
            homeTeamId = "",
            homeTeamName = "Home Team",
            awayTeamId = "",
            awayTeamName = "Away Team",
            date = "2025-01-15"
        )

        // When
        val result = MatchWebMapper.toDomain(emptyTeamIdsMatch)

        // Then
        assertThat(result.homeTeamId).isEmpty()
        assertThat(result.awayTeamId).isEmpty()
        assertThat(result.homeTeamName).isEqualTo("Home Team") // Usa el nombre del DTO
        assertThat(result.awayTeamName).isEqualTo("Away Team") // Usa el nombre del DTO
        assertThat(result.homeTeamLogo).isNull() // No puede generar logo sin TLA
        assertThat(result.awayTeamLogo).isNull()
    }
}
