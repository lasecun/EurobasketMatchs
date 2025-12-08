package es.itram.basketmatch.domain.usecase

import android.util.Log
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.StandingRepository
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 📊 Use Case para calcular clasificación dinámicamente desde resultados de partidos
 *
 * Esta es la implementación CORRECTA y DINÁMICA:
 * - No usa datos estáticos
 * - Calcula standings desde los partidos finalizados
 * - Se actualiza automáticamente con cada resultado
 * - Sigue la lógica oficial de EuroLeague
 */
class CalculateStandingsUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
    private val standingRepository: StandingRepository
) {

    companion object {
        private const val TAG = "CalculateStandingsUseCase"
    }

    /**
     * Calcula y actualiza la clasificación para un tipo de temporada
     */
    suspend fun calculateAndSave(seasonType: SeasonType = SeasonType.REGULAR): Result<List<Standing>> {
        return try {
            Log.d(TAG, "📊 Calculating standings for season type: $seasonType")

            // 1. Obtener todos los partidos finalizados de la temporada
            val allMatches = matchRepository.getAllMatches().first()
            val finishedMatches = allMatches.filter {
                it.status == MatchStatus.FINISHED &&
                it.seasonType == seasonType &&
                it.homeScore != null &&
                it.awayScore != null
            }

            Log.d(TAG, "📋 Found ${finishedMatches.size} finished matches for $seasonType")

            if (finishedMatches.isEmpty()) {
                Log.w(TAG, "⚠️ No finished matches found for $seasonType")
                return Result.success(emptyList())
            }

            // 2. Obtener todos los equipos
            val teams = teamRepository.getAllTeams().first()
            Log.d(TAG, "👥 Processing ${teams.size} teams")

            // 3. Calcular estadísticas para cada equipo
            val teamStats = mutableMapOf<String, TeamStats>()

            teams.forEach { team ->
                teamStats[team.id] = TeamStats(
                    teamId = team.id,
                    teamName = team.name
                )
            }

            // 4. Procesar cada partido finalizado
            finishedMatches.forEach { match ->
                val homeTeamId = match.homeTeamId
                val awayTeamId = match.awayTeamId
                val homeScore = match.homeScore!!
                val awayScore = match.awayScore!!

                // Actualizar estadísticas del equipo local
                teamStats[homeTeamId]?.apply {
                    played++
                    pointsFor += homeScore
                    pointsAgainst += awayScore
                    if (homeScore > awayScore) {
                        won++
                    } else {
                        lost++
                    }
                }

                // Actualizar estadísticas del equipo visitante
                teamStats[awayTeamId]?.apply {
                    played++
                    pointsFor += awayScore
                    pointsAgainst += homeScore
                    if (awayScore > homeScore) {
                        won++
                    } else {
                        lost++
                    }
                }
            }

            // 5. Convertir a Standing y ordenar
            val standings = teamStats.values
                .filter { it.played > 0 } // Solo equipos que han jugado
                .map { stats ->
                    Standing(
                        teamId = stats.teamId,
                        position = 0, // Se asignará después de ordenar
                        played = stats.played,
                        won = stats.won,
                        lost = stats.lost,
                        pointsFor = stats.pointsFor,
                        pointsAgainst = stats.pointsAgainst,
                        pointsDifference = stats.pointsFor - stats.pointsAgainst,
                        seasonType = seasonType
                    )
                }
                .sortedWith(
                    compareByDescending<Standing> { it.won } // Primero por victorias
                        .thenByDescending { it.pointsDifference } // Luego por diferencia
                        .thenByDescending { it.pointsFor } // Luego por puntos a favor
                )
                .mapIndexed { index, standing ->
                    standing.copy(position = index + 1)
                }

            Log.d(TAG, "✅ Calculated ${standings.size} standings for $seasonType")

            // 6. Guardar en la base de datos
            if (standings.isNotEmpty()) {
                // Eliminar standings anteriores de este tipo de temporada
                standingRepository.deleteStandingsBySeasonType(seasonType)
                // Insertar los nuevos
                standingRepository.insertStandings(standings)
                Log.d(TAG, "💾 Saved ${standings.size} standings to database")
            }

            Result.success(standings)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calculating standings", e)
            Result.failure(e)
        }
    }

    /**
     * Recalcula standings para todos los tipos de temporada
     */
    suspend fun recalculateAll(): Result<Unit> {
        return try {
            Log.d(TAG, "🔄 Recalculating all standings")

            // Calcular para temporada regular
            calculateAndSave(SeasonType.REGULAR)

            // Calcular para playoffs (si hay partidos)
            calculateAndSave(SeasonType.PLAYOFFS)

            Log.d(TAG, "✅ All standings recalculated successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error recalculating all standings", e)
            Result.failure(e)
        }
    }
}

/**
 * Clase auxiliar para acumular estadísticas de un equipo
 */
private data class TeamStats(
    val teamId: String,
    val teamName: String,
    var played: Int = 0,
    var won: Int = 0,
    var lost: Int = 0,
    var pointsFor: Int = 0,
    var pointsAgainst: Int = 0
)

