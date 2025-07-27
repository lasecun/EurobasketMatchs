package es.itram.basketmatch.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con rosters de equipos en la base de datos local
 */
@Dao
interface TeamRosterDao {
    
    /**
     * Obtiene un roster de equipo por su código
     */
    @Query("SELECT * FROM team_rosters WHERE teamCode = :teamCode")
    suspend fun getTeamRoster(teamCode: String): TeamRosterEntity?
    
    /**
     * Obtiene un roster de equipo como Flow
     */
    @Query("SELECT * FROM team_rosters WHERE teamCode = :teamCode")
    fun getTeamRosterFlow(teamCode: String): Flow<TeamRosterEntity?>
    
    /**
     * Inserta o actualiza un roster de equipo
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamRoster(teamRoster: TeamRosterEntity)
    
    /**
     * Obtiene todos los rosters almacenados
     */
    @Query("SELECT * FROM team_rosters ORDER BY teamName ASC")
    fun getAllTeamRosters(): Flow<List<TeamRosterEntity>>
    
    /**
     * Elimina un roster específico
     */
    @Query("DELETE FROM team_rosters WHERE teamCode = :teamCode")
    suspend fun deleteTeamRoster(teamCode: String)
    
    /**
     * Elimina todos los rosters
     */
    @Query("DELETE FROM team_rosters")
    suspend fun deleteAllTeamRosters()
    
    /**
     * Verifica si un roster existe y cuándo fue actualizado por última vez
     */
    @Query("SELECT lastUpdated FROM team_rosters WHERE teamCode = :teamCode")
    suspend fun getLastUpdated(teamCode: String): Long?
}
