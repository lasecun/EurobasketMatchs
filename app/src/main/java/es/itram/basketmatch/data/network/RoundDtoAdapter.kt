package es.itram.basketmatch.data.network

import com.google.gson.*
import es.itram.basketmatch.data.datasource.remote.dto.api.RoundDto
import java.lang.reflect.Type

/**
 * 🔧 Adaptador personalizado de Gson para RoundDto
 *
 * La API de Euroleague puede devolver el campo "round" de dos formas:
 * 1. Como número simple: "round": 5
 * 2. Como objeto: "round": { "number": 5, "name": "Round 5" }
 *
 * Este adaptador maneja ambos casos automáticamente.
 */
class RoundDtoAdapter : JsonDeserializer<RoundDto?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RoundDto? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return try {
            when {
                // Caso 1: Es un número simple
                json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> {
                    val number = json.asInt
                    RoundDto(number = number, name = "Round $number")
                }

                // Caso 2: Es un objeto completo
                json.isJsonObject -> {
                    val jsonObject = json.asJsonObject
                    val number = jsonObject.get("number")?.asInt ?: 0
                    val name = jsonObject.get("name")?.asString
                    RoundDto(number = number, name = name)
                }

                // Caso 3: Cualquier otro caso, devolver null
                else -> null
            }
        } catch (e: Exception) {
            // Si hay cualquier error, devolver null en lugar de fallar
            null
        }
    }
}

