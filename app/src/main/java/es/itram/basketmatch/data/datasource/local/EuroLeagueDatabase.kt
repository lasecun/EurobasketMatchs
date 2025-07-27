package es.itram.basketmatch.data.datasource.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar las nuevas columnas para nombres e imágenes de equipos en la tabla matches
                database.execSQL("""
                    ALTER TABLE matches ADD COLUMN homeTeamName TEXT NOT NULL DEFAULT ''
                """)
                database.execSQL("""
                    ALTER TABLE matches ADD COLUMN homeTeamLogo TEXT
                """)
                database.execSQL("""
                    ALTER TABLE matches ADD COLUMN awayTeamName TEXT NOT NULL DEFAULT ''
                """)
                database.execSQL("""
                    ALTER TABLE matches ADD COLUMN awayTeamLogo TEXT
                """)
            }
        }

        fun getDatabase(context: Context): EuroLeagueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EuroLeagueDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
