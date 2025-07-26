package ch.biketec.t.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.biketec.t.data.datasource.local.entity.StandingEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de clasificaciones en la base de datos local
 */
@Dao
interface StandingDao {

    @Query("SELECT * FROM standings ORDER BY position ASC")
    fun getAllStandings(): Flow<List<StandingEntity>>

    @Query("SELECT * FROM standings WHERE teamId = :teamId")
    fun getStandingByTeam(teamId: String): Flow<StandingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandings(standings: List<StandingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStanding(standing: StandingEntity)

    @Query("DELETE FROM standings")
    suspend fun deleteAllStandings()
}
