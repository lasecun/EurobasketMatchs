package es.itram.basketmatch.data.datasource.local.converter

import androidx.room.TypeConverter
import es.itram.basketmatch.domain.entity.MatchStatus

/**
 * Convertidor para el estado del partido en Room
 */
class MatchStatusConverter {

    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMatchStatus(status: String): MatchStatus {
        return MatchStatus.valueOf(status)
    }
}
