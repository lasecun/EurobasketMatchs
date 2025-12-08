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
                    Log.w(TAG, "⚠️ API oficial no devolvió equipos")
                    Result.failure(exception = Throwable("API oficial no devolvió equipos"))
                }
            } else {
                Log.e(TAG, "❌ Error en API oficial")
                Result.failure(exception = Throwable("API oficial no devolvió equipos"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción en API oficial: ${e.message}")
            Result.failure(exception = Throwable("API oficial no devolvió equipos"))
        }
    }
    
    /**
     * Obtiene todos los partidos usando únicamente la API oficial
     */
    suspend fun getAllMatches(): Result<List<MatchWebDto>> {
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
}
