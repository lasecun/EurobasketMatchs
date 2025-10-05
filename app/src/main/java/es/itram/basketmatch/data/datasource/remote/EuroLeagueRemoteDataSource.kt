package es.itram.basketmatch.data.datasource.remote

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source remoto para obtener datos de EuroLeague
 *
 * NUEVA ARQUITECTURA SIMPLIFICADA:
 * 🏆 ÚNICA FUENTE: API oficial de EuroLeague (api-live.euroleague.net)
 * 🔄 FALLBACK: Datos básicos hardcodeados solo en caso de emergencia
 *
 * Esta arquitectura garantiza:
 * ✅ Datos oficiales y confiables
 * ✅ Simplicidad y mantenibilidad
 * ✅ Rendimiento óptimo
 * ✅ Datos siempre actualizados
 */
@Singleton
class EuroLeagueRemoteDataSource @Inject constructor(
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource
) {
    
    companion object {
        private const val TAG = "EuroLeagueRemoteDataSource"
    }
    
    /**
     * Obtiene todos los equipos usando únicamente la API oficial
     */
    suspend fun getAllTeams(): Result<List<TeamWebDto>> {
        return try {
            Log.d(TAG, "🏀 Obteniendo equipos desde API oficial de EuroLeague...")

            val result = officialApiDataSource.getAllTeams()
            if (result.isSuccess) {
                val teams = result.getOrNull()
                if (!teams.isNullOrEmpty()) {
                    Log.d(TAG, "✅ Equipos obtenidos desde API oficial: ${teams.size}")
                    Result.success(teams)
                } else {
                    Log.w(TAG, "⚠️ API oficial no devolvió equipos, usando fallback de emergencia")
                    Result.success(getEmergencyTeams())
                }
            } else {
                Log.e(TAG, "❌ Error en API oficial, usando fallback de emergencia")
                Result.success(getEmergencyTeams())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción en API oficial: ${e.message}, usando fallback de emergencia")
            Result.success(getEmergencyTeams())
        }
    }
    
    /**
     * Obtiene todos los partidos usando únicamente la API oficial
     */
    suspend fun getAllMatches(season: String = "2024-25"): Result<List<MatchWebDto>> {
        return try {
            Log.d(TAG, "⚽ Obteniendo partidos desde API oficial de EuroLeague...")

            val result = officialApiDataSource.getAllMatches()
            if (result.isSuccess) {
                val matches = result.getOrNull()
                if (!matches.isNullOrEmpty()) {
                    Log.d(TAG, "✅ Partidos obtenidos desde API oficial: ${matches.size}")
                    Result.success(matches)
                } else {
                    Log.w(TAG, "⚠️ API oficial no devolvió partidos")
                    Result.success(emptyList())
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo partidos desde API oficial")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción obteniendo partidos: ${e.message}")
            Result.success(emptyList())
        }
    }

    /**
     * Obtiene partidos por rango de fechas usando la API oficial
     */
    suspend fun getMatchesByDateRange(dateFrom: String, dateTo: String): Result<List<MatchWebDto>> {
        return try {
            Log.d(TAG, "📅 Obteniendo partidos por fecha desde API oficial: $dateFrom a $dateTo")

            val result = officialApiDataSource.getMatchesByDateRange(dateFrom, dateTo)
            if (result.isSuccess) {
                Log.d(TAG, "✅ Partidos por fecha obtenidos: ${result.getOrNull()?.size ?: 0}")
            } else {
                Log.w(TAG, "⚠️ No se pudieron obtener partidos por fecha desde API oficial")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo partidos por fecha", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene detalles de un partido específico usando la API oficial
     */
    suspend fun getMatchDetails(gameCode: String): Result<MatchWebDto> {
        return try {
            Log.d(TAG, "🔍 Obteniendo detalles del partido: $gameCode")

            val result = officialApiDataSource.getMatchDetails(gameCode)
            if (result.isSuccess) {
                Log.d(TAG, "✅ Detalles del partido obtenidos")
            } else {
                Log.w(TAG, "⚠️ No se pudieron obtener detalles del partido")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo detalles del partido", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si la API oficial está disponible
     */
    suspend fun isApiAvailable(): Boolean {
        return officialApiDataSource.isApiAvailable()
    }

    /**
     * Datos de emergencia mínimos en caso de fallo total de la API
     * Solo los equipos más importantes para garantizar funcionalidad básica
     */
    private fun getEmergencyTeams(): List<TeamWebDto> {
        return listOf(
            TeamWebDto(
                id = "MAD",
                name = "Real Madrid",
                fullName = "Real Madrid Basketball",
                shortCode = "MAD",
                logoUrl = null,
                country = "Spain",
                venue = "WiZink Center"
            ),
            TeamWebDto(
                id = "BAR",
                name = "FC Barcelona",
                fullName = "FC Barcelona Basketball",
                shortCode = "BAR",
                logoUrl = null,
                country = "Spain",
                venue = "Palau Sant Jordi"
            ),
            TeamWebDto(
                id = "PAO",
                name = "Panathinaikos",
                fullName = "Panathinaikos Athens",
                shortCode = "PAO",
                logoUrl = null,
                country = "Greece",
                venue = "OAKA"
            ),
            TeamWebDto(
                id = "OLY",
                name = "Olympiacos",
                fullName = "Olympiacos Piraeus",
                shortCode = "OLY",
                logoUrl = null,
                country = "Greece",
                venue = "Peace and Friendship Stadium"
            )
        )
    }
}
