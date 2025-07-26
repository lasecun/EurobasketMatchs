package ch.biketec.t.data.datasource.local

import androidx.room.TypeConverter
import ch.biketec.t.domain.entity.MatchStatus
import ch.biketec.t.domain.entity.SeasonType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Convertidores de tipos para Room Database
 */
class Converters {

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDateTime(timestamp: Long?): LocalDateTime? {
        return timestamp?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
        }
    }

    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMatchStatus(status: String): MatchStatus {
        return MatchStatus.valueOf(status)
    }

    @TypeConverter
    fun fromSeasonType(seasonType: SeasonType): String {
        return seasonType.name
    }

    @TypeConverter
    fun toSeasonType(seasonType: String): SeasonType {
        return SeasonType.valueOf(seasonType)
    }
}
