package es.itram.basketmatch.data.datasource.remote

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toMatchWebDto
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toMatchWebDtoList
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toTeamWebDtoList
import es.itram.basketmatch.data.mapper.SimplePlayerDto
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toSimplePlayerList
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toTeamWebDto
import es.itram.basketmatch.data.network.EuroLeagueApiService
import es.itram.basketmatch.data.network.NetworkManager
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🏀 EuroLeague Official API Data Source
 *
 * Nueva implementación que usa la API oficial de EuroLeague en lugar de scraping
 * Esta es MUCHO más confiable, rápida y mantenible que el scraping web.
 *
 * Ventajas de la API oficial:
 * ✅ Datos oficiales y en tiempo real
 * ✅ Estructura JSON consistente
 * ✅ No hay riesgo de cambios en HTML
 * ✅ Mejor rendimiento y confiabilidad
 * ✅ Documentación Swagger completa
 * ✅ Incluye estadísticas detalladas
 * ✅ Soporte para múltiples temporadas
 */
@Singleton
class EuroLeagueOfficialApiDataSource @Inject constructor(
    private val apiService: EuroLeagueApiService,
    private val networkManager: NetworkManager
) {

    companion object {
        private const val TAG = "EuroLeagueOfficialApi"
        private const val DEFAULT_COMPETITION = "E" // EuroLeague
        private const val DEFAULT_SEASON = "E2024" // Temporada 2024-25
        private const val RETRY_DELAY_MS = 1000L
        private const val MAX_RETRIES = 3
    }

    /**
     * Obtiene todos los equipos de EuroLeague usando la API oficial
     */
    suspend fun getAllTeams(
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<List<TeamWebDto>> {
        return executeWithRetry("getAllTeams") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo equipos desde API oficial...")

            val response = apiService.getTeams(competitionCode, seasonCode)

            if (response.isSuccessful) {
                val teams = response.body()?.data?.toTeamWebDtoList() ?: emptyList()
                Log.d(TAG, "✅ Equipos obtenidos desde API oficial: ${teams.size}")
                teams
            } else {
                Log.e(TAG, "❌ Error en respuesta API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene todos los partidos de EuroLeague usando la API oficial
     */
    suspend fun getAllMatches(
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON,
        phaseTypeCode: String? = null // "RS" para Regular Season, "PO" para Playoffs
    ): Result<List<MatchWebDto>> {
        return executeWithRetry("getAllMatches") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo partidos desde API oficial...")

            val response = apiService.getGames(competitionCode, seasonCode, phaseTypeCode)

            if (response.isSuccessful) {
                val matches = response.body()?.data?.toMatchWebDtoList() ?: emptyList()
                Log.d(TAG, "✅ Partidos obtenidos desde API oficial: ${matches.size}")
                matches
            } else {
                Log.e(TAG, "❌ Error en respuesta API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene partidos por rango de fechas
     */
    suspend fun getMatchesByDateRange(
        dateFrom: String, // YYYY-MM-DD
        dateTo: String, // YYYY-MM-DD
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<List<MatchWebDto>> {
        return executeWithRetry("getMatchesByDateRange") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo partidos por fecha: $dateFrom a $dateTo")

            val response = apiService.getGamesByDate(competitionCode, seasonCode, dateFrom, dateTo)

            if (response.isSuccessful) {
                val matches = response.body()?.data?.toMatchWebDtoList() ?: emptyList()
                Log.d(TAG, "✅ Partidos obtenidos por fecha: ${matches.size}")
                matches
            } else {
                Log.e(TAG, "❌ Error en respuesta API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene detalles de un partido específico
     */
    suspend fun getMatchDetails(
        gameCode: String,
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<MatchWebDto> {
        return executeWithRetry("getMatchDetails") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo detalles del partido: $gameCode")

            val response = apiService.getGameDetails(competitionCode, seasonCode, gameCode)

            if (response.isSuccessful) {
                val gameDto = response.body()?.data
                if (gameDto != null) {
                    val match = gameDto.toMatchWebDto()
                    Log.d(TAG, "✅ Detalles del partido obtenidos: ${match.homeTeamName} vs ${match.awayTeamName}")
                    match
                } else {
                    throw Exception("No se encontraron datos del partido")
                }
            } else {
                Log.e(TAG, "❌ Error en respuesta API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene la plantilla de un equipo
     */
    suspend fun getTeamRoster(
        clubCode: String,
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<List<SimplePlayerDto>> {
        return executeWithRetry("getTeamRoster") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo plantilla del equipo: $clubCode")

            val response = apiService.getTeamRoster(competitionCode, seasonCode, clubCode)

            if (response.isSuccessful) {
                val players = response.body()?.data?.toSimplePlayerList() ?: emptyList()
                Log.d(TAG, "✅ Plantilla obtenida: ${players.size} jugadores")
                players
            } else {
                Log.e(TAG, "❌ Error en respuesta API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene información detallada de un equipo
     */
    suspend fun getTeamDetails(
        clubCode: String,
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<TeamWebDto> {
        return executeWithRetry("getTeamDetails") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo detalles del equipo: $clubCode")

            val response = apiService.getTeamDetails(competitionCode, seasonCode, clubCode)

            if (response.isSuccessful) {
                val teamDto = response.body()?.data
                if (teamDto != null) {
                    val team = teamDto.toTeamWebDto()
                    Log.d(TAG, "✅ Detalles del equipo obtenidos: ${team.name}")
                    team
                } else {
                    throw Exception("No se encontraron datos del equipo")
                }
            } else {
                Log.e(TAG, "❌ Error en respuesta API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Verifica si la API oficial está disponible
     */
    suspend fun isApiAvailable(): Boolean {
        return try {
            if (!networkManager.isConnected()) {
                false
            } else {
                val response = apiService.getCompetitions()
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ API oficial no disponible: ${e.message}")
            false
        }
    }

    /**
     * Ejecuta una operación con reintentos automáticos en caso de fallo
     */
    private suspend fun <T> executeWithRetry(
        operation: String,
        maxRetries: Int = MAX_RETRIES,
        block: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                val result = block()
                return Result.success(result)
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "⚠️ Intento ${attempt + 1}/$maxRetries falló para $operation: ${e.message}")

                if (attempt < maxRetries - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // Backoff exponencial
                }
            }
        }

        Log.e(TAG, "❌ Operación $operation falló después de $maxRetries intentos")
        return Result.failure(lastException ?: Exception("Operación fallida después de $maxRetries intentos"))
    }
}
