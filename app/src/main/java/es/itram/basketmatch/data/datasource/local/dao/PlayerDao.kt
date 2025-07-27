package es.itram.basketmatch.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de jugadores en la base de datos local
 */
@Dao
interface PlayerDao {
    
    /**
     * Inserta una lista de jugadores en la base de datos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)
    
    /**
     * Inserta un jugador en la base de datos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    /**
     * Obtiene todos los jugadores de un equipo específico (método síncrono)
     */
    @Query("SELECT * FROM players WHERE teamCode = :teamCode ORDER BY jersey ASC")
    suspend fun getPlayersByTeamSync(teamCode: String): List<PlayerEntity>
    
    /**
     * Obtiene todos los jugadores de un equipo específico (Flow reactivo)
     */
    @Query("SELECT * FROM players WHERE teamCode = :teamCode ORDER BY jersey ASC")
    fun getPlayersByTeam(teamCode: String): Flow<List<PlayerEntity>>
    
    /**
     * Obtiene un jugador específico por su ID
     */
    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: String): PlayerEntity?
    
    /**
     * Elimina todos los jugadores de un equipo específico
     */
    @Query("DELETE FROM players WHERE teamCode = :teamCode")
    suspend fun deletePlayersByTeam(teamCode: String)
    
    /**
     * Elimina un jugador específico
     */
    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: String)
    
    /**
     * Obtiene todos los jugadores almacenados
     */
    @Query("SELECT * FROM players ORDER BY teamCode, jersey ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>
    
    /**
     * Verifica si existen jugadores para un equipo específico
     */
    @Query("SELECT COUNT(*) FROM players WHERE teamCode = :teamCode")
    suspend fun getPlayersCountForTeam(teamCode: String): Int
    
    /**
     * Obtiene los jugadores actualizados después de una fecha específica
     */
    @Query("SELECT * FROM players WHERE teamCode = :teamCode AND lastUpdated > :timestamp ORDER BY jersey ASC")
    suspend fun getPlayersUpdatedAfter(teamCode: String, timestamp: Long): List<PlayerEntity>
    
    /**
     * Elimina jugadores antiguos (más de X días)
     */
    @Query("DELETE FROM players WHERE lastUpdated < :timestamp")
    suspend fun deleteOldPlayers(timestamp: Long)
}