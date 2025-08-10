package es.itram.basketmatch.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de equipos en la base de datos local
 */
@Dao
interface TeamDao {

    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams ORDER BY name ASC")
    suspend fun getAllTeamsSync(): List<TeamEntity>

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun getTeamCount(): Int

    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamById(teamId: String): Flow<TeamEntity?>

    @Query("SELECT * FROM teams WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE country = :country ORDER BY name ASC")
    fun getTeamsByCountry(country: String): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%'")
    fun searchTeams(query: String): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Query("SELECT * FROM teams WHERE code = :teamCode")
    fun getTeamByCode(teamCode: String): Flow<TeamEntity?>

    @Query("UPDATE teams SET is_favorite = :isFavorite WHERE id = :teamId")
    suspend fun updateFavoriteStatus(teamId: String, isFavorite: Boolean)

    @Query("UPDATE teams SET is_favorite = :isFavorite WHERE code = :teamCode")
    suspend fun updateFavoriteStatusByCode(teamCode: String, isFavorite: Boolean)

    @Query("DELETE FROM teams WHERE id = :teamId")
    suspend fun deleteTeam(teamId: String)

    @Query("DELETE FROM teams")
    suspend fun deleteAllTeams()
}
