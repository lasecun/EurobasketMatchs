package es.itram.basketmatch.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TeamRepositoryImplTest {

    // Mocks
    private val teamDao: TeamDao = mockk()
    private val remoteDataSource: EuroLeagueRemoteDataSource = mockk()
    private val networkManager: NetworkManager = mockk()

    // System under test
    private lateinit var repository: TeamRepositoryImpl

    // Test data
    private val testTeamEntity = TestDataFactory.createTestTeamEntity()
    private val testTeam = TestDataFactory.createTestTeam()

    @Before
    fun setup() {
        // Mock network manager para prevenir llamadas de red en tests
        every { networkManager.isConnected() } returns false
        
        // Mock remote data source para prevenir llamadas reales
        coEvery { remoteDataSource.getAllTeams() } returns Result.failure(Exception("No network in tests"))
        
        repository = TeamRepositoryImpl(teamDao, remoteDataSource, networkManager)
    }

    @Test
    fun `when getAllTeams is called, then returns mapped teams from dao`() = runTest {
        // Given
        val entities = listOf(testTeamEntity)
        coEvery { teamDao.getAllTeams() } returns flowOf(entities)

        // When
        val result = repository.getAllTeams()

        // Then
        result.test {
            val teams = awaitItem()
            assertThat(teams).hasSize(1)
            assertThat(teams[0].id).isEqualTo(testTeam.id)
            assertThat(teams[0].name).isEqualTo(testTeam.name)
            awaitComplete()
        }
        
        coVerify { teamDao.getAllTeams() }
    }

    @Test
    fun `when getTeamById is called, then returns mapped team from dao`() = runTest {
        // Given
        val teamId = "1"
        coEvery { teamDao.getTeamById(teamId) } returns flowOf(testTeamEntity)

        // When
        val result = repository.getTeamById(teamId)

        // Then
        result.test {
            val team = awaitItem()
            assertThat(team?.id).isEqualTo(testTeam.id)
            assertThat(team?.name).isEqualTo(testTeam.name)
            awaitComplete()
        }
        
        coVerify { teamDao.getTeamById(teamId) }
    }

    @Test
    fun `when getTeamById is called with non-existing id, then returns null`() = runTest {
        // Given
        val teamId = "non-existing"
        coEvery { teamDao.getTeamById(teamId) } returns flowOf(null)

        // When
        val result = repository.getTeamById(teamId)

        // Then
        result.test {
            val team = awaitItem()
            assertThat(team).isNull()
            awaitComplete()
        }
        
        coVerify { teamDao.getTeamById(teamId) }
    }

    @Test
    fun `when updateTeam is called, then dao update is called with mapped entity`() = runTest {
        // Given
        coEvery { teamDao.updateTeam(any()) } returns Unit

        // When
        repository.updateTeam(testTeam)

        // Then
        coVerify { teamDao.updateTeam(any()) }
    }

    @Test
    fun `when getFavoriteTeams is called, then returns mapped favorite teams from dao`() = runTest {
        // Given
        val favoriteTeamEntity = testTeamEntity.copy(isFavorite = true)
        val entities = listOf(favoriteTeamEntity)
        
        coEvery { teamDao.getFavoriteTeams() } returns flowOf(entities)

        // When
        val result = repository.getFavoriteTeams()

        // Then
        result.test {
            val teams = awaitItem()
            assertThat(teams).hasSize(1)
            assertThat(teams[0].isFavorite).isTrue()
            awaitComplete()
        }
        
        coVerify { teamDao.getFavoriteTeams() }
    }
}
