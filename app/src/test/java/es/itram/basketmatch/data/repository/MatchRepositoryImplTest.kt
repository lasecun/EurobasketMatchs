package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.testutil.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MatchRepositoryImplTest {

    private lateinit var matchDao: MatchDao
    private lateinit var remoteDataSource: EuroLeagueRemoteDataSource
    private lateinit var networkManager: NetworkManager
    private lateinit var repository: MatchRepositoryImpl

    @Before
    fun setup() {
        matchDao = mockk()
        remoteDataSource = mockk()
        networkManager = mockk()
        repository = MatchRepositoryImpl(
            matchDao = matchDao,
            remoteDataSource = remoteDataSource,
            networkManager = networkManager
        )
    }

    @Test
    fun `getAllMatches should return mapped matches from local data source`() = runTest {
        // Given
        val matchEntities = listOf(
            TestDataFactory.createTestMatchEntity(
                id = "1",
                homeTeamName = "Real Madrid",
                awayTeamName = "FC Barcelona"
            )
        )
        every { matchDao.getAllMatches() } returns flowOf(matchEntities)
        every { networkManager.isConnected() } returns false

        // When
        val result = repository.getAllMatches().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result.first().size)
        assertEquals("Real Madrid", result.first().first().homeTeamName)
    }

    @Test
    fun `getMatchById should return mapped match from local data source`() = runTest {
        // Given
        val matchEntity = TestDataFactory.createTestMatchEntity(
            id = "1",
            homeTeamName = "Real Madrid"
        )
        every { matchDao.getMatchById("1") } returns flowOf(matchEntity)

        // When
        val result = repository.getMatchById("1").toList()

        // Then
        assertEquals(1, result.size)
        assertEquals("Real Madrid", result.first()?.homeTeamName)
    }

    @Test
    fun `getMatchById should return null when match not found`() = runTest {
        // Given
        every { matchDao.getMatchById("999") } returns flowOf(null)

        // When
        val result = repository.getMatchById("999").toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(null, result.first())
    }

    @Test
    fun `getMatchesByStatus should return filtered matches`() = runTest {
        // Given
        val matchEntities = listOf(
            TestDataFactory.createTestMatchEntity(
                id = "1",
                status = MatchStatus.LIVE
            )
        )
        every { matchDao.getMatchesByStatus(MatchStatus.LIVE) } returns flowOf(matchEntities)

        // When
        val result = repository.getMatchesByStatus(MatchStatus.LIVE).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result.first().size)
        assertEquals(MatchStatus.LIVE, result.first().first().status)
    }
}
