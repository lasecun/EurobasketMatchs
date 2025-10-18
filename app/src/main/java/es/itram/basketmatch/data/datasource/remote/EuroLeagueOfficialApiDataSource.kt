package es.itram.basketmatch.data.datasource.remote

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toMatchWebDto
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toMatchWebDtoList
import es.itram.basketmatch.data.mapper.EuroLeagueApiMapper.toTeamWebDtoList
import es.itram.basketmatch.data.network.EuroLeagueApiService
import es.itram.basketmatch.data.network.NetworkManager
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🏀 EuroLeague Official API Data Source - Temporada 2025-2026
 *
 * Fuente de datos que usa ÚNICAMENTE la API oficial de EuroLeague
 * Solo temporada 2025-2026 (E2025), sin fallbacks ni datos de prueba
 */
@Singleton
class EuroLeagueOfficialApiDataSource @Inject constructor(
    private val apiService: EuroLeagueApiService,
    private val networkManager: NetworkManager
) {

    companion object {
        private const val TAG = "EuroLeagueOfficialApi"
        private const val DEFAULT_COMPETITION = "E" // EuroLeague
        private const val DEFAULT_SEASON = "E2025" // Temporada 2025-26 (código API oficial)
        private const val RETRY_DELAY_MS = 1000L
        private const val MAX_RETRIES = 3
    }

    /**
     * Obtiene todos los equipos de la temporada 2025-2026
     */
    suspend fun getAllTeams(
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<List<TeamWebDto>> {
        return executeWithRetry("getAllTeams") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo equipos temporada $seasonCode...")

            val response = apiService.getTeams(competitionCode, seasonCode)

            if (response.isSuccessful) {
                val teams = response.body()?.data?.toTeamWebDtoList() ?: emptyList()
                Log.d(TAG, "✅ Equipos obtenidos: ${teams.size}")
                teams
            } else {
                Log.e(TAG, "❌ Error API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene todos los partidos de la temporada 2025-2026
     * Incluye las 38 jornadas completas
     */
    suspend fun getAllMatches(
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON,
        phaseTypeCode: String? = null
    ): Result<List<MatchWebDto>> {
        return executeWithRetry("getAllMatches") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo partidos temporada $seasonCode usando API v2...")

            val response = apiService.getGamesV2(competitionCode, seasonCode, phaseTypeCode)

            if (response.isSuccessful) {
                val matches = response.body()?.data?.toMatchWebDtoList() ?: emptyList()
                Log.d(TAG, "✅ Partidos obtenidos desde API: ${matches.size}")

                // Log estados de los partidos para debugging
                val statusCount = matches.groupBy { it.status }.mapValues { it.value.size }
                Log.d(TAG, "📊 Estados de partidos: $statusCount")

                enrichFinishedMatches(matches, competitionCode, seasonCode)
            } else {
                Log.e(TAG, "❌ Error API: ${response.code()} - ${response.message()}")
                throw Exception("Error ${response.code()}: ${response.message()}")
            }
        }
    }

    /**
     * Obtiene partidos filtrados por fecha
     */
    suspend fun getGamesByDate(
        dateFrom: String,
        dateTo: String,
        competitionCode: String = DEFAULT_COMPETITION,
        seasonCode: String = DEFAULT_SEASON
    ): Result<List<MatchWebDto>> {
        return executeWithRetry("getGamesByDate") {
            if (!networkManager.isConnected()) {
                throw Exception("No hay conexión a internet")
            }

            Log.d(TAG, "🏀 Obteniendo partidos fecha: $dateFrom - $dateTo")

            // Obtener todos los partidos y filtrar localmente
            val allMatchesResult = getAllMatches(competitionCode, seasonCode)

            if (allMatchesResult.isSuccess) {
                val allMatches = allMatchesResult.getOrNull() ?: emptyList()

                val filteredMatches = allMatches.filter { match ->
                    try {
                        val matchDate = java.time.LocalDate.parse(match.date)
                        val fromDate = java.time.LocalDate.parse(dateFrom)
                        val toDate = java.time.LocalDate.parse(dateTo)
                        matchDate in fromDate..toDate
                    } catch (_: Exception) {
                        false
                    }
                }

                Log.d(TAG, "✅ Partidos filtrados: ${filteredMatches.size}")
                filteredMatches
            } else {
                throw Exception("Error obteniendo partidos: ${allMatchesResult.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Enriquece los partidos terminados con sus resultados reales
     * Usa API v3 para obtener el reporte detallado con marcadores
     */
    private suspend fun enrichFinishedMatches(
        matches: List<MatchWebDto>,
        competitionCode: String,
        seasonCode: String
    ): List<MatchWebDto> {
        Log.d(TAG, "🔍 Iniciando enriquecimiento de ${matches.size} partidos...")

        // Contar partidos por estado
        val finishedCount = matches.count { it.status == es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.FINISHED }
        val scheduledCount = matches.count { it.status == es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.SCHEDULED }
        val liveCount = matches.count { it.status == es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.LIVE }

        Log.d(TAG, "📊 Resumen: Finalizados=$finishedCount, Programados=$scheduledCount, En vivo=$liveCount")

        return matches.map { match ->
            try {
                // Log el estado de CADA partido
                Log.d(TAG, "🔹 ${match.id}: Estado=${match.status}, Fecha=${match.date}")

                // Verificar si es un partido finalizado
                val isFinished = match.status == es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.FINISHED

                if (isFinished) {
                    Log.d(TAG, "📊 ✅ PARTIDO FINALIZADO ${match.id} - Obteniendo marcador real...")

                    // Obtener el reporte completo del partido desde v3/report
                    val reportResponse = apiService.getGameReport(competitionCode, seasonCode, match.id)

                    if (reportResponse.isSuccessful) {
                        val gameDetails = reportResponse.body()?.data
                        if (gameDetails != null) {
                            // Convertir el GameApiDto completo con todos los datos
                            val enrichedMatch = gameDetails.toMatchWebDto()

                            if (enrichedMatch != null) {
                                val homeScore = enrichedMatch.homeScore ?: 0
                                val awayScore = enrichedMatch.awayScore ?: 0

                                if (homeScore > 0 || awayScore > 0) {
                                    Log.d(TAG, "✅ ${match.id}: ${enrichedMatch.homeTeamName} $homeScore - $awayScore ${enrichedMatch.awayTeamName}")
                                    return@map enrichedMatch
                                } else {
                                    Log.w(TAG, "⚠️ ${match.id}: Marcadores en 0, usando datos originales")
                                    return@map match
                                }
                            } else {
                                Log.w(TAG, "⚠️ ${match.id}: Datos incompletos en reporte, usando datos originales")
                                return@map match
                            }
                        } else {
                            Log.w(TAG, "⚠️ ${match.id}: Sin datos en response")
                        }
                    } else {
                        Log.w(TAG, "⚠️ ${match.id}: Error ${reportResponse.code()}")
                    }
                }

                match
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error procesando partido ${match.id}: ${e.message}")
                match
            }
        }
    }

    /**
     * Ejecuta una operación con reintentos automáticos
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
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }

        Log.e(TAG, "❌ $operation falló después de $maxRetries intentos")
        return Result.failure(lastException ?: Exception("Operación fallida"))
    }
}
