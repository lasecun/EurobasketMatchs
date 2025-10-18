package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

/**
 * Tests para TeamRepositoryImpl - Temporada 2025-2026
 */
class TeamRepositoryImplTest {

    private lateinit var repository: TeamRepositoryImpl
    private val teamDao: TeamDao = mockk()
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource = mockk()
    private val networkManager: NetworkManager = mockk()

    @Before
    fun setup() {
        repository = TeamRepositoryImpl(
            teamDao = teamDao,
            officialApiDataSource = officialApiDataSource,
            networkManager = networkManager
        )
    }

    @Test
    fun `getAllTeams returns teams from local database`() = runTest {
        // Given
        val teams = TestDataFactory.createTestTeamList(2)
        val teamEntities = teams.map { TeamMapper.fromDomain(it) }
        every { teamDao.getAllTeams() } returns flowOf(teamEntities)
        every { networkManager.isConnected() } returns false

        // When
        val result = repository.getAllTeams().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Team 1")
        verify { teamDao.getAllTeams() }
    }

    @Test
    fun `getTeamById returns specific team`() = runTest {
        // Given
        val team = TestDataFactory.createTestTeam(id = "TEAM1", name = "Test Team")
        val teamEntity = TeamMapper.fromDomain(team)
        every { teamDao.getTeamById("TEAM1") } returns flowOf(teamEntity)

        // When
        val result = repository.getTeamById("TEAM1").first()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("Test Team")
        verify { teamDao.getTeamById("TEAM1") }
    }

    @Test
    fun `getTeamById returns null when team not found`() = runTest {
        // Given
        every { teamDao.getTeamById("UNKNOWN") } returns flowOf(null)

        // When
        val result = repository.getTeamById("UNKNOWN").first()

        // Then
        assertThat(result).isNull()
        verify { teamDao.getTeamById("UNKNOWN") }
    }

    @Test
    fun `updateFavoriteStatus updates team favorite status`() = runTest {
        // Given
        coEvery { teamDao.updateFavoriteStatus("TEAM1", true) } returns Unit

        // When
        repository.updateFavoriteStatus("TEAM1", true)

        // Then
        coVerify { teamDao.updateFavoriteStatus("TEAM1", true) }
    }

    @Test
    fun `getFavoriteTeams returns only favorite teams`() = runTest {
        // Given
        val favoriteTeams = listOf(
            TestDataFactory.createTestTeam(id = "FAV1", name = "Favorite 1", isFavorite = true),
            TestDataFactory.createTestTeam(id = "FAV2", name = "Favorite 2", isFavorite = true)
        )
        val favoriteEntities = favoriteTeams.map { TeamMapper.fromDomain(it) }
        every { teamDao.getFavoriteTeams() } returns flowOf(favoriteEntities)

        // When
        val result = repository.getFavoriteTeams().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.isFavorite }).isTrue()
        verify { teamDao.getFavoriteTeams() }
    }

    @Test
    fun `insertTeams inserts teams into database`() = runTest {
        // Given
        val teams = TestDataFactory.createTestTeamList(3)
        val teamEntities = teams.map { TeamMapper.fromDomain(it) }
        coEvery { teamDao.insertTeams(teamEntities) } returns Unit

        // When
        repository.insertTeams(teams)

        // Then
        coVerify { teamDao.insertTeams(teamEntities) }
    }
}
