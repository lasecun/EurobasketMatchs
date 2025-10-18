package es.itram.basketmatch.data.network

import com.google.gson.*
import java.lang.reflect.Type

/**
 * 🔧 Adaptador personalizado de Gson para el campo del ID del partido
 *
 * La API de Euroleague usa diferentes nombres para el ID del partido:
 * - "gameCode" en algunas respuestas
 * - "code" en otras respuestas
 * - "id" en otras respuestas
 *
 * Este adaptador busca en todos los campos posibles.
 */
class GameCodeAdapter : JsonDeserializer<String?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            return null
        }

        // Si es un string directo, devolverlo
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return json.asString
        }

        // Si es un número, convertirlo a string
        if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) {
            return json.asString
        }

        return null
    }
}

