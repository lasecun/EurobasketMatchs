package es.itram.basketmatch.data.datasource.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import es.itram.basketmatch.data.datasource.local.dao.TeamDao
import es.itram.basketmatch.data.datasource.local.dao.MatchDao
import es.itram.basketmatch.data.datasource.local.dao.StandingDao
import es.itram.basketmatch.data.datasource.local.entity.TeamEntity
import es.itram.basketmatch.data.datasource.local.entity.MatchEntity
import es.itram.basketmatch.data.datasource.local.entity.StandingEntity
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
        StandingEntity::class
    ],
    version = 1,
    exportSchema = true
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

    companion object {
        const val DATABASE_NAME = "euroleague_database"

        @Volatile
        private var INSTANCE: EuroLeagueDatabase? = null

        fun getDatabase(context: Context): EuroLeagueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EuroLeagueDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
