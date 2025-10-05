package es.itram.basketmatch.data.network

import es.itram.basketmatch.data.datasource.remote.dto.api.CompetitionsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.SeasonResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.TeamsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.TeamDetailsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.GamesResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.GameDetailsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.GameStatsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.TeamRosterResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.StandingsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.PlayerResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 🏀 EuroLeague Official API Service
 *
 * Servicio Retrofit para consumir la API oficial de EuroLeague
 * Base URL: https://api-live.euroleague.net/
 *
 * Esta API oficial proporciona:
 * ✅ Datos en tiempo real y oficiales
 * ✅ Estructura JSON consistente
 * ✅ Mejor rendimiento que scraping
 * ✅ Documentación Swagger completa
 */
interface EuroLeagueApiService {

    /**
     * Obtiene todas las competiciones disponibles
     */
    @GET("v3/competitions")
    suspend fun getCompetitions(): Response<CompetitionsResponseDto>

    /**
     * Obtiene información de una temporada específica
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}")
    suspend fun getSeason(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024"
    ): Response<SeasonResponseDto>

    /**
     * Obtiene todos los equipos de una temporada
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/clubs")
    suspend fun getTeams(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024"
    ): Response<TeamsResponseDto>

    /**
     * Obtiene información detallada de un equipo
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/clubs/{clubCode}")
    suspend fun getTeamDetails(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Path("clubCode") clubCode: String
    ): Response<TeamDetailsResponseDto>

    /**
     * Obtiene todos los partidos de una temporada
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/games")
    suspend fun getGames(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Query("phaseTypeCode") phaseTypeCode: String? = null,
        @Query("gameStateCode") gameStateCode: String? = null
    ): Response<GamesResponseDto>

    /**
     * Obtiene información detallada de un partido
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/games/{gameCode}")
    suspend fun getGameDetails(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Path("gameCode") gameCode: String
    ): Response<GameDetailsResponseDto>

    /**
     * Obtiene partidos por fecha
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/games")
    suspend fun getGamesByDate(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Query("dateFrom") dateFrom: String, // YYYY-MM-DD
        @Query("dateTo") dateTo: String // YYYY-MM-DD
    ): Response<GamesResponseDto>

    /**
     * Obtiene estadísticas de un partido
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/games/{gameCode}/stats")
    suspend fun getGameStats(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Path("gameCode") gameCode: String
    ): Response<GameStatsResponseDto>

    /**
     * Obtiene la plantilla de un equipo
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/clubs/{clubCode}/people")
    suspend fun getTeamRoster(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Path("clubCode") clubCode: String
    ): Response<TeamRosterResponseDto>

    /**
     * Obtiene las clasificaciones
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/standings")
    suspend fun getStandings(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Query("phaseTypeCode") phaseTypeCode: String? = null
    ): Response<StandingsResponseDto>

    /**
     * Obtiene información de un jugador
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/people/{personCode}")
    suspend fun getPlayer(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2024",
        @Path("personCode") personCode: String
    ): Response<PlayerResponseDto>
}
