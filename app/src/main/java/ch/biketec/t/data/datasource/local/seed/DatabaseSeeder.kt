package ch.biketec.t.data.datasource.local.seed

import ch.biketec.t.data.datasource.local.dao.MatchDao
import ch.biketec.t.data.datasource.local.dao.StandingDao
import ch.biketec.t.data.datasource.local.dao.TeamDao
import ch.biketec.t.data.datasource.local.entity.MatchEntity
import ch.biketec.t.data.datasource.local.entity.StandingEntity
import ch.biketec.t.data.datasource.local.entity.TeamEntity
import ch.biketec.t.domain.entity.MatchStatus
import ch.biketec.t.domain.entity.SeasonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val teamDao: TeamDao,
    private val matchDao: MatchDao,
    private val standingDao: StandingDao
) {

    suspend fun seedDatabase() = withContext(Dispatchers.IO) {
        // Check if data already exists
        val existingTeams = teamDao.getAllTeamsSync()
        if (existingTeams.isNotEmpty()) {
            return@withContext // Data already seeded
        }

        // Insert teams
        val teams = createSampleTeams()
        teams.forEach { teamDao.insertTeam(it) }

        // Insert standings
        val standings = createSampleStandings(teams)
        standings.forEach { standingDao.insertStanding(it) }

        // Insert matches
        val matches = createSampleMatches(teams)
        matches.forEach { matchDao.insertMatch(it) }
    }

    private fun createSampleTeams(): List<TeamEntity> {
        return listOf(
            TeamEntity(
                id = "real-madrid",
                name = "Real Madrid",
                shortName = "RMA",
                city = "Madrid",
                country = "España",
                logoUrl = "",
                primaryColor = "#FFFFFF",
                secondaryColor = "#000080",
                isFavorite = false
            ),
            TeamEntity(
                id = "barcelona",
                name = "FC Barcelona",
                shortName = "FCB",
                city = "Barcelona",
                country = "España",
                logoUrl = "",
                primaryColor = "#A50044",
                secondaryColor = "#004D9F",
                isFavorite = true
            ),
            TeamEntity(
                id = "panathinaikos",
                name = "Panathinaikos AKTOR Athens",
                shortName = "PAO",
                city = "Atenas",
                country = "Grecia",
                logoUrl = "",
                primaryColor = "#00A651",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            ),
            TeamEntity(
                id = "olympiacos",
                name = "Olympiacos Piraeus",
                shortName = "OLY",
                city = "El Pireo",
                country = "Grecia",
                logoUrl = "",
                primaryColor = "#C8102E",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            ),
            TeamEntity(
                id = "fenerbahce",
                name = "Fenerbahçe Beko Istanbul",
                shortName = "FEN",
                city = "Estambul",
                country = "Turquía",
                logoUrl = "",
                primaryColor = "#FEE11B",
                secondaryColor = "#003399",
                isFavorite = false
            ),
            TeamEntity(
                id = "maccabi",
                name = "Maccabi Playtika Tel Aviv",
                shortName = "MAC",
                city = "Tel Aviv",
                country = "Israel",
                logoUrl = "",
                primaryColor = "#FFD700",
                secondaryColor = "#003399",
                isFavorite = false
            ),
            TeamEntity(
                id = "efes",
                name = "Anadolu Efes Istanbul",
                shortName = "EFS",
                city = "Estambul",
                country = "Turquía",
                logoUrl = "",
                primaryColor = "#002D72",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            ),
            TeamEntity(
                id = "milano",
                name = "EA7 Emporio Armani Milan",
                shortName = "MIL",
                city = "Milán",
                country = "Italia",
                logoUrl = "",
                primaryColor = "#C8102E",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            ),
            TeamEntity(
                id = "bayern",
                name = "FC Bayern Munich",
                shortName = "BAY",
                city = "Múnich",
                country = "Alemania",
                logoUrl = "",
                primaryColor = "#DC052D",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            ),
            TeamEntity(
                id = "virtus",
                name = "Virtus Segafredo Bologna",
                shortName = "VIR",
                city = "Bolonia",
                country = "Italia",
                logoUrl = "",
                primaryColor = "#000000",
                secondaryColor = "#FFFFFF",
                isFavorite = false
            )
        )
    }

    private fun createSampleStandings(teams: List<TeamEntity>): List<StandingEntity> {
        return teams.mapIndexed { index, team ->
            val gamesPlayed = (15..20).random()
            val wins = (5..gamesPlayed).random()
            val losses = gamesPlayed - wins
            StandingEntity(
                id = UUID.randomUUID().toString(),
                teamId = team.id,
                position = index + 1,
                gamesPlayed = gamesPlayed,
                wins = wins,
                losses = losses,
                pointsFor = (1500..2000).random(),
                pointsAgainst = (1400..1900).random(),
                seasonType = SeasonType.REGULAR_SEASON
            )
        }
    }

    private fun createSampleMatches(teams: List<TeamEntity>): List<MatchEntity> {
        val matches = mutableListOf<MatchEntity>()
        val now = LocalDateTime.now()
        
        // Create matches for today
        matches.add(
            MatchEntity(
                id = UUID.randomUUID().toString(),
                homeTeamId = teams[0].id, // Real Madrid
                awayTeamId = teams[1].id, // Barcelona
                dateTime = now.withHour(20).withMinute(0).withSecond(0),
                arena = "WiZink Center",
                city = "Madrid",
                round = 15,
                seasonType = SeasonType.REGULAR_SEASON,
                status = MatchStatus.SCHEDULED,
                homeScore = null,
                awayScore = null
            )
        )
        
        matches.add(
            MatchEntity(
                id = UUID.randomUUID().toString(),
                homeTeamId = teams[2].id, // Panathinaikos
                awayTeamId = teams[3].id, // Olympiacos
                dateTime = now.withHour(22).withMinute(0).withSecond(0),
                arena = "OAKA",
                city = "Atenas",
                round = 15,
                seasonType = SeasonType.REGULAR_SEASON,
                status = MatchStatus.SCHEDULED,
                homeScore = null,
                awayScore = null
            )
        )

        // Create matches for tomorrow
        val tomorrow = now.plusDays(1)
        matches.add(
            MatchEntity(
                id = UUID.randomUUID().toString(),
                homeTeamId = teams[4].id, // Fenerbahce
                awayTeamId = teams[5].id, // Maccabi
                dateTime = tomorrow.withHour(19).withMinute(30).withSecond(0),
                arena = "Ülker Sports Arena",
                city = "Estambul",
                round = 15,
                seasonType = SeasonType.REGULAR_SEASON,
                status = MatchStatus.SCHEDULED,
                homeScore = null,
                awayScore = null
            )
        )

        // Create some finished matches for yesterday
        val yesterday = now.minusDays(1)
        matches.add(
            MatchEntity(
                id = UUID.randomUUID().toString(),
                homeTeamId = teams[6].id, // Efes
                awayTeamId = teams[7].id, // Milano
                dateTime = yesterday.withHour(20).withMinute(30).withSecond(0),
                arena = "Sinan Erdem Dome",
                city = "Estambul",
                round = 14,
                seasonType = SeasonType.REGULAR_SEASON,
                status = MatchStatus.FINISHED,
                homeScore = 85,
                awayScore = 78
            )
        )

        // Create matches for next week
        repeat(7) { dayOffset ->
            val matchDate = now.plusDays((dayOffset + 2).toLong())
            if (dayOffset % 2 == 0) { // Every other day
                matches.add(
                    MatchEntity(
                        id = UUID.randomUUID().toString(),
                        homeTeamId = teams.random().id,
                        awayTeamId = teams.random().id,
                        dateTime = matchDate.withHour(20).withMinute(0).withSecond(0),
                        arena = listOf("Arena", "Palace", "Center", "Stadium").random(),
                        city = teams.random().city,
                        round = 16,
                        seasonType = SeasonType.REGULAR_SEASON,
                        status = MatchStatus.SCHEDULED,
                        homeScore = null,
                        awayScore = null
                    )
                )
            }
        }

        return matches
    }
}
