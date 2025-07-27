import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Test simple para verificar que la API JSON de EuroLeague está funcionando
 */
fun main() {
    println("🧪 === TEST SIMPLE JSON API ===")
    
    val jsonUrl = "https://www.euroleaguebasketball.net/_next/data/a52CgOKFrJehM6XbgT-b_/es/euroleague/game-center.json"
    
    try {
        println("📡 Conectando a: $jsonUrl")
        
        val url = URL(jsonUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }
        
        val responseCode = connection.responseCode
        println("📊 Código de respuesta: $responseCode")
        
        if (responseCode == 200) {
            val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
            println("✅ JSON recibido, tamaño: ${jsonResponse.length} caracteres")
            
            // Parseado básico para verificar estructura
            val json = Json { ignoreUnknownKeys = true }
            val jsonElement = json.parseToJsonElement(jsonResponse)
            val jsonObj = jsonElement.jsonObject
            
            // Verificar si tiene headerData
            val headerData = jsonObj["headerData"]?.jsonObject
            if (headerData != null) {
                println("✅ headerData encontrado")
                
                val euroleague = headerData["euroleague"]?.jsonObject
                if (euroleague != null) {
                    println("✅ euroleague encontrado")
                    
                    val clubs = euroleague["clubs"]?.jsonObject
                    if (clubs != null) {
                        println("✅ clubs encontrado")
                        
                        val clubsList = clubs["clubs"]?.jsonArray
                        if (clubsList != null) {
                            println("✅ Lista de clubs encontrada: ${clubsList.size} equipos")
                            
                            // Mostrar algunos equipos
                            clubsList.take(3).forEach { club ->
                                val clubObj = club.jsonObject
                                val name = clubObj["name"]?.jsonPrimitive?.content
                                val logo = clubObj["logo"]?.jsonObject?.get("image")?.jsonPrimitive?.content
                                println("   🏀 $name - Logo: $logo")
                            }
                        }
                    }
                }
            }
            
            // Verificar si tiene pageProps con partidos
            val pageProps = jsonObj["pageProps"]?.jsonObject
            if (pageProps != null) {
                println("✅ pageProps encontrado")
                
                val gameGroups = pageProps["currentRoundGameGroups"]?.jsonArray
                if (gameGroups != null) {
                    println("✅ currentRoundGameGroups encontrado: ${gameGroups.size} grupos")
                    
                    var totalGames = 0
                    gameGroups.forEach { group ->
                        val groupObj = group.jsonObject
                        val games = groupObj["games"]?.jsonArray
                        if (games != null) {
                            totalGames += games.size
                        }
                    }
                    println("⚽ Total de partidos encontrados: $totalGames")
                    
                    // Mostrar un partido de ejemplo
                    if (gameGroups.isNotEmpty()) {
                        val firstGroup = gameGroups[0].jsonObject
                        val games = firstGroup["games"]?.jsonArray
                        if (games != null && games.isNotEmpty()) {
                            val firstGame = games[0].jsonObject
                            val homeTeam = firstGame["home"]?.jsonObject?.get("name")?.jsonPrimitive?.content
                            val awayTeam = firstGame["away"]?.jsonObject?.get("name")?.jsonPrimitive?.content
                            val date = firstGame["date"]?.jsonPrimitive?.content
                            println("   ⚽ Ejemplo: $homeTeam vs $awayTeam - $date")
                        }
                    }
                }
            }
            
            println("\n✅ === API JSON FUNCIONA CORRECTAMENTE ===")
            
        } else {
            println("❌ Error HTTP: $responseCode")
        }
        
    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
    }
}
