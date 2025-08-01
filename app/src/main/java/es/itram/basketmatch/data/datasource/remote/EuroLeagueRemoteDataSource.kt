package es.itram.basketmatch.data.datasource.remote

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.datasource.remote.scraper.EuroLeagueJsonApiScraper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source remoto para obtener datos de EuroLeague desde la API JSON oficial
 * 
 * ARQUITECTURA UNIFICADA:
 * - ÚNICA FUENTE: feeds.incrowdsports.com (API de feeds) - Para TODOS los datos
 * - ✅ Equipos, partidos, rosters, estadísticas, imágenes
 * - ✅ Logos de equipos e imágenes de jugadores incluidos
 * 
 * Esta clase es un wrapper sobre EuroLeagueJsonApiScraper que proporciona:
 * - Manejo de errores centralizado
 * - Equipos de fallback si la API falla
 * - Interfaz consistente para los repositorios
 * 
 * Nota: DataSyncService también usa directamente EuroLeagueJsonApiScraper para sincronización masiva
 */
@Singleton
class EuroLeagueRemoteDataSource @Inject constructor(
    private val jsonApiScraper: EuroLeagueJsonApiScraper
) {
    
    companion object {
        private const val TAG = "EuroLeagueRemoteDataSource"
    }
    
    /**
     * Obtiene todos los equipos de EuroLeague usando la API JSON
     */
    suspend fun getAllTeams(): Result<List<TeamWebDto>> {
        return try {
            Log.d(TAG, "🏀 Obteniendo equipos desde API JSON...")
            
            val teamsFromJson = jsonApiScraper.getTeams()
            
            if (teamsFromJson.isNotEmpty()) {
                Log.d(TAG, "✅ Equipos obtenidos desde API JSON: ${teamsFromJson.size}")
                Result.success(teamsFromJson)
            } else {
                Log.w(TAG, "⚠️ API JSON no devolvió equipos, usando equipos de fallback...")
                Result.success(getFallbackTeams())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo equipos desde API JSON", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los partidos usando la API JSON
     */
    suspend fun getAllMatches(season: String = "2025-26"): Result<List<MatchWebDto>> {
        return try {
            Log.d(TAG, "⚽ Obteniendo partidos desde API JSON para temporada $season...")
            
            val matchesFromJson = jsonApiScraper.getMatches(season)
            
            if (matchesFromJson.isNotEmpty()) {
                Log.d(TAG, "✅ Partidos obtenidos desde API JSON: ${matchesFromJson.size}")
                
                // Verificar si hay partidos del 30 de septiembre específicamente
                val september30Matches = matchesFromJson.filter { it.date == "2025-09-30" }
                Log.d(TAG, "🎯 Partidos del 30/09/2025 encontrados: ${september30Matches.size}")
                
                Result.success(matchesFromJson)
            } else {
                Log.w(TAG, "⚠️ API JSON no devolvió partidos")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo partidos desde API JSON", e)
            Result.failure(e)
        }
    }
    
    /**
     * Equipos de fallback en caso de que la feeds API falle
     * Todos los datos están alineados con la feeds API unificada
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
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/MAD"
            ),
            TeamWebDto(
                id = "fc_barcelona",
                name = "FC Barcelona",
                fullName = "FC Barcelona Basketball",
                shortCode = "BAR",
                logoUrl = null,
                country = "Spain",
                venue = "Palau de la Música Catalana",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/BAR"
            ),
            TeamWebDto(
                id = "panathinaikos",
                name = "Panathinaikos",
                fullName = "Panathinaikos AKTOR Athens",
                shortCode = "PAN",
                logoUrl = null,
                country = "Greece",
                venue = "OAKA",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/PAN"
            ),
            TeamWebDto(
                id = "olympiacos",
                name = "Olympiacos",
                fullName = "Olympiacos Piraeus",
                shortCode = "OLY",
                logoUrl = null,
                country = "Greece",
                venue = "Peace and Friendship Stadium",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/OLY"
            ),
            TeamWebDto(
                id = "fenerbahce",
                name = "Fenerbahce",
                fullName = "Fenerbahce Beko Istanbul",
                shortCode = "ULK",
                logoUrl = null,
                country = "Turkey",
                venue = "Ulker Sports and Event Hall",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/ULK"
            ),
            TeamWebDto(
                id = "anadolu_efes",
                name = "Anadolu Efes",
                fullName = "Anadolu Efes Istanbul",
                shortCode = "IST",
                logoUrl = null,
                country = "Turkey",
                venue = "Sinan Erdem Dome",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/IST"
            ),
            TeamWebDto(
                id = "baskonia",
                name = "Baskonia",
                fullName = "Baskonia Vitoria-Gasteiz",
                shortCode = "BAS",
                logoUrl = null,
                country = "Spain",
                venue = "Fernando Buesa Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/BAS"
            ),
            TeamWebDto(
                id = "valencia_basket",
                name = "Valencia Basket",
                fullName = "Valencia Basket",
                shortCode = "PAM",
                logoUrl = null,
                country = "Spain",
                venue = "Pabellón Fuente de San Luis",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/PAM"
            ),
            TeamWebDto(
                id = "zalgiris",
                name = "Zalgiris",
                fullName = "Zalgiris Kaunas",
                shortCode = "ZAL",
                logoUrl = null,
                country = "Lithuania",
                venue = "Zalgiris Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/ZAL"
            ),
            TeamWebDto(
                id = "maccabi_tel_aviv",
                name = "Maccabi Tel Aviv",
                fullName = "Maccabi Rapyd Tel Aviv",
                shortCode = "TEL",
                logoUrl = null,
                country = "Israel",
                venue = "Menora Mivtachim Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/TEL"
            ),
            TeamWebDto(
                id = "as_monaco",
                name = "AS Monaco",
                fullName = "AS Monaco Basketball",
                shortCode = "MCO",
                logoUrl = null,
                country = "Monaco",
                venue = "Salle Gaston Médecin",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/MCO"
            ),
            TeamWebDto(
                id = "bayern_munich",
                name = "Bayern Munich",
                fullName = "FC Bayern Munich Basketball",
                shortCode = "MUN",
                logoUrl = null,
                country = "Germany",
                venue = "BMW Park",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/MUN"
            ),
            TeamWebDto(
                id = "virtus_bologna",
                name = "Virtus Bologna",
                fullName = "Virtus Segafredo Bologna",
                shortCode = "VIR",
                logoUrl = null,
                country = "Italy",
                venue = "Segafredo Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/VIR"
            ),
            TeamWebDto(
                id = "milan",
                name = "Milan",
                fullName = "EA7 Emporio Armani Milan",
                shortCode = "MIL",
                logoUrl = null,
                country = "Italy",
                venue = "Mediolanum Forum",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/MIL"
            ),
            TeamWebDto(
                id = "red_star_belgrade",
                name = "Red Star Belgrade",
                fullName = "Crvena Zvezda Meridianbet Belgrade",
                shortCode = "RED",
                logoUrl = null,
                country = "Serbia",
                venue = "Štark Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/RED"
            ),
            TeamWebDto(
                id = "partizan_belgrade",
                name = "Partizan Belgrade",
                fullName = "Partizan Mozzart Bet Belgrade",
                shortCode = "PAR",
                logoUrl = null,
                country = "Serbia",
                venue = "Štark Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/PAR"
            ),
            TeamWebDto(
                id = "asvel_villeurbanne",
                name = "ASVEL",
                fullName = "LDLC ASVEL Villeurbanne",
                shortCode = "ASV",
                logoUrl = null,
                country = "France",
                venue = "LDLC Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/ASV"
            ),
            TeamWebDto(
                id = "paris_basketball",
                name = "Paris Basketball",
                fullName = "Paris Basketball",
                shortCode = "PRS",
                logoUrl = null,
                country = "France",
                venue = "Adidas Arena",
                profileUrl = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs/PRS"
            )
        )
    }
}
