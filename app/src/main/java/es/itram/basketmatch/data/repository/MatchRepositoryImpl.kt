package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueOfficialApiDataSource
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de partidos - Temporada 2025-2026 (E2025)
 *
 * Flujo simple:
 * 1. Al arrancar la app, descarga las 38 jornadas de E2025 (temporada 2025-2026)
 * 2. Guarda en base de datos local
 * 3. Filtra por fecha según el selector del usuario
 * 4. Muestra resultados si ya se jugó, "Programado" si no
 */
@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val officialApiDataSource: EuroLeagueOfficialApiDataSource,
    private val networkManager: NetworkManager
) : MatchRepository {

    companion object {
        private const val TAG = "MatchRepositoryImpl"
    }

    override fun getAllMatches(): Flow<List<Match>> = flow {
        Log.d(TAG, "📱 Obteniendo partidos de la temporada 2025-2026 (E2025)...")

        // Intentar sincronizar desde API si hay conexión
        if (networkManager.isConnected()) {
            try {
                Log.d(TAG, "🔄 Sincronizando desde API...")
                syncMatchesFromApi()
                Log.d(TAG, "✅ Sincronización completada")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en sincronización: ${e.message}", e)
                // Continuar para devolver datos locales aunque falle la sincronización
            }
        } else {
            Log.w(TAG, "⚠️ Sin conexión a internet, usando cache local")
        }

        // Devolver partidos desde la BD local
        matchDao.getAllMatches().collect { entities ->
            val matches = MatchMapper.toDomainList(entities)
            Log.d(TAG, "📊 Devolviendo ${matches.size} partidos desde BD local")
            emit(matches)
        }
    }.onStart {
        Log.d(TAG, "🚀 Iniciando flujo de partidos...")
    }

    override fun getMatchesByDate(date: LocalDateTime): Flow<List<Match>> {
        Log.d(TAG, "📱 Obteniendo partidos para fecha: $date")
        return matchDao.getMatchesByDate(date).map { entities ->
            Log.d(TAG, "✅ Partidos encontrados: ${entities.size}")
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByTeam(teamId: String): Flow<List<Match>> {
        return matchDao.getMatchesByTeam(teamId).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByStatus(status: MatchStatus): Flow<List<Match>> {
        return matchDao.getMatchesByStatus(status).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<Match>> {
        return matchDao.getMatchesBySeasonType(seasonType).map { entities ->
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchById(matchId: String): Flow<Match?> {
        return matchDao.getMatchById(matchId).map { entity ->
            entity?.let { MatchMapper.toDomain(it) }
        }
    }

    override suspend fun insertMatches(matches: List<Match>) {
        val entities = MatchMapper.fromDomainList(matches)
        matchDao.insertMatches(entities)
    }

    override suspend fun updateMatch(match: Match) {
        val entity = MatchMapper.fromDomain(match)
        matchDao.insertMatch(entity)
    }

    override suspend fun deleteAllMatches() {
        matchDao.deleteAllMatches()
    }

    /**
     * Sincroniza partidos de la temporada 2025-2026 desde la API oficial
     */
    private suspend fun syncMatchesFromApi() {
        if (!networkManager.isConnected()) {
            Log.w(TAG, "Sin conexión a internet")
            return
        }

        try {
            Log.d(TAG, "🌐 Descargando partidos de temporada E2025 desde API...")

            val result = officialApiDataSource.getAllMatches()

            if (result.isSuccess) {
                val remoteMatches = result.getOrNull() ?: emptyList()
                if (remoteMatches.isNotEmpty()) {
                    Log.d(TAG, "✅ Partidos descargados: ${remoteMatches.size}")

                    val domainMatches = MatchWebMapper.toDomainList(remoteMatches)
                    val entities = MatchMapper.fromDomainList(domainMatches)
                    matchDao.deleteAllMatches()
                    matchDao.insertMatches(entities)

                    Log.d(TAG, "💾 Partidos guardados en BD local")
                } else {
                    Log.w(TAG, "⚠️ API devolvió lista vacía")
                }
            } else {
                Log.e(TAG, "❌ Error en API: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sincronizando: ${e.message}", e)
        }
    }

    /**
     * Obtiene partidos de una fecha específica desde la API
     */
    fun getMatchResultsByDateFromApi(date: LocalDateTime): Flow<List<Match>> = flow {
        if (!networkManager.isConnected()) {
            Log.w(TAG, "Sin conexión a internet")
            emit(emptyList())
            return@flow
        }

        try {
            val targetDateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            Log.d(TAG, "🌐 Obteniendo partidos para: $targetDateString")

            val result = officialApiDataSource.getGamesByDate(
                dateFrom = targetDateString,
                dateTo = targetDateString
            )

            if (result.isSuccess) {
                val games = result.getOrNull() ?: emptyList()
                Log.d(TAG, "✅ Partidos encontrados: ${games.size}")

                val matches = games.map { game ->
                    Match(
                        id = game.id,
                        homeTeamId = game.homeTeamId,
                        homeTeamName = game.homeTeamName,
                        homeTeamLogo = game.homeTeamLogo,
                        awayTeamId = game.awayTeamId,
                        awayTeamName = game.awayTeamName,
                        awayTeamLogo = game.awayTeamLogo,
                        homeScore = game.homeScore,
                        awayScore = game.awayScore,
                        dateTime = LocalDateTime.parse(game.date + "T" + (game.time ?: "20:00")),
                        status = when (game.status) {
                            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.FINISHED -> MatchStatus.FINISHED
                            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.LIVE -> MatchStatus.LIVE
                            es.itram.basketmatch.data.datasource.remote.dto.MatchStatus.SCHEDULED -> MatchStatus.SCHEDULED
                            else -> MatchStatus.SCHEDULED
                        },
                        round = 1,
                        seasonType = SeasonType.REGULAR,
                        venue = game.venue ?: "Por determinar"
                    )
                }

                emit(matches.sortedBy { it.dateTime })
            } else {
                Log.e(TAG, "❌ Error: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción: ${e.message}", e)
            emit(emptyList())
        }
    }
}

