package es.itram.basketmatch.data.network

import com.google.gson.*
import es.itram.basketmatch.data.datasource.remote.dto.api.*
import java.lang.reflect.Type

/**
 * 🔧 Adaptador personalizado de Gson para GameApiDto
 *
 * La API de Euroleague usa diferentes nombres para el ID del partido en diferentes endpoints:
 * - "gameCode" en v2/games
 * - "code" en v3/games
 * - "id" en algunos casos
 *
 * Este adaptador busca en todos los campos posibles y usa el primero que encuentre.
 */
class GameApiDtoAdapter : JsonDeserializer<GameApiDto> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): GameApiDto {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("JSON inválido para GameApiDto")

        // Buscar el ID del partido en múltiples campos posibles
        val code = jsonObject.get("gameCode")?.asString
            ?: jsonObject.get("code")?.asString
            ?: jsonObject.get("id")?.asString

        val date = jsonObject.get("date")?.asString

        // Deserializar los equipos
        val local = jsonObject.get("local")?.let {
            context?.deserialize<GameTeamDto>(it, GameTeamDto::class.java)
        }

        val road = jsonObject.get("road")?.let {
            context?.deserialize<GameTeamDto>(it, GameTeamDto::class.java)
        }

        val venue = jsonObject.get("venue")?.let {
            context?.deserialize<VenueDto>(it, VenueDto::class.java)
        }

        val phase = jsonObject.get("phase")?.let {
            context?.deserialize<PhaseDto>(it, PhaseDto::class.java)
        }

        val round = jsonObject.get("round")?.let {
            context?.deserialize<RoundDto>(it, RoundDto::class.java)
        }

        val gameState = jsonObject.get("gameState")?.let {
            if (!it.isJsonNull) {
                context?.deserialize<GameStateDto>(it, GameStateDto::class.java)
            } else null
        }

        val boxscore = jsonObject.get("boxscore")?.let {
            if (!it.isJsonNull) {
                context?.deserialize<BoxscoreDto>(it, BoxscoreDto::class.java)
            } else null
        }

        return GameApiDto(
            code = code,
            date = date,
            local = local,
            road = road,
            venue = venue,
            phase = phase,
            round = round,
            gameState = gameState,
            boxscore = boxscore
        )
    }
}

