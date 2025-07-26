package ch.biketec.t.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.biketec.t.data.datasource.local.dao.MatchDao
import ch.biketec.t.data.datasource.local.dao.StandingDao
import ch.biketec.t.data.datasource.local.dao.TeamDao
import ch.biketec.t.data.datasource.local.entity.MatchEntity
import ch.biketec.t.data.datasource.local.entity.StandingEntity
import ch.biketec.t.data.datasource.local.entity.TeamEntity

/**
 * Base de datos Room para la aplicación EuroLeague 2026
 */
@Database(
    entities = [
        TeamEntity::class,
        MatchEntity::class,
        StandingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EuroLeagueDatabase : RoomDatabase() {
    
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun standingDao(): StandingDao
}
