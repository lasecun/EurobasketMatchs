package es.itram.basketmatch.data.repository

import android.util.Log
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.remote.EuroLeagueRemoteDataSource
import es.itram.basketmatch.data.mapper.MatchMapper
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamMapper
import es.itram.basketmatch.data.network.NetworkManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    private val teamDao: TeamDao,
    private val remoteDataSource: EuroLeagueRemoteDataSource,
    private val networkManager: NetworkManager
) : MatchRepository {

    companion object {
        private const val TAG = "MatchRepositoryImpl"
    }

    override fun getAllMatches(): Flow<List<Match>> {
        return matchDao.getAllMatches().map { entities ->
            MatchMapper.toDomainList(entities)
        }
        // TODO: Implementar refresh en background
        // .onStart {
        //     // Intentar sincronizar desde la web si hay conexión
        //     refreshMatchesIfNeeded()
        // }
    }

    override fun getMatchesByDate(date: LocalDateTime): Flow<List<Match>> {
        return matchDao.getMatchesByDate(date).map { entities ->
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
     * Sincroniza partidos desde la web si es necesario
     */
    private suspend fun refreshMatchesIfNeeded() {
        if (!networkManager.isConnected()) {
            Log.d(TAG, "No hay conexión a internet, usando datos locales")
            return
        }
        
        try {
            Log.d(TAG, "Sincronizando partidos desde la web oficial de EuroLeague...")
            
            val remoteResult = remoteDataSource.getAllMatches()
            
            if (remoteResult.isSuccess) {
                val remoteMatches = remoteResult.getOrNull() ?: emptyList()
                if (remoteMatches.isNotEmpty()) {
                    // Convertir DTOs web a entidades de dominio
                    val domainMatches = MatchWebMapper.toDomainList(remoteMatches)
                    
                    // Convertir a entidades de base de datos y guardar
                    val entities = MatchMapper.fromDomainList(domainMatches)
                    matchDao.insertMatches(entities)
                    
                    Log.d(TAG, "Partidos sincronizados exitosamente: ${domainMatches.size}")
                } else {
                    Log.w(TAG, "No se obtuvieron partidos del scraping")
                }
            } else {
                Log.e(TAG, "Error en sincronización remota de partidos", remoteResult.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando partidos desde la web", e)
            // Continuar con datos locales en caso de error
        }
    }
}
