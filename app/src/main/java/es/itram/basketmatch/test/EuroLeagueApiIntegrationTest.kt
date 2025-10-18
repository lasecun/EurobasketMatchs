package es.itram.basketmatch.test

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 🏀 Test de integración - API oficial EuroLeague E2026
 *
 * Verifica:
 * ✅ Conectividad con la API oficial
 * ✅ Obtención de equipos de E2026
 * ✅ Obtención de partidos de E2026
 */
class EuroLeagueApiIntegrationTest @Inject constructor(
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource
) {

    companion object {
        private const val TAG = "EuroLeagueApiTest"
    }

    /**
     * Prueba completa de la integración E2026
     */
    suspend fun testIntegration() {
        Log.d(TAG, "🚀 Iniciando prueba de integración API E2026...")

        testApiAvailability()
        testTeamsRetrieval()
        testMatchesRetrieval()

        Log.d(TAG, "✅ Prueba de integración completada")
    }

    /**
     * Prueba la disponibilidad de la API oficial
     */
    private suspend fun testApiAvailability() {
        Log.d(TAG, "🔍 Verificando disponibilidad de API E2026...")

        try {
            val result = officialApiDataSource.getAllTeams()

            if (result.isSuccess) {
                val teams = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✅ API E2026 disponible - Equipos: ${teams.size}")
            } else {
                Log.e(TAG, "❌ API E2026 no disponible: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando API: ${e.message}", e)
        }
    }

    /**
     * Prueba obtención de equipos E2026
     */
    private suspend fun testTeamsRetrieval() {
        Log.d(TAG, "🏀 Obteniendo equipos E2026...")

        try {
            val result = officialApiDataSource.getAllTeams()

            if (result.isSuccess) {
                val teams = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✅ Equipos obtenidos: ${teams.size}")
                teams.take(3).forEach { team ->
                    Log.d(TAG, "  - ${team.name} (${team.shortCode})")
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo equipos: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
        }
    }

    /**
     * Prueba obtención de partidos E2026
     */
    private suspend fun testMatchesRetrieval() {
        Log.d(TAG, "🏀 Obteniendo partidos E2026...")

        try {
            val result = officialApiDataSource.getAllMatches()

            if (result.isSuccess) {
                val matches = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✅ Partidos obtenidos: ${matches.size}")
                matches.take(3).forEach { match ->
                    Log.d(TAG, "  - ${match.homeTeamName} vs ${match.awayTeamName} (${match.date})")
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo partidos: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
        }
    }
}
