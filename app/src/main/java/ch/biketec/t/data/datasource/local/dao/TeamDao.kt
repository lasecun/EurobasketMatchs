package ch.biketec.t.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.biketec.t.data.datasource.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de equipos en la base de datos local
 */
@Dao
interface TeamDao {

    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: String): Flow<TeamEntity?>

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%'")
    fun searchTeams(query: String): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Query("DELETE FROM teams")
    suspend fun deleteAllTeams()
}
