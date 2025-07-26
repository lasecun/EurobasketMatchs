package es.itram.basketmatch.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO para operaciones de partidos en la base de datos local
 */
@Dao
interface MatchDao {

    @Query("SELECT * FROM matches ORDER BY dateTime ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE DATE(dateTime) = DATE(:date) ORDER BY dateTime ASC")
    fun getMatchesByDate(date: LocalDateTime): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE homeTeamId = :teamId OR awayTeamId = :teamId ORDER BY dateTime ASC")
    fun getMatchesByTeam(teamId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = :status ORDER BY dateTime ASC")
    fun getMatchesByStatus(status: MatchStatus): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE seasonType = :seasonType ORDER BY dateTime ASC")
    fun getMatchesBySeasonType(seasonType: SeasonType): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: String): Flow<MatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()
}
