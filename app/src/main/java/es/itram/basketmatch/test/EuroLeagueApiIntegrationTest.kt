package es.itram.basketmatch.test

import android.util.Log
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 🏀 Clase de prueba para verificar la integración de la API oficial de EuroLeague
 *
 * Esta clase permite probar:
 * ✅ Conectividad con la API oficial
 * ✅ Obtención de equipos y partidos
 * ✅ Funcionamiento del sistema híbrido (oficial + fallback)
 */
class EuroLeagueApiIntegrationTest @Inject constructor(
    private val euroLeagueRemoteDataSource: EuroLeagueRemoteDataSource,
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource
) {

    companion object {
        private const val TAG = "EuroLeagueApiTest"
    }

    /**
     * Prueba completa de la integración
     */
    suspend fun testIntegration() {
        Log.d(TAG, "🚀 Iniciando prueba de integración de API oficial de EuroLeague...")

        // Prueba 1: Verificar disponibilidad de API oficial
        testApiAvailability()

        // Prueba 2: Obtener equipos con sistema híbrido
        testTeamsRetrieval()

        // Prueba 3: Obtener partidos con sistema híbrido
        testMatchesRetrieval()

        Log.d(TAG, "✅ Prueba de integración completada")
    }

    /**
     * Prueba la disponibilidad de la API oficial
     */
    private suspend fun testApiAvailability() {
        Log.d(TAG, "🔍 Verificando disponibilidad de API oficial...")

        try {
            val isAvailable = officialApiDataSource.isApiAvailable()
            if (isAvailable) {
                Log.d(TAG, "✅ API oficial disponible y funcionando")
            } else {
                Log.w(TAG, "⚠️ API oficial no disponible, se usará fallback")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando API oficial: ${e.message}")
        }
    }

    /**
     * Prueba la obtención de equipos
     */
    private suspend fun testTeamsRetrieval() {
        Log.d(TAG, "🏀 Probando obtención de equipos...")

        try {
            val result = euroLeagueRemoteDataSource.getAllTeams()

            if (result.isSuccess) {
                val teams = result.getOrNull()
                if (teams != null && teams.isNotEmpty()) {
                    Log.d(TAG, "✅ Equipos obtenidos exitosamente: ${teams.size} equipos")
                    Log.d(TAG, "📋 Primeros equipos:")
                    teams.take(3).forEach { team ->
                        Log.d(TAG, "  - ${team.name} (${team.shortCode}) - ${team.country}")
                    }
                } else {
                    Log.w(TAG, "⚠️ No se obtuvieron equipos")
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo equipos: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción obteniendo equipos: ${e.message}")
        }
    }

    /**
     * Prueba la obtención de partidos
     */
    private suspend fun testMatchesRetrieval() {
        Log.d(TAG, "⚽ Probando obtención de partidos...")

        try {
            val result = euroLeagueRemoteDataSource.getAllMatches()

            if (result.isSuccess) {
                val matches = result.getOrNull()
                if (matches != null && matches.isNotEmpty()) {
                    Log.d(TAG, "✅ Partidos obtenidos exitosamente: ${matches.size} partidos")
                    Log.d(TAG, "📋 Próximos partidos:")
                    matches.take(3).forEach { match ->
                        Log.d(TAG, "  - ${match.homeTeamName} vs ${match.awayTeamName}")
                        Log.d(TAG, "    Fecha: ${match.date} | Estado: ${match.status}")
                    }
                } else {
                    Log.w(TAG, "⚠️ No se obtuvieron partidos")
                }
            } else {
                Log.e(TAG, "❌ Error obteniendo partidos: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción obteniendo partidos: ${e.message}")
        }
    }

    /**
     * Prueba específica de la API oficial (solo equipos)
     */
    suspend fun testOfficialApiDirect() {
        Log.d(TAG, "🔬 Probando API oficial directamente...")

        try {
            val result = officialApiDataSource.getAllTeams()

            if (result.isSuccess) {
                val teams = result.getOrNull()
                Log.d(TAG, "✅ API oficial funcionando: ${teams?.size ?: 0} equipos obtenidos")
            } else {
                Log.w(TAG, "⚠️ API oficial falló, fallback activado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en API oficial: ${e.message}")
        }
    }

    /**
     * Ejecuta todas las pruebas de forma síncrona (para testing rápido)
     */
    fun runAllTestsBlocking() {
        runBlocking {
            testIntegration()
        }
    }
}
