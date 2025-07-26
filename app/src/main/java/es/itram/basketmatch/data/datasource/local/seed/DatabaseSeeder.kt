package es.itram.basketmatch.data.datasource.local.seed

import es.itram.basketmatch.data.datasource.local.EuroLeagueDatabase
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.data.mapper.StandingMapper
import es.itram.basketmatch.domain.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase para poblar la base de datos con datos iniciales
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: EuroLeagueDatabase
) {

    suspend fun seedDatabase() = withContext(Dispatchers.IO) {
        val teamDao = database.teamDao()
        val matchDao = database.matchDao()
        val standingDao = database.standingDao()

        // Verificar si ya hay datos
        val existingTeamsCount = teamDao.getAllTeams().let { flow ->
            var count = 0
            flow.collect { teams -> count = teams.size }
            count
        }

        if (existingTeamsCount > 0) {
            return@withContext // Ya hay datos, no seedear
        }

        // Equipos de EuroLeague
        val teams = listOf(
            Team(
                id = "real-madrid",
                name = "Real Madrid",
                shortName = "Real Madrid",
                code = "MAD",
                logoUrl = "",
                country = "España",
                city = "Madrid",
                founded = 1902,
                coach = "Chus Mateo",
                website = "https://www.realmadrid.com"
            ),
            Team(
                id = "barcelona",
                name = "FC Barcelona",
                shortName = "Barcelona",
                code = "BAR",
                logoUrl = "",
                country = "España",
                city = "Barcelona",
                founded = 1899,
                coach = "Roger Grimau",
                website = "https://www.fcbarcelona.com"
            ),
            Team(
                id = "fenerbahce",
                name = "Fenerbahçe Beko Istanbul",
                shortName = "Fenerbahce",
                code = "FEN",
                logoUrl = "",
                country = "Turquía",
                city = "Istanbul",
                founded = 1907,
                coach = "Dimitris Itoudis",
                website = "https://www.fenerbahce.org"
            ),
            Team(
                id = "olympiacos",
                name = "Olympiacos Piraeus",
                shortName = "Olympiacos",
                code = "OLY",
                logoUrl = "",
                country = "Grecia",
                city = "Piraeus",
                founded = 1925,
                coach = "Georgios Bartzokas",
                website = "https://www.olympiacos.org"
            ),
            Team(
                id = "panathinaikos",
                name = "Panathinaikos AKTOR Athens",
                shortName = "Panathinaikos",
                code = "PAO",
                logoUrl = "",
                country = "Grecia",
                city = "Athens",
                founded = 1908,
                coach = "Ergin Ataman",
                website = "https://www.pao.gr"
            ),
            Team(
                id = "bayern-munich",
                name = "FC Bayern Munich",
                shortName = "Bayern",
                code = "BAY",
                logoUrl = "",
                country = "Alemania",
                city = "Munich",
                founded = 1946,
                coach = "Pablo Laso",
                website = "https://fcbayern.com"
            ),
            Team(
                id = "anadolu-efes",
                name = "Anadolu Efes Istanbul",
                shortName = "Efes",
                code = "EFS",
                logoUrl = "",
                country = "Turquía",
                city = "Istanbul",
                founded = 1976,
                coach = "Tomislav Mijatovic",
                website = "https://www.anadoluefes.com"
            ),
            Team(
                id = "zalgiris",
                name = "Zalgiris Kaunas",
                shortName = "Zalgiris",
                code = "ZAL",
                logoUrl = "",
                country = "Lituania",
                city = "Kaunas",
                founded = 1944,
                coach = "Andrea Trinchieri",
                website = "https://www.zalgiris.lt"
            ),
            Team(
                id = "red-star",
                name = "Crvena zvezda mts Belgrade",
                shortName = "Red Star",
                code = "RED",
                logoUrl = "",
                country = "Serbia",
                city = "Belgrade",
                founded = 1945,
                coach = "Ioannis Sfairopoulos",
                website = "https://www.kkcrvenazvezda.rs"
            ),
            Team(
                id = "maccabi",
                name = "Maccabi Playtika Tel Aviv",
                shortName = "Maccabi",
                code = "MAC",
                logoUrl = "",
                country = "Israel",
                city = "Tel Aviv",
                founded = 1932,
                coach = "Oded Kattash",
                website = "https://www.maccabi.co.il"
            )
        )

        // Insertar equipos
        val teamEntities = TeamMapper.fromDomainList(teams)
        teamDao.insertTeams(teamEntities)

        // Generar partidos de ejemplo para los próximos días
        val matches = generateSampleMatches(teams)
        val matchEntities = MatchMapper.fromDomainList(matches)
        matchDao.insertMatches(matchEntities)

        // Generar clasificación de ejemplo
        val standings = generateSampleStandings(teams)
        val standingEntities = StandingMapper.fromDomainList(standings)
        standingDao.insertStandings(standingEntities)
    }

    private fun generateSampleMatches(teams: List<Team>): List<Match> {
        val matches = mutableListOf<Match>()
        val now = LocalDateTime.now()

        // Generar partidos para los próximos 7 días
        for (day in 0..6) {
            val matchDate = now.plusDays(day.toLong())
            
            // 2-3 partidos por día
            val matchesPerDay = if (day % 2 == 0) 2 else 3
            
            for (matchIndex in 0 until matchesPerDay) {
                val homeTeamIndex = (day * matchesPerDay + matchIndex) % teams.size
                val awayTeamIndex = (homeTeamIndex + 1) % teams.size
                
                val homeTeam = teams[homeTeamIndex]
                val awayTeam = teams[awayTeamIndex]
                
                matches.add(
                    Match(
                        id = "match-${day}-${matchIndex}",
                        homeTeamId = homeTeam.id,
                        awayTeamId = awayTeam.id,
                        dateTime = matchDate.withHour(20 + matchIndex).withMinute(30),
                        venue = "${homeTeam.city} Arena",
                        round = day + 1,
                        seasonType = SeasonType.REGULAR,
                        status = if (day < 0) MatchStatus.FINISHED else MatchStatus.SCHEDULED,
                        homeScore = if (day < 0) (70..95).random() else null,
                        awayScore = if (day < 0) (70..95).random() else null
                    )
                )
            }
        }

        return matches
    }

    private fun generateSampleStandings(teams: List<Team>): List<Standing> {
        return teams.mapIndexed { index, team ->
            val played = (15..20).random()
            val minWins = (played * 0.3).toInt()
            val maxWins = (played * 0.8).toInt()
            val won = (minWins..maxWins).random()
            val lost = played - won
            val pointsFor = won * (75..90).random() + lost * (70..85).random()
            val pointsAgainst = won * (70..85).random() + lost * (75..90).random()
            
            Standing(
                teamId = team.id,
                position = index + 1,
                played = played,
                won = won,
                lost = lost,
                pointsFor = pointsFor,
                pointsAgainst = pointsAgainst,
                pointsDifference = pointsFor - pointsAgainst,
                seasonType = SeasonType.REGULAR
            )
        }.sortedByDescending { (it.won.toDouble() / it.played.toDouble()) }
            .mapIndexed { index, standing -> standing.copy(position = index + 1) }
    }
}
