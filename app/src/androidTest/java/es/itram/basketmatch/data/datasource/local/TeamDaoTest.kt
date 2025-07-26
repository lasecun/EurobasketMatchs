package es.itram.basketmatch.data.datasource.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeamDaoTest {

    private lateinit var database: EuroLeagueDatabase
    private lateinit var teamDao: TeamDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            EuroLeagueDatabase::class.java
        ).build()
        teamDao = database.teamDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTeamAndRetrieveById() = runTest {
        // Given
        val team = TeamEntity(
            id = "1",
            name = "Real Madrid",
            city = "Madrid",
            country = "España",
            logoUrl = "https://example.com/logo.png",
            shortName = "RMA",
            code = "MAD",
            founded = 1902,
            coach = "Chus Mateo",
            isFavorite = false
        )

        // When
        teamDao.insertTeams(listOf(team))

        // Then
        teamDao.getTeamById("1").test {
            val retrievedTeam = awaitItem()
            assertThat(retrievedTeam).isNotNull()
            assertThat(retrievedTeam?.name).isEqualTo("Real Madrid")
            assertThat(retrievedTeam?.city).isEqualTo("Madrid")
        }
    }

    @Test
    fun insertMultipleTeamsAndGetAll() = runTest {
        // Given
        val teams = listOf(
            TeamEntity(
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
            TeamEntity(
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

        // When
        teamDao.insertTeams(teams)

        // Then
        teamDao.getAllTeams().test {
            val allTeams = awaitItem()
            assertThat(allTeams).hasSize(2)
            assertThat(allTeams.map { it.name }).containsExactly("Real Madrid", "FC Barcelona")
        }
    }

    @Test
    fun updateTeamFavoriteStatus() = runTest {
        // Given
        val team = TeamEntity(
            id = "1",
            name = "Real Madrid",
            city = "Madrid",
            country = "España",
            logoUrl = "https://example.com/logo.png",
            shortName = "RMA",
            code = "MAD",
            founded = 1902,
            coach = "Chus Mateo",
            isFavorite = false
        )
        teamDao.insertTeams(listOf(team))

        // When
        val updatedTeam = team.copy(isFavorite = true)
        teamDao.updateTeam(updatedTeam)

        // Then
        teamDao.getTeamById("1").test {
            val retrievedTeam = awaitItem()
            assertThat(retrievedTeam?.isFavorite).isTrue()
        }
    }

    @Test
    fun getFavoriteTeamsOnly() = runTest {
        // Given
        val teams = listOf(
            TeamEntity(
                id = "1",
                name = "Real Madrid",
                city = "Madrid",
                country = "España",
                logoUrl = "https://example.com/logo1.png",
                shortName = "RMA",
                code = "MAD",
                founded = 1902,
                coach = "Chus Mateo",
                isFavorite = true
            ),
            TeamEntity(
                id = "2",
                name = "FC Barcelona",
                city = "Barcelona",
                country = "España",
                logoUrl = "https://example.com/logo2.png",
                shortName = "BAR",
                code = "FCB",
                founded = 1899,
                coach = "Joan Peñarroya",
                isFavorite = false
            ),
            TeamEntity(
                id = "3",
                name = "Anadolu Efes",
                city = "Istanbul",
                country = "Turkey",
                logoUrl = "https://example.com/logo3.png",
                shortName = "AEF",
                code = "EFS",
                founded = 1976,
                coach = "Tomislav Mijatovic",
                isFavorite = true
            )
        )

        // When
        teamDao.insertTeams(teams)

        // Then
        teamDao.getFavoriteTeams().test {
            val favoriteTeams = awaitItem()
            assertThat(favoriteTeams).hasSize(2)
            assertThat(favoriteTeams.map { it.name }).containsExactly("Real Madrid", "Anadolu Efes")
            assertThat(favoriteTeams.all { it.isFavorite }).isTrue()
        }
    }

    @Test
    fun deleteAllTeams() = runTest {
        // Given
        val teams = listOf(
            TeamEntity(
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
            )
        )
        teamDao.insertTeams(teams)

        // When
        teamDao.deleteAllTeams()

        // Then
        teamDao.getAllTeams().test {
            val allTeams = awaitItem()
            assertThat(allTeams).isEmpty()
        }
    }
}
