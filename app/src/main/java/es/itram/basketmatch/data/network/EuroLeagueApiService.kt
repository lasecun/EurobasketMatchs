package es.itram.basketmatch.data.network

import es.itram.basketmatch.data.datasource.remote.dto.api.GameDetailsResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.GamesResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.TeamRosterResponseDto
import es.itram.basketmatch.data.datasource.remote.dto.api.TeamsResponseDto
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
 * ACTUALIZADO: Usando API v2 que sí funciona (v3 daba error 405)
 * Esta API oficial proporciona:
 * ✅ Datos en tiempo real y oficiales
 * ✅ Estructura JSON consistente
 * ✅ Mejor rendimiento que scraping
 * ✅ Documentación Swagger completa
 */
interface EuroLeagueApiService {

    /**
     * Obtiene todos los equipos de una temporada
     */
    @GET("v2/competitions/{competitionCode}/seasons/{seasonCode}/clubs")
    suspend fun getTeams(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2025"
    ): Response<TeamsResponseDto>

    /**
     * Obtiene el reporte completo de un partido (NUEVO - CON RESULTADOS REALES)
     * Este endpoint SÍ funciona y devuelve los marcadores correctos
     */
    @GET("v3/competitions/{competitionCode}/seasons/{seasonCode}/games/{gameCode}/report")
    suspend fun getGameReport(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2025",
        @Path("gameCode") gameCode: String
    ): Response<GameDetailsResponseDto>

    /**
     * Obtiene todos los partidos de una temporada usando v2 (FALLBACK)
     */
    @GET("v2/competitions/{competitionCode}/seasons/{seasonCode}/games")
    suspend fun getGamesV2(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2025",
        @Query("phaseTypeCode") phaseTypeCode: String? = null
    ): Response<GamesResponseDto>

    /**
     * Obtiene la plantilla de un equipo
     */
    @GET("v2/competitions/{competitionCode}/seasons/{seasonCode}/clubs/{clubCode}/people")
    suspend fun getTeamRoster(
        @Path("competitionCode") competitionCode: String = "E",
        @Path("seasonCode") seasonCode: String = "E2025",
        @Path("clubCode") clubCode: String
    ): Response<TeamRosterResponseDto>
}
