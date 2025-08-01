package es.itram.basketmatch.presentation.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigationRoutesTest {

    @Test
    fun `main route should be correct`() {
        // Assert
        assertThat(NavigationRoutes.MAIN).isEqualTo("main")
    }

    @Test
    fun `calendar route should be correct`() {
        // Assert
        assertThat(NavigationRoutes.CALENDAR).isEqualTo("calendar")
    }

    @Test
    fun `player detail route should be correct`() {
        // Assert
        assertThat(NavigationRoutes.PLAYER_DETAIL).isEqualTo("player_detail/{playerCode}/{teamName}")
    }

    @Test
    fun `teamDetail function should generate correct route`() {
        // Arrange
        val teamId = "T001"

        // Act
        val result = NavigationRoutes.teamDetail(teamId)

        // Assert
        assertThat(result).contains("team_detail")
        assertThat(result).contains(teamId)
    }

    @Test
    fun `matchDetail function should generate correct route`() {
        // Arrange
        val matchId = "M001"

        // Act
        val result = NavigationRoutes.matchDetail(matchId)

        // Assert
        assertThat(result).contains("match_detail")
        assertThat(result).contains(matchId)
    }

    @Test
    fun `teamRoster function should generate correct route`() {
        // Arrange
        val teamTla = "MAD"
        val teamName = "Real Madrid"

        // Act
        val result = NavigationRoutes.teamRoster(teamTla, teamName)

        // Assert
        assertThat(result).contains("team_roster")
        assertThat(result).contains(teamTla)
        assertThat(result).contains(teamName)
    }

    @Test
    fun `playerDetail function should generate correct route`() {
        // Arrange
        val playerCode = "P004888"
        val teamName = "Barcelona"

        // Act
        val result = NavigationRoutes.playerDetail(playerCode, teamName)

        // Assert
        assertThat(result).contains("player_detail")
        assertThat(result).contains(playerCode)
        assertThat(result).contains(teamName)
    }
}
