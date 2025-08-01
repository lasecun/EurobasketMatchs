package es.itram.basketmatch.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerImageUtil @Inject constructor(
    private val httpClient: OkHttpClient
) {
    
    /**
     * Obtiene la URL de la imagen de un jugador desde el sitio web oficial de EuroLeague
     * 
     * NOTA: Esta función específicamente utiliza el sitio web oficial (www.euroleaguebasketball.net)
     * para obtener imágenes de jugadores, ya que la API de feeds no proporciona URLs de imágenes de jugadores.
     * Los datos de partidos y equipos se obtienen de la API de feeds (feeds.incrowdsports.com).
     * 
     * @param playerCode Código único del jugador
     * @param playerName Nombre del jugador
     * @param teamCode Código del equipo
     * @return URL de la imagen o null si no se encuentra
     */
    suspend fun getPlayerImageUrl(
        playerCode: String,
        playerName: String,
        teamCode: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Convertir el nombre del jugador al formato URL de EuroLeague
            val formattedName = formatPlayerNameForUrl(playerName)
            
            // Construir la URL de la página del jugador
            // NOTA: Usamos el sitio web oficial para imágenes porque la API de feeds no las incluye
            val playerPageUrl = "https://www.euroleaguebasketball.net/euroleague/players/$formattedName/$playerCode/"
            
            // Hacer la petición HTTP
            val request = Request.Builder()
                .url(playerPageUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val htmlContent = response.body?.string() ?: return@withContext null
                
                // Buscar el patrón de la imagen en el JSON incrustado
                val photoPattern = """"photo":"([^"]*\.png)"""".toRegex()
                val match = photoPattern.find(htmlContent)
                
                match?.groupValues?.get(1)
            } else {
                null
            }
        } catch (e: Exception) {
            // Log del error si es necesario
            null
        }
    }
    
    /**
     * Convierte el nombre del jugador al formato requerido por la URL de EuroLeague
     * Ejemplo: "SERGIO LLULL" -> "llull-sergio"
     */
    private fun formatPlayerNameForUrl(playerName: String): String {
        val parts = playerName.trim().split(" ")
        return if (parts.size >= 2) {
            val lastName = parts.last().lowercase()
            val firstName = parts.first().lowercase()
            "$lastName-$firstName"
        } else {
            playerName.lowercase().replace(" ", "-")
        }
    }
    
    /**
     * Genera una URL de imagen placeholder basada en las iniciales del jugador
     */
    fun generatePlaceholderImageUrl(playerName: String): String {
        val initials = playerName.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercase() ?: "" }
            .joinToString("")
        
        // Usar un servicio de avatares con las iniciales
        return "https://ui-avatars.com/api/?name=$initials&size=400&background=004996&color=ffffff&font-size=0.4"
    }
    
    companion object {
        // URLs de imágenes por defecto para casos especiales
        val DEFAULT_PLAYER_IMAGES = mapOf(
            "003733" to "https://media-cdn.incrowdsports.com/c0c0c586-9005-47a5-970d-86b57458701d.png", // Sergio Llull
            // Aquí se pueden agregar más imágenes conocidas
        )
    }
}
