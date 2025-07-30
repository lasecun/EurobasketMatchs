package es.itram.basketmatch.presentation.navigation

import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerNavigationHelperTest {

    private val testPlayer = Player(
        code = "P001234",
        name = "John",
        surname = "Smith",
        fullName = "John Smith",
        jersey = 10,
        position = PlayerPosition.FORWARD,
        height = "200cm",
        weight = "85kg",
        dateOfBirth = "1995-06-15",
        placeOfBirth = "Madrid, Spain",
        nationality = "Spanish",
        experience = 5,
        profileImageUrl = "https://example.com/player.jpg",
        isActive = true,
        isStarter = false,
        isCaptain = false
    )
    
    private val testTeamName = "Real Madrid"

    @Before
    fun setUp() {
        // Limpiar el estado antes de cada test
        PlayerNavigationHelper.clearSelection()
    }

    @After
    fun tearDown() {
        // Limpiar el estado después de cada test
        PlayerNavigationHelper.clearSelection()
    }

    @Test
    fun `setSelectedPlayer and getSelectedPlayer should work correctly`() {
        // Arrange & Act
        PlayerNavigationHelper.setSelectedPlayer(testPlayer, testTeamName)
        
        val retrievedPlayer = PlayerNavigationHelper.getSelectedPlayer()
        val retrievedTeamName = PlayerNavigationHelper.getSelectedTeamName()

        // Assert
        assertThat(retrievedPlayer).isEqualTo(testPlayer)
        assertThat(retrievedTeamName).isEqualTo(testTeamName)
    }

    @Test
    fun `getSelectedPlayer should return null when no player is set`() {
        // Act
        val retrievedPlayer = PlayerNavigationHelper.getSelectedPlayer()
        val retrievedTeamName = PlayerNavigationHelper.getSelectedTeamName()

        // Assert
        assertThat(retrievedPlayer).isNull()
        assertThat(retrievedTeamName).isNull()
    }

    @Test
    fun `clearSelection should remove stored player and team`() {
        // Arrange
        PlayerNavigationHelper.setSelectedPlayer(testPlayer, testTeamName)
        
        // Act
        PlayerNavigationHelper.clearSelection()
        
        val retrievedPlayer = PlayerNavigationHelper.getSelectedPlayer()
        val retrievedTeamName = PlayerNavigationHelper.getSelectedTeamName()

        // Assert
        assertThat(retrievedPlayer).isNull()
        assertThat(retrievedTeamName).isNull()
    }

    @Test
    fun `setSelectedPlayer should replace previous selection`() {
        // Arrange
        val firstPlayer = testPlayer.copy(code = "P001", name = "First Player")
        val firstTeam = "First Team"
        
        val secondPlayer = testPlayer.copy(code = "P002", name = "Second Player")
        val secondTeam = "Second Team"

        // Act
        PlayerNavigationHelper.setSelectedPlayer(firstPlayer, firstTeam)
        PlayerNavigationHelper.setSelectedPlayer(secondPlayer, secondTeam)
        
        val retrievedPlayer = PlayerNavigationHelper.getSelectedPlayer()
        val retrievedTeamName = PlayerNavigationHelper.getSelectedTeamName()

        // Assert
        assertThat(retrievedPlayer).isEqualTo(secondPlayer)
        assertThat(retrievedTeamName).isEqualTo(secondTeam)
    }

    @Test
    fun `concurrent access should be thread safe`() {
        // Este test verifica que el singleton es thread-safe
        val players = listOf(
            testPlayer.copy(code = "P001", name = "Player 1"),
            testPlayer.copy(code = "P002", name = "Player 2"),
            testPlayer.copy(code = "P003", name = "Player 3")
        )

        // Act - Simular acceso concurrente
        val threads = players.map { player ->
            Thread {
                PlayerNavigationHelper.setSelectedPlayer(player, "Team ${player.code}")
                Thread.sleep(10) // Pequeña pausa para simular concurrencia
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Assert - Verificar que tenemos un estado válido
        val retrievedPlayer = PlayerNavigationHelper.getSelectedPlayer()
        val retrievedTeamName = PlayerNavigationHelper.getSelectedTeamName()

        assertThat(retrievedPlayer).isNotNull()
        assertThat(retrievedTeamName).isNotNull()
        assertThat(players).contains(retrievedPlayer)
    }
}
