package es.itram.basketmatch.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.StandingDao
import es.itram.basketmatch.data.datasource.local.dao.PlayerDao
import es.itram.basketmatch.data.datasource.local.dao.TeamRosterDao
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
import es.itram.basketmatch.data.datasource.local.entity.PlayerEntity
import es.itram.basketmatch.data.datasource.local.entity.TeamRosterEntity
import es.itram.basketmatch.data.datasource.local.converter.LocalDateTimeConverter
import es.itram.basketmatch.data.datasource.local.converter.MatchStatusConverter
import es.itram.basketmatch.data.datasource.local.converter.SeasonTypeConverter

/**
 * Base de datos Room para la aplicación EuroLeague
 */
@Database(
    entities = [
        TeamEntity::class,
        MatchEntity::class,
        StandingEntity::class,
        PlayerEntity::class,
        TeamRosterEntity::class
    ],
    version = 4, // Incrementamos la versión para incluir logoUrl en TeamRosterEntity
    exportSchema = false // Deshabilitamos el schema export para desarrollo
)
@TypeConverters(
    LocalDateTimeConverter::class,
    MatchStatusConverter::class,
    SeasonTypeConverter::class
)
abstract class EuroLeagueDatabase : RoomDatabase() {

    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun standingDao(): StandingDao
    abstract fun playerDao(): PlayerDao
    abstract fun teamRosterDao(): TeamRosterDao

    companion object {
        const val DATABASE_NAME = "euroleague_database"
        
        // Nota: Las migraciones han sido deshabilitadas para desarrollo.
        // La base de datos se recreará automáticamente cuando haya cambios de esquema
        // gracias a fallbackToDestructiveMigration().
    }
}
