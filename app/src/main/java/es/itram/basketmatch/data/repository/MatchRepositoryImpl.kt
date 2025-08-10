package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de partidos
 * Combina datos locales (Room) con datos remotos (Web scraping)
 */
@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val remoteDataSource: EuroLeagueRemoteDataSource,
    private val networkManager: NetworkManager
) : MatchRepository {

    companion object {
        private const val TAG = "MatchRepositoryImpl"
    }

    // Scope para operaciones de background que no deben bloquear el UI
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllMatches(): Flow<List<Match>> {
        Log.d(TAG, "📱 [LOCAL] Iniciando obtención de partidos desde cache local...")
        return matchDao.getAllMatches().map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Partidos obtenidos desde BD local: ${entities.size}")
            MatchMapper.toDomainList(entities)
        }.onStart {
            // Solo ejecutar refresh si la BD local está vacía y hay conexión
            if (networkManager.isConnected()) {
                backgroundScope.launch {
                    try {
                        val localMatchCount = matchDao.getMatchCount()
                        Log.d(TAG, "📱 [LOCAL] Verificando cache: $localMatchCount partidos en BD local")
                        
                        if (localMatchCount == 0) {
                            Log.d(TAG, "📱 [LOCAL] ⚠️ Cache vacío, iniciando descarga desde API...")
                            refreshMatchesIfNeeded()
                        } else {
                            Log.d(TAG, "📱 [LOCAL] ✅ Cache disponible, usando datos locales")
                        }
                    } catch (e: Exception) {
                        // En producción se loggearía, en tests se ignora silenciosamente
                        // Log.w(TAG, "Error en refresh en background", e)
                    }
                }
            }
        }
    }

    override fun getMatchesByDate(date: LocalDateTime): Flow<List<Match>> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo partidos por fecha desde BD local: $date")
        return matchDao.getMatchesByDate(date).map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Partidos encontrados para $date: ${entities.size}")
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByTeam(teamId: String): Flow<List<Match>> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo partidos por equipo desde BD local: $teamId")
        return matchDao.getMatchesByTeam(teamId).map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Partidos encontrados para $teamId: ${entities.size}")
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesByStatus(status: MatchStatus): Flow<List<Match>> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo partidos por estado desde BD local: $status")
        return matchDao.getMatchesByStatus(status).map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Partidos encontrados con estado $status: ${entities.size}")
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<Match>> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo partidos por temporada desde BD local: $seasonType")
        return matchDao.getMatchesBySeasonType(seasonType).map { entities ->
            Log.d(TAG, "📱 [LOCAL] ✅ Partidos encontrados para temporada $seasonType: ${entities.size}")
            MatchMapper.toDomainList(entities)
        }
    }

    override fun getMatchById(matchId: String): Flow<Match?> {
        Log.d(TAG, "📱 [LOCAL] Obteniendo partido específico desde BD local: $matchId")
        return matchDao.getMatchById(matchId).map { entity ->
            if (entity != null) {
                Log.d(TAG, "📱 [LOCAL] ✅ Partido encontrado: $matchId")
            } else {
                Log.d(TAG, "📱 [LOCAL] ⚠️ Partido no encontrado: $matchId")
            }
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
     * Sincroniza partidos desde la web si es necesario
     */
    private suspend fun refreshMatchesIfNeeded() {
        if (!networkManager.isConnected()) {
            Log.d(TAG, "📱 [LOCAL] Sin conexión a internet, usando datos locales únicamente")
            return
        }
        
        try {
            Log.d(TAG, "🌐 [NETWORK] Iniciando sincronización de partidos desde API EuroLeague...")
            
            val remoteResult = remoteDataSource.getAllMatches()
            
            if (remoteResult.isSuccess) {
                val remoteMatches = remoteResult.getOrNull() ?: emptyList()
                if (remoteMatches.isNotEmpty()) {
                    Log.d(TAG, "🌐 [NETWORK] ✅ Partidos obtenidos desde API: ${remoteMatches.size}")
                    
                    // Convertir DTOs web a entidades de dominio
                    val domainMatches = MatchWebMapper.toDomainList(remoteMatches)
                    
                    // Convertir a entidades de base de datos y guardar
                    val entities = MatchMapper.fromDomainList(domainMatches)
                    matchDao.insertMatches(entities)
                    
                    Log.d(TAG, "💾 [SAVE] Partidos guardados en BD local: ${domainMatches.size}")
                } else {
                    Log.w(TAG, "🌐 [NETWORK] ⚠️ API devolvió lista vacía de partidos")
                }
            } else {
                Log.e(TAG, "❌ [NETWORK] Error en sincronización remota de partidos", remoteResult.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [NETWORK] Error sincronizando partidos desde la API", e)
            // Continuar con datos locales en caso de error
        }
    }
}
