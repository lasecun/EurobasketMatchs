package es.itram.basketmatch.data.datasource.local.converter

import androidx.room.TypeConverter
import es.itram.basketmatch.domain.entity.SeasonType

/**
 * Convertidor para el tipo de temporada en Room
 */
class SeasonTypeConverter {

    @TypeConverter
    fun fromSeasonType(seasonType: SeasonType): String {
        return seasonType.name
    }

    @TypeConverter
    fun toSeasonType(seasonType: String): SeasonType {
        return SeasonType.valueOf(seasonType)
    }
}
