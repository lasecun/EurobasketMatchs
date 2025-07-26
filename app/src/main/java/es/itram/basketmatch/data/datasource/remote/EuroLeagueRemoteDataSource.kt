package es.itram.basketmatch.data.datasource.remote

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueWebScraper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source remoto para obtener datos de EuroLeague desde la web oficial
 */
@Singleton
class EuroLeagueRemoteDataSource @Inject constructor(
    private val webScraper: EuroLeagueWebScraper
) {
    
    companion object {
        private const val TAG = "EuroLeagueRemoteDataSource"
    }
    
    /**
     * Obtiene todos los equipos de EuroLeague
     */
    suspend fun getAllTeams(): Result<List<TeamWebDto>> {
        return try {
            Log.d(TAG, "Obteniendo equipos desde web scraping...")
            val teams = webScraper.getTeams()
            
            if (teams.isNotEmpty()) {
                Log.d(TAG, "Equipos obtenidos exitosamente: ${teams.size}")
                Result.success(teams)
            } else {
                Log.w(TAG, "No se obtuvieron equipos del scraping")
                Result.success(getFallbackTeams())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo equipos", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los partidos
     */
    suspend fun getAllMatches(season: String = "2024-25"): Result<List<MatchWebDto>> {
        return try {
            Log.d(TAG, "Obteniendo partidos desde web scraping...")
            val matches = webScraper.getMatches(season)
            
            Log.d(TAG, "Partidos obtenidos: ${matches.size}")
            Result.success(matches)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo partidos", e)
            Result.failure(e)
        }
    }
    
    /**
     * Equipos de fallback en caso de que el scraping falle
     */
    private fun getFallbackTeams(): List<TeamWebDto> {
        return listOf(
            TeamWebDto(
                id = "real_madrid",
                name = "Real Madrid",
                fullName = "Real Madrid Basketball",
                shortCode = "MAD",
                logoUrl = null,
                country = "Spain",
                venue = "WiZink Center",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/real-madrid/"
            ),
            TeamWebDto(
                id = "fc_barcelona",
                name = "FC Barcelona",
                fullName = "FC Barcelona Basketball",
                shortCode = "BAR",
                logoUrl = null,
                country = "Spain",
                venue = "Palau de la Música Catalana",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/fc-barcelona/"
            ),
            TeamWebDto(
                id = "panathinaikos",
                name = "Panathinaikos",
                fullName = "Panathinaikos AKTOR Athens",
                shortCode = "PAN",
                logoUrl = null,
                country = "Greece",
                venue = "OAKA",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/panathinaikos-aktor-athens/"
            ),
            TeamWebDto(
                id = "olympiacos",
                name = "Olympiacos",
                fullName = "Olympiacos Piraeus",
                shortCode = "OLY",
                logoUrl = null,
                country = "Greece",
                venue = "Peace and Friendship Stadium",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/olympiacos-piraeus/"
            ),
            TeamWebDto(
                id = "fenerbahce",
                name = "Fenerbahce",
                fullName = "Fenerbahce Beko Istanbul",
                shortCode = "ULK",
                logoUrl = null,
                country = "Turkey",
                venue = "Ulker Sports and Event Hall",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/fenerbahce-beko-istanbul/"
            ),
            TeamWebDto(
                id = "anadolu_efes",
                name = "Anadolu Efes",
                fullName = "Anadolu Efes Istanbul",
                shortCode = "IST",
                logoUrl = null,
                country = "Turkey",
                venue = "Sinan Erdem Dome",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/anadolu-efes-istanbul/"
            ),
            TeamWebDto(
                id = "baskonia",
                name = "Baskonia",
                fullName = "Baskonia Vitoria-Gasteiz",
                shortCode = "BAS",
                logoUrl = null,
                country = "Spain",
                venue = "Fernando Buesa Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/baskonia-vitoria-gasteiz/"
            ),
            TeamWebDto(
                id = "valencia_basket",
                name = "Valencia Basket",
                fullName = "Valencia Basket",
                shortCode = "PAM",
                logoUrl = null,
                country = "Spain",
                venue = "Pabellón Fuente de San Luis",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/valencia-basket/"
            ),
            TeamWebDto(
                id = "zalgiris",
                name = "Zalgiris",
                fullName = "Zalgiris Kaunas",
                shortCode = "ZAL",
                logoUrl = null,
                country = "Lithuania",
                venue = "Zalgiris Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/zalgiris-kaunas/"
            ),
            TeamWebDto(
                id = "maccabi_tel_aviv",
                name = "Maccabi Tel Aviv",
                fullName = "Maccabi Rapyd Tel Aviv",
                shortCode = "TEL",
                logoUrl = null,
                country = "Israel",
                venue = "Menora Mivtachim Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/maccabi-rapyd-tel-aviv/"
            ),
            TeamWebDto(
                id = "as_monaco",
                name = "AS Monaco",
                fullName = "AS Monaco Basketball",
                shortCode = "MCO",
                logoUrl = null,
                country = "Monaco",
                venue = "Salle Gaston Médecin",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/as-monaco/"
            ),
            TeamWebDto(
                id = "bayern_munich",
                name = "Bayern Munich",
                fullName = "FC Bayern Munich Basketball",
                shortCode = "MUN",
                logoUrl = null,
                country = "Germany",
                venue = "BMW Park",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/fc-bayern-munich/"
            ),
            TeamWebDto(
                id = "virtus_bologna",
                name = "Virtus Bologna",
                fullName = "Virtus Segafredo Bologna",
                shortCode = "VIR",
                logoUrl = null,
                country = "Italy",
                venue = "Segafredo Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/virtus-bologna/"
            ),
            TeamWebDto(
                id = "milan",
                name = "Milan",
                fullName = "EA7 Emporio Armani Milan",
                shortCode = "MIL",
                logoUrl = null,
                country = "Italy",
                venue = "Mediolanum Forum",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/ea7-emporio-armani-milan/"
            ),
            TeamWebDto(
                id = "red_star_belgrade",
                name = "Red Star Belgrade",
                fullName = "Crvena Zvezda Meridianbet Belgrade",
                shortCode = "RED",
                logoUrl = null,
                country = "Serbia",
                venue = "Štark Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/crvena-zvezda-meridianbet-belgrade/"
            ),
            TeamWebDto(
                id = "partizan_belgrade",
                name = "Partizan Belgrade",
                fullName = "Partizan Mozzart Bet Belgrade",
                shortCode = "PAR",
                logoUrl = null,
                country = "Serbia",
                venue = "Štark Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/partizan-mozzart-bet-belgrade/"
            ),
            TeamWebDto(
                id = "asvel_villeurbanne",
                name = "ASVEL",
                fullName = "LDLC ASVEL Villeurbanne",
                shortCode = "ASV",
                logoUrl = null,
                country = "France",
                venue = "LDLC Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/ldlc-asvel-villeurbanne/"
            ),
            TeamWebDto(
                id = "paris_basketball",
                name = "Paris Basketball",
                fullName = "Paris Basketball",
                shortCode = "PRS",
                logoUrl = null,
                country = "France",
                venue = "Adidas Arena",
                profileUrl = "https://www.euroleaguebasketball.net/euroleague/teams/paris-basketball/"
            )
        )
    }
}
