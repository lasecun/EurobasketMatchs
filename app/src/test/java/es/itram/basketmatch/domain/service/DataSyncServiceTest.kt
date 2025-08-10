package es.itram.basketmatch.domain.service

import android.content.SharedPreferences
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DataSyncServiceTest {
    private lateinit var service: DataSyncService
    private val mockScraper = mockk<EuroLeagueJsonApiScraper>(relaxed = true)
    private val mockTeamDao = mockk<TeamDao>(relaxed = true)
    private val mockMatchDao = mockk<MatchDao>(relaxed = true)
    private val mockTeamMapper = mockk<TeamWebMapper>(relaxed = true)
    private val mockMatchMapper = mockk<MatchWebMapper>(relaxed = true)
    private val mockPrefs = mockk<SharedPreferences>(relaxed = true)

    @Before
    fun setup() {
        service = DataSyncService(
            jsonApiScraper = mockScraper,
            teamDao = mockTeamDao,
            matchDao = mockMatchDao,
            teamMapper = mockTeamMapper,
            matchMapper = mockMatchMapper,
            prefs = mockPrefs
        )
    }

    @Test
    fun `isSyncNeeded returns true when data not populated`() = runTest {
        every { mockPrefs.getBoolean("data_populated", false) } returns false
        coEvery { mockMatchDao.getMatchCount() } returns 0
        val result = service.isSyncNeeded()
        assertTrue(result)
    }
}

