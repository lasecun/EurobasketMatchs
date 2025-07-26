package es.itram.basketmatch.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetAllTeamsUseCaseTest {

    // Mock
    private val teamRepository: TeamRepository = mockk()

    // System under test
    private lateinit var useCase: GetAllTeamsUseCase

    // Test data
    private val testTeams = listOf(
        Team(
            id = "1",
            name = "Real Madrid",
            city = "Madrid",
            country = "España",
            logoUrl = "https://example.com/logo1.png",
            shortName = "RMA",
            code = "MAD",
            founded = 1902,
            coach = "Chus Mateo",
            isFavorite = false
        ),
        Team(
            id = "2",
            name = "FC Barcelona", 
            city = "Barcelona",
            country = "España",
            logoUrl = "https://example.com/logo2.png",
            shortName = "BAR",
            code = "FCB",
            founded = 1899,
            coach = "Joan Peñarroya",
            isFavorite = true
        )
    )

    @Before
    fun setup() {
        useCase = GetAllTeamsUseCase(teamRepository)
    }

    @Test
    fun `when repository returns teams, then usecase emits same teams`() = runTest {
        // Given
        coEvery { teamRepository.getAllTeams() } returns flowOf(testTeams)

        // When
        val result = useCase()

        // Then
        result.test {
            val teams = awaitItem()
            assertThat(teams).hasSize(2)
            assertThat(teams[0].name).isEqualTo("Real Madrid")
            assertThat(teams[1].name).isEqualTo("FC Barcelona")
            assertThat(teams[1].isFavorite).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns empty list, then usecase emits empty list`() = runTest {
        // Given
        coEvery { teamRepository.getAllTeams() } returns flowOf(emptyList())

        // When
        val result = useCase()

        // Then
        result.test {
            val teams = awaitItem()
            assertThat(teams).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `when repository throws exception, then usecase propagates exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { teamRepository.getAllTeams() } throws exception

        // When & Then
        try {
            useCase().test {
                awaitError()
            }
        } catch (e: Exception) {
            assertThat(e.message).isEqualTo("Database error")
        }
    }
}
