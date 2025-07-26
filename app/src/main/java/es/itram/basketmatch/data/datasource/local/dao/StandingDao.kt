package es.itram.basketmatch.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
import es.itram.basketmatch.domain.entity.SeasonType
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de clasificación en la base de datos local
 */
@Dao
interface StandingDao {

    @Query("SELECT * FROM standings ORDER BY position ASC")
    fun getAllStandings(): Flow<List<StandingEntity>>

    @Query("SELECT * FROM standings WHERE seasonType = :seasonType ORDER BY position ASC")
    fun getStandingsBySeasonType(seasonType: SeasonType): Flow<List<StandingEntity>>

    @Query("SELECT * FROM standings WHERE teamId = :teamId")
    fun getStandingByTeam(teamId: String): Flow<StandingEntity?>

    @Query("SELECT * FROM standings WHERE position <= :maxPosition ORDER BY position ASC")
    fun getTopStandings(maxPosition: Int): Flow<List<StandingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandings(standings: List<StandingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStanding(standing: StandingEntity)

    @Query("DELETE FROM standings")
    suspend fun deleteAllStandings()

    @Query("DELETE FROM standings WHERE seasonType = :seasonType")
    suspend fun deleteStandingsBySeasonType(seasonType: SeasonType)
}
